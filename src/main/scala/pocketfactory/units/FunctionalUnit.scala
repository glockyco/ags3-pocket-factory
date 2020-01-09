package pocketfactory.units

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, blocking}
import scala.concurrent.duration.FiniteDuration

trait FunctionalUnit {
  var isInitialized: Boolean = false
  var isIdle: Boolean = true

  def initialize(duration: FiniteDuration): Future[Unit] = {
    if (isInitialized) {
      Future.unit
    } else {
      Future({ blocking(Thread.sleep(duration.toMillis)); isInitialized = true })
    }
  }

  def work(duration: FiniteDuration): Future[Unit] = {
    if (isIdle) {
      isIdle = false
      Future({ blocking(Thread.sleep(duration.toMillis)); isIdle = true })
    } else {
      Future.failed(new RuntimeException("Machine must be idle to start work."))
    }
  }
}
