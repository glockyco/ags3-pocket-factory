package pocketfactory

import scalafx.beans.property.StringProperty

class UiModel {

  val order = StringProperty("")

  val inStatus = StringProperty("Status")
  val outStatus = StringProperty("Status")

  val c1Status = StringProperty("Status")
  val c2Status = StringProperty("Status")

  val p1Status = StringProperty("Status")
  val p2Status = StringProperty("Status")
  val p3Status = StringProperty("Status")
  val p4Status = StringProperty("Status")

  val orderStatus = StringProperty("")
}
