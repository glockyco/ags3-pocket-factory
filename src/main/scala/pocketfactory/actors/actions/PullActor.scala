package pocketfactory.actors.actions

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.messages.{PullRequest, PullResponse}

object PullActor {
  sealed trait State
  case object Pulling extends State

  def props(ref: ActorRef, replyTo: ActorRef): Props =
    Props(new PullActor(ref, replyTo))
}

class PullActor(ref: ActorRef, replyTo: ActorRef)
  extends FSM[PullActor.State, Unit] {

  import PullActor._

  startWith(Pulling, ())

  onTransition {
    case _ -> Pulling =>
      ref ! PullRequest(self)
  }

  when(Pulling) {
    case Event(PullResponse(), _) =>
      replyTo ! PullResponse()
      stop()
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
