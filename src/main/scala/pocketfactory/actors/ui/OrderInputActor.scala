package pocketfactory.actors.ui

import akka.actor.{ActorRef, FSM, Props}
import pocketfactory.domain.{Order, OrderItem}
import pocketfactory.messages.{OrderInputRequest, OrderRequest}
import pocketfactory.{Colors, Delays}

import scala.concurrent.ExecutionContext

object OrderInputActor {
  sealed trait State
  case object Initializing extends State
  case object Running extends State

  final case class PollOrders()
  final case class Run(orders: ActorRef)

  def props(): Props = Props(new OrderInputActor())
}

class OrderInputActor
  extends FSM[OrderInputActor.State, Option[ActorRef]] {

  import OrderInputActor._

  implicit val executor: ExecutionContext = context.dispatcher

  startWith(Initializing, None)

  startTimerWithFixedDelay("poll", PollOrders(), Delays.Poll)

  onTransition {
    case Initializing -> Running =>
      cancelTimer("poll")
  }

  when(Initializing) {
    case Event(PollOrders(), _) =>
      context.system
        .actorSelection(s"/user/main/factory/orders")
        .resolveOne(Delays.Poll)
        .foreach(self ! Run(_))
      stay()

    case Event(Run(orders), _) =>
      goto(Running).using(Some(orders))
  }

  when(Running) {
    case Event(OrderInputRequest(input), _) =>
      Seq(input)
        .map(input => input.toLowerCase.trim)
        .map(string => string.map[Colors.Color](charToColor))
        .map(colors => Order(colors.map(OrderItem)))
        .foreach(order => stateData.foreach(_ ! OrderRequest(order)))
      stay()
  }

  def charToColor(char: Char): Colors.Color = {
    char match {
      case 'r' | '1' => Colors.Red
      case 'g' | '2' => Colors.Green
      case 'b' | '3' => Colors.Blue
      case 'w' | '4' => Colors.White
      case _ => Colors.random
    }
  }

  whenUnhandled {
    case event =>
      log.warning(s"Unhandled event $event in state $stateName.")
      stay()
  }

  initialize()
}
