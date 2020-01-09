package pocketfactory.actors.ui

import akka.actor.{ActorRef, FSM, Props, Terminated}
import pocketfactory.actors.OrderActor
import pocketfactory.domain.Actions.{Move, Plot}
import pocketfactory.messages.{OrderStatusRequest, OrderStatusResponse}
import pocketfactory.{Delays, UiModel}

import scala.concurrent.ExecutionContext

object OrderStateActor {
  sealed trait State
  case object WaitingForOrder extends State
  case object TrackingOrder extends State

  final case class Data(order: Option[ActorRef])

  final case class GetOrder()
  final case class GetOrderState()
  final case class TrackOrder(ref: ActorRef)

  def props(model: UiModel): Props = Props(new OrderStateActor(model))
}

class OrderStateActor(model: UiModel)
  extends FSM[OrderStateActor.State, OrderStateActor.Data] {

  import OrderStateActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(WaitingForOrder, Data(None))

  onTransition {
    case _ -> WaitingForOrder =>
      cancelTimer("get-order-state")
      startTimerWithFixedDelay("get-order", GetOrder(), Delays.Poll)

    case _ -> TrackingOrder =>
      cancelTimer("get-order")
      startTimerWithFixedDelay("get-order-state", GetOrderState(), Delays.Poll)

      nextStateData.order.foreach { order =>
        context.watch(order)
      }
  }

  when(WaitingForOrder) {
    case Event(GetOrder(), _) =>
      context.system
        .actorSelection(s"/user/main/factory/orders/order-*")
        .resolveOne(Delays.Poll)
        .foreach(self ! TrackOrder(_))
      stay()
  }

  when(TrackingOrder) {
    case Event(GetOrderState(), _) =>
      stateData.order.foreach(_ ! OrderStatusRequest(self))
      stay()
  }

  whenUnhandled {
    case Event(TrackOrder(order), _) =>
      goto(TrackingOrder).using(Data(Some(order)))

    case Event(OrderStatusResponse(data), _) =>
      model.orderStatus.value = orderStatus(data)
      stay()

    case Event(Terminated(_), _) =>
      //model.orderStatus.value = ""
      goto(WaitingForOrder)

    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  def orderStatus(status: Option[OrderActor.Data]): String = {
    status.map(
      data => data.route.map({
        case Move(_, from, to) => s"Move: ${from.path.name} -> ${to.path.name}"
        case Plot(_, ref, item) => s"Plot: ${ref.path.name} = ${item.color}"
      }).zipWithIndex.map({
        case (step, i) => if (i == data.index) s"$step <-" else step
      }).mkString("\n")
    ).getOrElse("")
  }

  initialize()
}
