package pocketfactory.domain

import akka.actor.ActorRef
import pocketfactory.Machines
import pocketfactory.Machines.Machine

object Registry {
  def apply(): Registry = {
    new Registry()
  }

  private def apply(entries: Map[Machine, ActorRef]): Registry = {
    new Registry(entries)
  }

  private def empty(): Map[Machine, ActorRef] = {
    Map(
      Machines.In -> null,
      Machines.Out -> null,
      Machines.C1 -> null,
      Machines.C2 -> null,
      Machines.P1 -> null,
      Machines.P2 -> null,
      Machines.P3 -> null,
      Machines.P4 -> null,
    )
  }
}

class Registry(val entries: Map[Machine, ActorRef] = Registry.empty()) {
  val isComplete: Boolean = entries.values.forall(_ != null)
  val missingMachines: Iterable[Machine] = entries.filter(_._2 == null).keys

  def register(machine: Machine, ref: ActorRef): Registry = {
    Registry(entries + (machine -> ref))
  }
}
