package pocketfactory.actors

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.Delays
import pocketfactory.Machines.Machine
import pocketfactory.messages._
import pocketfactory.units.OutputUnit

import scala.concurrent.ExecutionContext

object OutputActor {
  sealed trait State
  case object Initializing extends State
  case object Registering extends State
  case object Idle extends State
  case object HandshakingPull extends State
  case object Pulling extends State

  final case class Data(replyTo: ActorRef)

  def props(machine: Machine, unit: OutputUnit): Props =
    Props(new OutputActor(machine, unit))
}

class OutputActor(machine: Machine, unit: OutputUnit)
  extends FSM[OutputActor.State, OutputActor.Data] {

  import OutputActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(Initializing, null)

  onTransition {
    case Initializing -> Registering => // do nothing
    case Registering -> Idle => nextStateData.replyTo ! RegistrationResponse(machine, self)

    case Idle -> HandshakingPull => nextStateData.replyTo ! HandshakePullResponse()
    case HandshakingPull -> Pulling => unit.pull()
    case Pulling -> Idle => stateData.replyTo ! PullResponse()
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
  }

  when(HandshakingPull) {
    case Event(PullRequest(replyTo), _) => goto(Pulling).using(Data(replyTo))
  }

  when(Pulling, Delays.Poll) {
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
