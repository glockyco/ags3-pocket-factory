package pocketfactory.messages

import akka.actor.ActorRef

final case class PullRequest(replyTo: ActorRef)
