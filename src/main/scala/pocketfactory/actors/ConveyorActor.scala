package pocketfactory.actors

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.Delays
import pocketfactory.Machines.Machine
import pocketfactory.messages._
import pocketfactory.units.ConveyorUnit

import scala.concurrent.ExecutionContext

object ConveyorActor {
  sealed trait State
  case object Initializing extends State
  case object Registering extends State
  case object Idle extends State
  case object TurningPull extends State
  case object TurningPush extends State
  case object HandshakingPull extends State
  case object HandshakingPush extends State
  case object Pulling extends State
  case object Pushing extends State

  final case class Data(replyTo: ActorRef)

  def props(machine: Machine, unit: ConveyorUnit): Props =
    Props(new ConveyorActor(machine, unit))
}

class ConveyorActor(machine: Machine, unit: ConveyorUnit)
  extends FSM[ConveyorActor.State, ConveyorActor.Data] {

  import ConveyorActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(Initializing, null)

  onTransition {
    case Initializing -> Registering => // do nothing
    case Registering -> Idle => nextStateData.replyTo ! RegistrationResponse(machine, self)

    case Idle -> TurningPull => unit.turn()
    case Idle -> TurningPush => unit.turn()
    case TurningPull -> HandshakingPull => stateData.replyTo ! HandshakePullResponse()
    case TurningPush -> HandshakingPush => stateData.replyTo ! HandshakePushResponse()
    case HandshakingPull -> Pulling => unit.pull()
    case HandshakingPush -> Pushing => unit.push()
    case Pulling -> Idle => stateData.replyTo ! PullResponse()
    case Pushing -> Idle => stateData.replyTo ! PushResponse()
  }

  when(Initializing, Delays.Poll) {
    case Event(StateTimeout, _) => if (unit.isInitialized) goto(Registering) else stay()
    case Event(RegistrationRequest(_), _) => stay() // ignore until initialized
  }

  when(Registering) {
    case Event(RegistrationRequest(replyTo), _) => goto(Idle).using(Data(replyTo))
  }

  when(Idle) {
    case Event(HandshakePullRequest(replyTo), _) => goto(TurningPull).using(Data(replyTo))
    case Event(HandshakePushRequest(replyTo), _) => goto(TurningPush).using(Data(replyTo))
  }

  when(TurningPull, Delays.Poll) {
    case Event(StateTimeout, _) => if (unit.isIdle) goto(HandshakingPull) else stay()
  }

  when(TurningPush, Delays.Poll) {
    case Event(StateTimeout, _) => if (unit.isIdle) goto(HandshakingPush) else stay()
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
