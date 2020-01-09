package pocketfactory.messages

import akka.actor.ActorRef
import pocketfactory.domain.Order

final case class RoutingRequest(order: Order, replyTo: ActorRef)
