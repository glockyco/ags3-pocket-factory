package pocketfactory.actors

import akka.actor.{FSM, Props}
import akka.stream.{Materializer, SystemMaterializer}
import pocketfactory.Delays
import pocketfactory.domain.{Layout, Registry}
import pocketfactory.messages.RegistryResponse

import scala.concurrent.ExecutionContext

object FactoryActor {
  sealed trait State
  case object Initializing extends State
  case object Registering extends State
  case object Running extends State

  def props(): Props = Props(new FactoryActor())
}

class FactoryActor extends FSM[FactoryActor.State, Registry] {

  import FactoryActor._

  implicit val executor: ExecutionContext = context.dispatcher
  implicit val mat: Materializer = SystemMaterializer(context.system).materializer

  startWith(Initializing, Registry())

  onTransition {
    case Initializing -> Registering =>
      context.actorOf(RegistryActor.props(self), "registry")

    case Registering -> Running =>
      val registry = nextStateData

      //context.actorOf(LoggingActor.props(registry))

      val layout = Layout(registry)

      val router = context.actorOf(RoutingActor.props(layout), "router")
      val orders = context.actorOf(OrdersActor.props(router), "orders")
  }

  when(Initializing, Delays.InitializeFactory) {
    case Event(StateTimeout, _) => goto(Registering)
  }

  when(Registering) {
    case Event(RegistryResponse(registry), _) =>
      goto(Running).using(registry)
  }

  when(Running) {
    FSM.NullFunction
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
