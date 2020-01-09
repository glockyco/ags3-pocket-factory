package pocketfactory

import pocketfactory.domain.Actions.Action

package object domain {
  type Route = Seq[Action]
  val Route: Seq.type = Seq
}
