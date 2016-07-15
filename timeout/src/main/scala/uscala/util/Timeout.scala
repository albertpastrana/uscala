package uscala.util

import scala.concurrent.duration._

/**
  * Class that allows to query if a specific amount of time has
  * elapsed or not.
  *
  * As an example, it can be useful when some polling mechanism
  * is needed to verify if an operation is successful or not.
  *
  * It can also be used as a replacement for a stopwatch, if the
  * only thing you need is to check the time elapsed every so on.
  *
  * @param length the duration of the timeout
  * @param ticker the ticker to use to count the time elapsed
  */
case class Timeout(length: Duration, ticker: Ticker = Timeout.DefaultTicker) {

  val start = ticker.read

  /**
    * Returns a new timeout with the same length, ticker
    * and the start time reset to the current time.
    */
  def reset: Timeout = this.copy(length, ticker)

  /**
    * Returns true if the timeout has elapsed or false otherwise.
    */
  def hasElapsed: Boolean = ticker.read - start >= length

  /**
    * Returns the time elapsed since the timeout was started.
    */
  def elapsed: Duration = ticker.read - start

}

object Timeout {

  /**
    * Default ticker that uses `System.currentTimeMillis`
    */
  val DefaultTicker = new Ticker {
    def read: Duration = System.currentTimeMillis.millis
  }

}

trait Ticker {
  def read: Duration
}
