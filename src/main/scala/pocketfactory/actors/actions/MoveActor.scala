package pocketfactory.actors.actions

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.domain.Actions.Move
import pocketfactory.messages._

object MoveActor {
  sealed trait State
  case object Handshaking extends State
  case object Moving extends State
  case object Pulling extends State
  case object Pushing extends State

  def props(move: Move, replyTo: ActorRef): Props =
    Props(new MoveActor(move, replyTo))
}

class MoveActor(move: Move, replyTo: ActorRef)
  extends FSM[MoveActor.State, Unit] {

  import MoveActor._

  startWith(Handshaking, ())

  onTransition {
    case _ -> Handshaking =>
      context.actorOf(HandshakeActor.props(move.from, move.to, self), s"handshake-${move.id}")

    case _ -> Moving =>
      log.info(s"MOVE: ${move.from.path.name} -> ${move.to.path.name}")
      context.actorOf(PushActor.props(move.from, self), s"push-${move.id}")
      context.actorOf(PullActor.props(move.to, self), s"pull-${move.id}")

    case _ -> Pulling => // do nothing
    case _ -> Pushing => // do nothing
  }

  when(Handshaking) {
    case Event(HandshakeResponse(), _) => goto(Moving)
  }

  when(Moving) {
    case Event(PullResponse(), _) => goto(Pushing)
    case Event(PushResponse(), _) => goto(Pulling)
  }

  when(Pulling) {
    case Event(PullResponse(), _) =>
      replyTo ! MoveResponse()
      stop()
  }

  when(Pushing) {
    case Event(PushResponse(), _) =>
      replyTo ! MoveResponse()
      stop()
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
