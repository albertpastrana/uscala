package uscala.util

import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicBoolean

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.concurrent.ExecutionEnv
import org.specs2.matcher.FutureMatchers
import org.specs2.mutable.Specification

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success, Try}

class RetrySpec(implicit ee: ExecutionEnv) extends Specification with ScalaCheck with FutureMatchers {

  private val counter = Array(0, 0, 0, 0, 0)
  private def fail(index: Int): Try[Int] = {
    counter(index) += 1
    if (counter(index) < 3) Failure(new Exception())
    else Success(counter(index))
  }

  private def constant(retry: Int, interval: Duration) = interval

  //Exp -> retry #, min and max intervals
  type Exp = (Int, Duration, Duration)
  private val exponential = List(
    (1, 0.25.seconds, 0.75.seconds),
    (2, 0.375.seconds, 1.125.seconds),
    (3, 0.562.seconds, 1.687.seconds),
    (4, 0.8435.seconds, 2.53.seconds),
    (5, 1.265.seconds, 3.795.seconds),
    (6, 1.897.seconds, 5.692.seconds),
    (7, 2.846.seconds, 8.538.seconds),
    (8, 4.269.seconds, 12.807.seconds),
    (9, 6.403.seconds, 19.210.seconds),
    (10, 9.605.seconds, 28.815.seconds)
  )
  private def arbExpGenerator = Gen.oneOf(exponential)

  implicit def abExp: Arbitrary[Exp] = Arbitrary(arbExpGenerator)

  "retry" >> {
    "should retun the value of the computation if it's successful" >> {
      Retry.retry(None)(Success(1)) must beASuccessfulTry(1)
    }
    "should retry until the computation is successful" >> {
      Retry.retry(None)(fail(0)) must beASuccessfulTry(3)
    }
    "should use the exponential backoff strategy by default" >> {
      val interval = 50.millis
      val start = System.currentTimeMillis
      Retry.retry(None, interval)(fail(1))
      val elapsed = (System.currentTimeMillis - start).toInt

      val minWait = 25 + 37
      val maxWait = 75 + 112 + 150 //some extra time for the computation
      elapsed must be_>=(minWait)
      elapsed must be_<=(maxWait * ee.timeFactor)
    }
    "should use the specified backoff strategy" >> {
      val pause = 50.millis
      val start = System.currentTimeMillis
      Retry.retry(None, pause, constant)(fail(2))
      val elapsed = System.currentTimeMillis - start

      elapsed must be_>((pause * 2).toMillis)
      elapsed must be_<((pause * 3).toMillis * ee.timeFactor + 300) //adding some extra time for the computation
    }
    "should execute the specified failAction" >> {
      val errors = new ArrayBuffer[Throwable]
      def failAction(tr: Throwable): Unit = errors += tr
      Retry.retry(None, failAction = failAction)(fail(3))

      errors must haveLength(2)
    }
    "should retry only until the max retries has been reached" >> {
      Retry.retry(Some(1))(fail(4)) must beAFailedTry
    }
  }

  "forever" >> {
    "should never finish if the function is not successful" >> {
      val end = new AtomicBoolean(false)
      def err = if (end.get) Success(1) else Failure(new IllegalArgumentException)
      val result = Await.result(Future(Retry.forever[Int]()(err)), 1.second) must throwA[TimeoutException]
      end.set(true)
      result
    }
    "should unwrap the try and return the value if it's successful" >> {
      Retry.forever()(Success(1)) must_=== 1
    }
  }

  "until" >> {
    "should finish after max retries if the function is not successful" >> {
      val err = new IllegalArgumentException
      Retry.until[Int](2)(Failure(err)) must beAFailedTry(err)
    }
    "should return the successful try if the computation is successful" >> {
      Retry.until(2)(Success(1)) must beASuccessfulTry(1)
    }
  }

  "exponential backoff" >> prop { e: Exp => e match {
      case (retry, min, max) =>
        val res = Retry.exponentialBackoff(retry-1, 0.5.seconds)
        res must be_>=(min)
        res must be_<=(max)
    }
  }

}
