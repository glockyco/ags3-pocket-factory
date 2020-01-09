package pocketfactory.messages

import akka.actor.ActorRef

final case class OrderStatusRequest(replyTo: ActorRef)
