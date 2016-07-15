package uscala.util

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.{Random, Failure, Success, Try}

object Retry {

  type Backoff = (Int, Duration) => Duration

  val DefaultWait = 2.millis

  private val DoNothing = (_: Throwable) => ()

  /**
    * This function tries to synchronously execute a function forever,
    * after a maximum number of retries or until the function returns
    * a successful computation (`Success`).
    *
    * Be extremely careful as it's blocking and can potentially
    * (and easily) be an infinite loop.
    *
    * By default uses the exponential backoff strategy.
    *
    * @param f function to be executed
    * @param maxRetries if specified, indicates the maximum number of
    *                   times it will try to execute the function,
    *                   otherwise it will never end until there is
    *                   a successful computation
    * @param interval retry interval (will seed the backoff strategy),
    *                 default is 2.millis
    * @param backoff backoff strategy, default is exponential
    * @param failAction method to execute if the main function fails,
    *                   useful if you want to log something.
    *                   Default is `()`.
    */
  def retry[T](f: => Try[T],
               maxRetries: Option[Int],
               interval: Duration = 0.5.seconds,
               backoff: Backoff = exponentialBackoff,
               failAction: Throwable => Unit = DoNothing): Try[T] = {
    @tailrec
    def loop(retry: Int): Try[T] = f match {
      case s @ Success(v) => s
      case Failure(e) if maxRetries.forall(_ < retry) =>
        failAction(e)
        sleep(backoff(retry, interval))
        loop(retry)
      case fail => fail
    }
    loop(1)
  }

  /**
    * Convenience method that calls `retry` with no maximum
    * retries, effectively making it wait forever until the
    * function `f` returns a `Success`.
    */
  def forever[T](f: => Try[T],
                 interval: Duration = DefaultWait,
                 backoff: Backoff = exponentialBackoff,
                 failAction: Throwable => Unit = DoNothing): T =
    retry(f, None, interval, backoff, failAction).get

  /**
    * Convenience method that calls `retry` wrapping the
    * `maxRetries` parameter into a `Some`.
    */
  def until[T](f: => Try[T],
               maxRetries: Int,
               interval: Duration = 2.millis,
               backoff: Backoff = exponentialBackoff,
               failAction: Throwable => Unit = DoNothing): Try[T] =
    retry(f, Some(maxRetries), interval, backoff, failAction)

  @inline
  def exponentialBackoff(retry: Int, interval: Duration): Duration =
    interval * Math.pow(1.5, retry) * (Random.nextDouble() + 0.5)

  @inline
  private def sleep(time: Duration) = Thread.sleep(time.toMillis)

}