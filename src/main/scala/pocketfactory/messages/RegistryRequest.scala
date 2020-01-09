package pocketfactory.messages

import akka.actor.ActorRef

final case class RegistryRequest(replyTo: ActorRef)
