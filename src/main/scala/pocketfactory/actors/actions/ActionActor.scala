package pocketfactory.actors.actions

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.domain.Actions
import pocketfactory.domain.Actions.Action
import pocketfactory.messages.{ActionResponse, MoveResponse, PlotResponse}

object ActionActor {
  sealed trait State
  case object Processing extends State
  case object ProcessingMove extends State
  case object ProcessingPlot extends State

  sealed trait InternalMessage
  case object GotoProcessingMove extends InternalMessage
  case object GotoProcessingPlot extends InternalMessage

  def props(action: Action, replyTo: ActorRef): Props =
    Props(new ActionActor(action, replyTo))
}

class ActionActor(action: Action, replyTo: ActorRef)
  extends FSM[ActionActor.State, Unit] {

  import ActionActor._

  startWith(Processing, ())

  onTransition {
    case _ -> Processing =>
      action match {
        case _: Actions.Move => self ! GotoProcessingMove
        case _: Actions.Plot => self ! GotoProcessingPlot
      }

    case _ -> ProcessingMove =>
      action match {
        case move: Actions.Move =>
          context.actorOf(MoveActor.props(move, self), s"move-${action.id}")
      }

    case _ -> ProcessingPlot =>
      action match {
        case plot: Actions.Plot =>
          context.actorOf(PlotActor.props(plot, self), s"plot-${action.id}")
      }
  }

  when(Processing) {
    case Event(GotoProcessingMove, _) => goto(ProcessingMove)
    case Event(GotoProcessingPlot, _) => goto(ProcessingPlot)
  }

  when(ProcessingMove) {
    case Event(MoveResponse(), _) =>
      replyTo ! ActionResponse()
      stop()
  }

  when(ProcessingPlot) {
    case Event(PlotResponse(), _) =>
      replyTo ! ActionResponse()
      stop()
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
