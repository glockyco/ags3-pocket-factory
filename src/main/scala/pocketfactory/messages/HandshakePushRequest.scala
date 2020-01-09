package pocketfactory.messages

import akka.actor.ActorRef

final case class HandshakePushRequest(replyTo: ActorRef)
