package pocketfactory.units

import pocketfactory.Delays

object InputUnit {
  def apply(): InputUnit = new InputUnit()
}

class InputUnit extends FunctionalUnit {
  def push(): Unit = work(Delays.Push)

  initialize(Delays.InitializeInput)
}
