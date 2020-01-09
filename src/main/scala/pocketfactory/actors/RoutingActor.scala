package pocketfactory.actors

import akka.actor.{Actor, Props}
import pocketfactory.Machines._
import pocketfactory.domain.Actions.{Action, Move, Plot}
import pocketfactory.domain.Nodes.{Node, Plotter}
import pocketfactory.domain.{Layout, Order, Route}
import pocketfactory.messages.{RoutingRequest, RoutingResponse}

import scala.collection.mutable

object RoutingActor {
  def props(layout: Layout): Props = Props(new RoutingActor(layout))
}

class RoutingActor(layout: Layout) extends Actor {

  override def receive: Receive = {
    case RoutingRequest(order, replyTo) =>
      replyTo ! RoutingResponse(route(order))
  }

  override def unhandled(message: Any): Unit = {
    context.system.log.info(s"Unhandled: $message")
  }

  private def route(order: Order): Route = {
    var actions: mutable.Seq[Action] = mutable.Seq[Action]()

    var from: Node = layout.items(In)

    for (item <- order.items) {
      val to = layout.items.values.find({
        case n: Plotter => n.canPlot(item)
        case _ => false
      }).get

      val path: Seq[Node] = calculatePath(from, to)
      val moves: Seq[Move] = pathToMoves(path)

      actions ++= moves :+ Plot(to.ref, item)

      from = to
    }

    val path = calculatePath(from, layout.items(Out))
    val moves = pathToMoves(path)
    actions ++= moves

    actions.toSeq
  }

  private def calculatePath(from: Node, to: Node): Seq[Node] = {
    val visited: mutable.Stack[Node] = mutable.Stack[Node]()

    def recurse(current: Node): Boolean = {
      visited.push(current)

      if (current == to) {
        return true
      }

      for (next <- current.neighbors) {
        if (!visited.contains(next)) {
          if (recurse(next)) {
            return true
          }
        }
      }

      visited.pop()
      false
    }

    if (!recurse(from)) {
      throw new Exception("No path found.")
    }

    visited.reverse.toSeq
  }

  private def pathToMoves(path: Seq[Node]): Seq[Move] = {
    for (i <- 0 to path.length - 2)
      yield Move(path(i).ref, path(i + 1).ref)
  }
}
