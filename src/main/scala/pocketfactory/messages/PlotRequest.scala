package pocketfactory.messages

import akka.actor.ActorRef

final case class PlotRequest(replyTo: ActorRef)
