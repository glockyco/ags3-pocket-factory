package pocketfactory.actors

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.Delays
import pocketfactory.domain.Registry
import pocketfactory.messages.{RegistrationRequest, RegistrationResponse, RegistryRequest, RegistryResponse}

import scala.concurrent.ExecutionContext

object RegistryActor {
  sealed trait State
  case object Registering extends State
  case object Registered extends State

  final case class PollMachines()

  def props(replyTo: ActorRef): Props = Props(new RegistryActor(replyTo))
}

class RegistryActor(replyTo: ActorRef)
  extends FSM[RegistryActor.State, Registry] {

  import RegistryActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(Registering, Registry())

  startTimerWithFixedDelay("poll", PollMachines(), Delays.Poll)

  onTransition {
    case Registering -> Registered =>
      cancelTimer("poll")
      replyTo ! RegistryResponse(nextStateData)
  }

  when(Registering) {
    case Event(PollMachines(), _) =>
      for (machine <- stateData.missingMachines) {
        context
          .actorSelection(s"/user/main/${machine.toString}")
          .resolveOne(Delays.Poll)
          .foreach(ref => ref ! RegistrationRequest(self))
      }
      stay()

    case Event(RegistrationResponse(machine, ref), _) =>
      val registry = stateData.register(machine, ref)

      if (!registry.isComplete) {
        stay().using(registry)
      } else {
        goto(Registered).using(registry)
      }
  }

  when(Registered) {
    case Event(RegistryRequest(ref), _) =>
      ref ! RegistryResponse(stateData)
      stay()
  }

  whenUnhandled {
    case Event(RegistryRequest(_), _) => stay
    case Event(RegistrationResponse(_, _), _) => stay()

    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
