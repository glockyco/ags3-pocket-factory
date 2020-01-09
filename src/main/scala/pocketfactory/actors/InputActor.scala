package pocketfactory.actors

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.Delays
import pocketfactory.Machines.Machine
import pocketfactory.messages._
import pocketfactory.units.InputUnit

import scala.concurrent.ExecutionContext

object InputActor {
  sealed trait State
  case object Initializing extends State
  case object Registering extends State
  case object Idle extends State
  case object HandshakingPush extends State
  case object Pushing extends State

  final case class Data(replyTo: ActorRef)

  def props(machine: Machine, unit: InputUnit): Props =
    Props(new InputActor(machine, unit))
}

class InputActor(machine: Machine, unit: InputUnit)
  extends FSM[InputActor.State, InputActor.Data] {

  import InputActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(Initializing, null)

  onTransition {
    case Initializing -> Registering => // do nothing
    case Registering -> Idle => nextStateData.replyTo ! RegistrationResponse(machine, self)

    case Idle -> HandshakingPush => nextStateData.replyTo ! HandshakePushResponse()
    case HandshakingPush -> Pushing => unit.push()
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
    case Event(HandshakePushRequest(replyTo), _) => goto(HandshakingPush).using(Data(replyTo))
  }

  when(HandshakingPush) {
    case Event(PushRequest(replyTo), _) => goto(Pushing).using(Data(replyTo))
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
