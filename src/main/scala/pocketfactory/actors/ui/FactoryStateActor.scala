package pocketfactory.actors.ui

import akka.actor.FSM.{CurrentState, SubscribeTransitionCallBack, Transition}
import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.Machines.Machine
import pocketfactory.domain.Registry
import pocketfactory.messages.{RegistryRequest, RegistryResponse}
import pocketfactory.{Delays, Machines, UiModel}

import scala.concurrent.ExecutionContext

object FactoryStateActor {
  sealed trait State
  case object Initializing extends State
  case object Running extends State

  final case class PollRegistry()

  def props(model: UiModel): Props = Props(new FactoryStateActor(model))
}

class FactoryStateActor(model: UiModel)
  extends FSM[FactoryStateActor.State, Registry] {

  import FactoryStateActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(Initializing, Registry())

  startTimerWithFixedDelay("poll", PollRegistry(), Delays.Poll)

  onTransition {
    case Initializing -> Running =>
      cancelTimer("poll")
  }

  when(Initializing) {
    case Event(PollRegistry(), _) =>
      context.system
        .actorSelection(s"/user/main/factory/registry")
        .resolveOne(Delays.Poll)
        .foreach(_ ! RegistryRequest(self))
      stay()

    case Event(RegistryResponse(registry), _) =>
      registry.entries.values.foreach(_ ! SubscribeTransitionCallBack(self))
      goto(Running).using(registry)
  }

  when(Running) {
    case Event(CurrentState(ref, state), _) =>
      updateUiState(ref, state)
      stay()

    case Event(Transition(ref, _, newState), _) =>
      updateUiState(ref, newState)
      stay()
  }

  def updateUiState[S](ref: ActorRef, state: S): Unit = {
    val machines: Map[ActorRef, Machine] =
      for ((k, v) <- stateData.entries) yield (v, k)

    machines(ref) match {
      case Machines.In => model.inStatus.value = state.toString
      case Machines.Out => model.outStatus.value = state.toString
      case Machines.C1 => model.c1Status.value = state.toString
      case Machines.C2 => model.c2Status.value = state.toString
      case Machines.P1 => model.p1Status.value = state.toString
      case Machines.P2 => model.p2Status.value = state.toString
      case Machines.P3 => model.p3Status.value = state.toString
      case Machines.P4 => model.p4Status.value = state.toString
    }
  }

  whenUnhandled {
    case Event(RegistryResponse(_), _) =>
      stay()

    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
