package uscala.util

import scala.annotation.tailrec
import scala.concurrent.duration._
import scala.util.{Random, Failure, Success, Try}

object Retry {

  type Backoff = (Int, Duration) => Duration

  val DefaultInterval = 0.5.seconds

  @inline
  private def doNothing(t: Throwable) =  ()

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
    * @param f function to be executed
    */
  def retry[T](maxRetries: Option[Int],
               interval: Duration = DefaultInterval,
               backoff: Backoff = exponentialBackoff,
               failAction: Throwable => Unit = doNothing)
              (f: => Try[T]): Try[T] = {
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
  def forever[T](interval: Duration = DefaultInterval,
                 backoff: Backoff = exponentialBackoff,
                 failAction: Throwable => Unit = doNothing)
                (f: => Try[T]): T =
    retry(None, interval, backoff, failAction)(f).get

  /**
    * Convenience method that calls `retry` wrapping the
    * `maxRetries` parameter into a `Some`.
    */
  def until[T](maxRetries: Int,
               interval: Duration = DefaultInterval,
               backoff: Backoff = exponentialBackoff,
               failAction: Throwable => Unit = doNothing)
              (f: => Try[T]): Try[T] =
    retry(Some(maxRetries), interval, backoff, failAction)(f)

  @inline
  def exponentialBackoff(retry: Int, interval: Duration): Duration =
    interval * Math.pow(1.5, retry) * (Random.nextDouble() + 0.5)

  @inline
  private def sleep(time: Duration) = Thread.sleep(time.toMillis)

}