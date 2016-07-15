package uscala.util

import org.specs2.mutable.Specification

import scala.concurrent.duration._

class TimeoutSpec extends Specification {

  val timeoutLen = 100.seconds

  "when the timeout has not elapsed" >> {
    val ticker = new MutableTicker
    val timeout = Timeout(timeoutLen, ticker)
    ticker.time = 20.seconds
    "hasElapsed should return false" >> {
      timeout.hasElapsed must beFalse
    }
    "elapsed should return the time elapsed" >> {
      timeout.elapsed must_=== 20.seconds
    }
    "reset shouldn't have any effect" >> {
      val reset = timeout.reset
      reset.hasElapsed must beFalse
      reset.elapsed must_=== 0.seconds
    }
  }

  "when the timeout has elapsed" >> {
    val ticker = new MutableTicker
    val timeout = Timeout(timeoutLen, ticker)
    ticker.time = timeoutLen
    "hasElapsed should return true" >> {
      timeout.hasElapsed must beTrue
    }
    "elapsed should return the time elapsed" >> {
      timeout.elapsed must_=== timeoutLen
    }
    "reset should return non elapsed timeout" >> {
      val reset = timeout.reset
      reset.hasElapsed must beFalse
      reset.elapsed must_=== 0.seconds
    }
  }

  "should use the system time in millis by default" >> {
    val start = System.currentTimeMillis
    val timeout = new Timeout(timeoutLen)
    timeout.hasElapsed must beFalse
    timeout.elapsed.toMillis must be_<=(System.currentTimeMillis() - start)
  }

  class MutableTicker extends Ticker {
    var time = 0.seconds
    override def read = time
  }
}

