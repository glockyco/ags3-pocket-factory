package pocketfactory.domain

import pocketfactory.{Colors, Machines}
import pocketfactory.Machines.Machine
import Nodes.{Conveyor, Input, Node, Output, Plotter}

object Layout {
  def apply(registry: Registry): Layout = {
    val in = Input(registry.entries(Machines.In))
    val out = Output(registry.entries(Machines.Out))

    val c1 = Conveyor(registry.entries(Machines.C1))
    val c2 = Conveyor(registry.entries(Machines.C2))

    val p1 = Plotter(registry.entries(Machines.P1), Set(Colors.Red))
    val p2 = Plotter(registry.entries(Machines.P2), Set(Colors.Green))
    val p3 = Plotter(registry.entries(Machines.P3), Set(Colors.Blue))
    val p4 = Plotter(registry.entries(Machines.P4), Set(Colors.White))

    in.addNeighbor(c1)
    out.addNeighbor(c2)

    c1.addNeighbor(in)
    c1.addNeighbor(c2)
    c1.addNeighbor(p1)
    c1.addNeighbor(p2)

    c2.addNeighbor(out)
    c2.addNeighbor(c1)
    c2.addNeighbor(p3)
    c2.addNeighbor(p4)

    p1.addNeighbor(c1)
    p2.addNeighbor(c1)
    p3.addNeighbor(c2)
    p4.addNeighbor(c2)

    new Layout(Map(
      Machines.In -> in,
      Machines.Out -> out,
      Machines.C1 -> c1,
      Machines.C2 -> c2,
      Machines.P1 -> p1,
      Machines.P2 -> p2,
      Machines.P3 -> p3,
      Machines.P4 -> p4,
    ))
  }
}

final case class Layout(items: Map[Machine, Node])
