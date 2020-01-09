package pocketfactory.actors.actions

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.domain.Actions.Plot
import pocketfactory.messages.{PlotRequest, PlotResponse}

object PlotActor {
  sealed trait State
  case object Plotting extends State

  def props(plot: Plot, replyTo: ActorRef): Props =
    Props(new PlotActor(plot, replyTo))
}

class PlotActor(plot: Plot, replyTo: ActorRef)
  extends FSM[PlotActor.State, Unit] {

  import PlotActor._

  startWith(Plotting, ())

  onTransition {
    case _ -> Plotting =>
      log.info(s"PLOT: ${plot.ref.path.name} - ${plot.item}")
      plot.ref ! PlotRequest(self)
  }

  when(Plotting) {
    case Event(PlotResponse(), _) =>
      replyTo ! PlotResponse()
      stop()
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
