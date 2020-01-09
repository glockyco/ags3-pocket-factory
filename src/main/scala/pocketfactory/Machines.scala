package pocketfactory

object Machines {
  sealed trait Machine
  case object In extends Machine
  case object Out extends Machine
  case object C1 extends Machine
  case object C2 extends Machine
  case object P1 extends Machine
  case object P2 extends Machine
  case object P3 extends Machine
  case object P4 extends Machine
}