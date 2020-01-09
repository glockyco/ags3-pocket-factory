package pocketfactory.messages

import akka.actor.ActorRef

final case class PushRequest(replyTo: ActorRef)
