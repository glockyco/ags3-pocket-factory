package pocketfactory.domain

import pocketfactory.Colors.Color

object Order {
  var id = 0

  def apply(items: Seq[OrderItem]): Order = {
    Order(nextId(), items)
  }

  def nextId(): Int = {
    id += 1
    id
  }
}

final case class OrderItem(color: Color)

final case class Order(id: Int, items: Seq[OrderItem]) {
  override def hashCode(): Int = id.##
}
