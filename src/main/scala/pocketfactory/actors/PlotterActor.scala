package pocketfactory.actors

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.Delays
import pocketfactory.Machines.Machine
import pocketfactory.messages._
import pocketfactory.units.PlotterUnit

import scala.concurrent.ExecutionContext

object PlotterActor {
  sealed trait State
  case object Initializing extends State
  case object Registering extends State
  case object Idle extends State
  case object HandshakingPull extends State
  case object HandshakingPush extends State
  case object Pulling extends State
  case object Pushing extends State
  case object Plotting extends State

  final case class Data(replyTo: ActorRef)

  def props(machine: Machine, unit: PlotterUnit): Props =
    Props(new PlotterActor(machine, unit))
}

class PlotterActor(machine: Machine, unit: PlotterUnit)
  extends FSM[PlotterActor.State, PlotterActor.Data] {

  import PlotterActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(Initializing, null)

  onTransition {
    case Initializing -> Registering => // do nothing
    case Registering -> Idle => nextStateData.replyTo ! RegistrationResponse(machine, self)

    case Idle -> HandshakingPull => nextStateData.replyTo ! HandshakePullResponse()
    case Idle -> HandshakingPush => nextStateData.replyTo ! HandshakePushResponse()
    case HandshakingPull -> Pulling => unit.pull()
    case HandshakingPush -> Pushing => unit.push()
    case Pulling -> Idle => stateData.replyTo ! PullResponse()
    case Pushing -> Idle => stateData.replyTo ! PushResponse()

    case Idle -> Plotting => unit.plot()
    case Plotting -> Idle => stateData.replyTo ! PlotResponse()
  }

  when(Initializing, Delays.Poll) {
    case Event(StateTimeout, _) => if (unit.isInitialized) goto(Registering) else stay()
    case Event(RegistrationRequest(_), _) => stay() // ignore until initialized
  }

  when(Registering) {
    case Event(RegistrationRequest(replyTo), _) => goto(Idle).using(Data(replyTo))
  }

  when(Idle) {
    case Event(HandshakePullRequest(replyTo), _) => goto(HandshakingPull).using(Data(replyTo))
    case Event(HandshakePushRequest(replyTo), _) => goto(HandshakingPush).using(Data(replyTo))
    case Event(PlotRequest(replyTo), _) => goto(Plotting).using(Data(replyTo))
  }

  when(HandshakingPull) {
    case Event(PullRequest(replyTo), _) => goto(Pulling).using(Data(replyTo))
  }

  when(HandshakingPush) {
    case Event(PushRequest(replyTo), _) => goto(Pushing).using(Data(replyTo))
  }

  when(Pulling, Delays.Poll) {
    case Event(StateTimeout, _) => if (unit.isIdle) goto(Idle) else stay()
  }

  when(Pushing, Delays.Poll) {
    case Event(StateTimeout, _) => if (unit.isIdle) goto(Idle) else stay()
  }

  when(Plotting, Delays.Poll) {
    case Event(StateTimeout, _) => if (unit.isIdle) goto(Idle) else stay()
  }

  whenUnhandled {
    case Event(RegistrationRequest(replyTo), _) =>
      replyTo ! RegistrationResponse(machine, self)
      stay()

    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
