package pocketfactory.actors

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.actors.actions.ActionActor
import pocketfactory.domain.{Order, Route}
import pocketfactory.messages._

object OrderActor {
  sealed trait State
  case object Routing extends State
  case object Idle extends State
  case object Processing extends State

  final case class Data(route: Route, index: Int)

  final case class GotoProcessing(index: Int)
  final case class GotoStop()

  def props(order: Order, router: ActorRef, replyTo: ActorRef): Props =
    Props(new OrderActor(order, router, replyTo))
}

class OrderActor(order: Order, router: ActorRef, replyTo: ActorRef)
  extends FSM[OrderActor.State, Option[OrderActor.Data]] {

  import OrderActor._

  startWith(Routing, None)

  onTransition {
    case _ -> Routing =>
      router ! RoutingRequest(order, self)

    case _ -> Idle =>
      val nextIndex = nextStateData.map(_.index + 1).getOrElse(0)
      val hasMoreActions = nextIndex < nextStateData.map(_.route.length).getOrElse(0)

      if (hasMoreActions) {
        self ! GotoProcessing(nextIndex)
      } else {
        replyTo ! OrderResponse(order)
        self ! GotoStop()
      }

    case _ -> Processing =>
      val action = nextStateData.get.route(nextStateData.get.index)
      context.actorOf(ActionActor.props(action, self), s"action-${action.id}")
  }

  when(Routing) {
    case Event(RoutingResponse(route), _) => goto(Idle).using(Some(Data(route, -1)))
  }

  when(Idle) {
    case Event(GotoProcessing(i), _) => goto(Processing).using(stateData.map(_.copy(index = i)))
    case Event(GotoStop(), _) => stop()
  }

  when(Processing) {
    case Event(ActionResponse(), _) => goto(Idle)
  }

  whenUnhandled {
    case Event(OrderStatusRequest(ref), data) =>
      ref ! OrderStatusResponse(data)
      stay()

    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
