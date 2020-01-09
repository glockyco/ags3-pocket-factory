package pocketfactory.messages

import akka.actor.ActorRef

final case class RegistrationRequest(replyTo: ActorRef)
