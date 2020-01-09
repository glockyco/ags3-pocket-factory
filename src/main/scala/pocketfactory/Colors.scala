package pocketfactory

import scala.util.Random

object Colors {
  sealed trait Color
  case object Red extends Color
  case object Green extends Color
  case object Blue extends Color
  case object White extends Color

  def all: Seq[Color] = {
    Seq(Red, Green, Blue, White)
  }

  def random: Color = {
    all(Random.nextInt(all.length))
  }
}
