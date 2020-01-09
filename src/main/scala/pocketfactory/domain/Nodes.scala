package pocketfactory.domain

import akka.actor.ActorRef
import pocketfactory.Colors.Color

object Nodes {
  sealed trait Node {
    val ref: ActorRef
    var neighbors: Seq[Node]
    def addNeighbor(neighbor: Node): Unit = { neighbors = neighbors :+ neighbor }
  }

  object Input { def apply(ref: ActorRef): Input = Input(ref, Seq()) }

  final case class Input(
    ref: ActorRef,
    var neighbors: Seq[Node],
  ) extends Node

  object Output { def apply(ref: ActorRef): Output = Output(ref, Seq()) }

  final case class Output(
    ref: ActorRef,
    var neighbors: Seq[Node],
  ) extends Node

  object Conveyor { def apply(ref: ActorRef): Conveyor = Conveyor(ref, Seq()) }

  final case class Conveyor(
    ref: ActorRef,
    var neighbors: Seq[Node],
  ) extends Node

  object Plotter { def apply(ref: ActorRef, colors: Set[Color]): Plotter = Plotter(ref, Seq(), colors) }

  final case class Plotter(
    ref: ActorRef,
    var neighbors: Seq[Node],
    colors: Set[Color]
  ) extends Node {
    def canPlot(orderItem: OrderItem): Boolean =
      colors.contains(orderItem.color)
  }
}
