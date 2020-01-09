package pocketfactory.domain

import akka.actor.ActorRef

object Actions {
  object Action {
    var id = 0
    def nextId(): Int = { id += 1; id }
  }

  object Move {
    def apply(from: ActorRef, to: ActorRef): Move = {
      Move(Action.nextId(), from, to)
    }
  }

  object Plot {
    def apply(ref: ActorRef, item: OrderItem): Plot = {
      Plot(Action.nextId(), ref, item)
    }
  }

  sealed trait Action { def id: Int }

  final case class Move(id: Int, from: ActorRef, to: ActorRef) extends Action {
    override def hashCode(): Int = id.##
  }

  final case class Plot(id: Int, ref: ActorRef, item: OrderItem) extends Action {
    override def hashCode(): Int = id.##
  }
}
