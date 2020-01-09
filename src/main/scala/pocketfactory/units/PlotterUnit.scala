package pocketfactory.units

import pocketfactory.Delays

object PlotterUnit {
  def apply(): PlotterUnit = new PlotterUnit()
}

class PlotterUnit extends FunctionalUnit {
  def plot(): Unit = work(Delays.Plot)
  def pull(): Unit = work(Delays.Pull)
  def push(): Unit = work(Delays.Push)

  initialize(Delays.InitializePlotter)
}
