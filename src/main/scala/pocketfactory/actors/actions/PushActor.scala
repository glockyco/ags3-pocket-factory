package pocketfactory.actors.actions

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.messages.{PushRequest, PushResponse}

object PushActor {
  sealed trait State
  case object Pushing extends State

  def props(ref: ActorRef, replyTo: ActorRef): Props =
    Props(new PushActor(ref, replyTo))
}

class PushActor(ref: ActorRef, replyTo: ActorRef)
  extends FSM[PushActor.State, Unit] {

  import PushActor._

  startWith(Pushing, ())

  onTransition {
    case _ -> Pushing =>
      ref ! PushRequest(self)
  }

  when(Pushing) {
    case Event(PushResponse(), _) =>
      replyTo ! PushResponse()
      stop()
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
