package pocketfactory.actors.ui

import akka.actor.{FSM, Props}
import pocketfactory.UiModel

object UiActor {
  sealed trait State
  case object Initializing extends State
  case object Running extends State

  def props(model: UiModel): Props = Props(new FactoryStateActor(model))
}

class UiActor(model: UiModel) extends FSM[UiActor.State, Unit] {
}
