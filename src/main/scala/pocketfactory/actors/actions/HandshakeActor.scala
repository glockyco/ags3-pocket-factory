package pocketfactory.actors.actions

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.messages._

object HandshakeActor {
  sealed trait State
  case object Handshaking extends State
  case object HandshakingPull extends State
  case object HandshakingPush extends State

  def props(from: ActorRef, to: ActorRef, replyTo: ActorRef): Props =
    Props(new HandshakeActor(from, to, replyTo))
}

class HandshakeActor(from: ActorRef, to: ActorRef, replyTo: ActorRef)
  extends FSM[HandshakeActor.State, Unit] {

  import HandshakeActor._

  startWith(Handshaking, ())

  onTransition {
    case _ -> Handshaking =>
      from ! HandshakePushRequest(self)
      to ! HandshakePullRequest(self)

    case _ -> HandshakingPull => // do nothing
    case _ -> HandshakingPush => // do nothing
  }

  when(Handshaking) {
    case Event(HandshakePullResponse(), _) => goto(HandshakingPush)
    case Event(HandshakePushResponse(), _) => goto(HandshakingPull)
  }

  when(HandshakingPull) {
    case Event(HandshakePullResponse(), _) =>
      replyTo ! HandshakeResponse()
      stop()
  }

  when(HandshakingPush) {
    case Event(HandshakePushResponse(), _) =>
      replyTo ! HandshakeResponse()
      stop()
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
