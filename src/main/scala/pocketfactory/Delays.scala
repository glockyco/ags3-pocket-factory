package pocketfactory

import scala.concurrent.duration.{DurationInt, FiniteDuration}

object Delays {
  private val Default = 3.seconds

  val Pull: FiniteDuration = Default
  val Push: FiniteDuration = Default
  val Turn: FiniteDuration = Default
  val Plot: FiniteDuration = Default

  private val Initialize: FiniteDuration = 10.millis

  val InitializeFactory: FiniteDuration = Initialize
  val InitializeInput: FiniteDuration = Initialize
  val InitializeOutput: FiniteDuration = Initialize
  val InitializeConveyor: FiniteDuration = Initialize
  val InitializePlotter: FiniteDuration = Initialize

  val Poll: FiniteDuration = 10.millis
}
