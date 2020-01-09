package pocketfactory.units

import pocketfactory.Delays

object ConveyorUnit {
  def apply(): ConveyorUnit = new ConveyorUnit()
}

class ConveyorUnit extends FunctionalUnit {
  def turn(): Unit = work(Delays.Turn)
  def pull(): Unit = work(Delays.Pull)
  def push(): Unit = work(Delays.Push)

  initialize(Delays.InitializeConveyor)
}
