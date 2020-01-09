package pocketfactory.actors

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.domain.Order
import pocketfactory.messages.{OrderRequest, OrderResponse}

object OrdersActor {
  sealed trait State
  case object Idle extends State
  case object Active extends State

  final case class GotoActive()

  def props(router: ActorRef): Props = Props(new OrdersActor(router))
}

class OrdersActor(router: ActorRef)
  extends FSM[OrdersActor.State, Seq[Order]] {

  import OrdersActor._

  startWith(Idle, Seq())

  onTransition {
    case _ -> Idle =>
      if (nextStateData.nonEmpty) {
        self ! GotoActive()
      }

    case _ -> Active =>
      val order = nextStateData(0)
      context.actorOf(OrderActor.props(order, router, self), s"order-${order.id}")
  }

  when(Idle) {
    case Event(OrderRequest(order), _) =>
      val orders = stateData :+ order
      goto(Active).using(orders)

    case Event(GotoActive(), _) =>
      goto(Active)
  }

  when(Active) {
    case Event(OrderRequest(order), _) =>
      val orders = stateData :+ order
      stay().using(orders)

    case Event(OrderResponse(order), _) =>
      log.info(s"DONE: $order\n\n")

      val orders = stateData.filter(_ != order)
      goto(Idle).using(orders)
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
