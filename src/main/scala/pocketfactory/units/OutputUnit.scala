package pocketfactory.units

import pocketfactory.Delays

object OutputUnit {
  def apply(): OutputUnit = new OutputUnit()
}

class OutputUnit extends FunctionalUnit {
  def pull(): Unit = work(Delays.Pull)

  initialize(Delays.InitializeOutput)
}
