package pocketfactory.messages

import pocketfactory.actors.OrderActor

final case class OrderStatusResponse(data: Option[OrderActor.Data])
