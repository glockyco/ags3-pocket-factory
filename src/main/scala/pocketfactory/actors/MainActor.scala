package pocketfactory.actors

import akka.actor.Actor
import pocketfactory.Machines
import pocketfactory.units.{ConveyorUnit, InputUnit, OutputUnit, PlotterUnit}

class MainActor extends Actor {

  context.actorOf(InputActor.props(Machines.In, InputUnit()), Machines.In.toString)
  context.actorOf(OutputActor.props(Machines.Out, OutputUnit()), Machines.Out.toString)

  context.actorOf(ConveyorActor.props(Machines.C1, ConveyorUnit()), Machines.C1.toString)
  context.actorOf(ConveyorActor.props(Machines.C2, ConveyorUnit()), Machines.C2.toString)

  context.actorOf(PlotterActor.props(Machines.P1, PlotterUnit()), Machines.P1.toString)
  context.actorOf(PlotterActor.props(Machines.P2, PlotterUnit()), Machines.P2.toString)
  context.actorOf(PlotterActor.props(Machines.P3, PlotterUnit()), Machines.P3.toString)
  context.actorOf(PlotterActor.props(Machines.P4, PlotterUnit()), Machines.P4.toString)

  context.actorOf(FactoryActor.props(), "factory")

  override def receive: Receive = {
    case message => unhandled(message)
  }

  override def unhandled(message: Any): Unit = {
    context.system.log.info(s"Unhandled: $message")
  }
}
