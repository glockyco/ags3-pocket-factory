package pocketfactory.messages

import akka.actor.ActorRef

final case class HandshakePullRequest(replyTo: ActorRef)
