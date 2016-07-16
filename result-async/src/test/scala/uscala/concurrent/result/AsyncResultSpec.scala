package uscala.concurrent.result

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import uscala.result.Result
import uscala.result.Result.{Fail, Ok}
import uscala.result.specs2.ResultMatchers

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.{Duration, _}
import scala.concurrent.{Await, Future}

class AsyncResultSpec extends Specification with ScalaCheck with ResultMatchers {

  def resultGen: Gen[Result[Int, Int]] = for {
    i <- Gen.posNum[Int]
    res <- Gen.oneOf(Ok[Int](_), Fail[Int](_))
  } yield res(i)

  implicit def arbResult: Arbitrary[Result[Int, Int]] = Arbitrary(resultGen)

  def f(n: Int) = n + 1
  def fa(n: Int) = f(n)
  def fb(n: Int) = n + 2

  def attempt[A, B](a: AsyncResult[A, B]): Result[A, B] = attempt(a.underlying)
  def attempt[T](f: Future[T]): T = Await.result(f, Duration.Inf)

  "fold" >> {
    "should apply fa if the result is Fail" >> prop { n: Int =>
      attempt(AsyncResult.fail(n).fold(fa, fb)) must_=== fa(n)
    }
    "should apply fb if the result is Ok" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).fold(fa, fb)) must_=== fb(n)
    }
  }

  "map" >> {
    "should not apply f if the result is Fail" >> prop { n: Int =>
      def a(i: Int) = i.toString
      val b = AsyncResult.ok[Throwable, Int](1)
      val c = b.map(a)
      attempt(AsyncResult.fail(n).map(f)) must_=== Fail(n)
    }
    "should apply f if the result is Ok" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).map(f)) must_=== Ok(f(n))
    }
  }

  "leftMap" >> {
    "should apply f if the result is Fail" >> prop { n: Int =>
      attempt(AsyncResult.fail(n).leftMap(f)) must_=== Fail(f(n))
    }
    "should not apply f if the result is Ok" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).leftMap(f)) must_=== Ok(n)
    }
  }

  "mapOk" >> {
    "should be an alias of map" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).mapOk(f)) must_=== Ok(n).map(f)
      attempt(AsyncResult.fail(n).mapOk(f)) must_=== Fail(n).map(f)
    }
  }

  "mapFail" >> {
    "should be an alias of leftMap" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).mapFail(f)) must_=== Ok(n).leftMap(f)
      attempt(AsyncResult.fail(n).mapFail(f)) must_=== Fail(n).leftMap(f)
    }
  }

  "flatMap" >> {
    def fa(a: Int): AsyncResult[Int, Int] = if (a % 2 == 0) AsyncResult.ok(a) else AsyncResult.fail(a)
    def fb(a: Int): AsyncResult[Int, Int] = if (a % 3 == 0) AsyncResult.ok(a) else AsyncResult.fail(a)
    def fc(a: Int): AsyncResult[Int, Int] = if (a % 5 == 0) AsyncResult.ok(a) else AsyncResult.fail(a)
    "should not apply fa if the result is Fail" >> prop { n: Int =>
      attempt(AsyncResult.fail(n).flatMap(fa)) must_=== Fail(n)
    }
    "should apply fa if the result is Ok" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).flatMap(fa)) must_=== attempt(fa(n))
    }
    "should be associative" >> prop { n: Int =>
      attempt(fa(n).flatMap(fb).flatMap(fc)) must_=== attempt(fa(n).flatMap(x => fb(x).flatMap(fc)))
    }
  }

  "flatMapR" >> {
    def fa(a: Int): Result[Int, Int] = if (a % 2 == 0) Ok(a) else Fail(a)
    def fb(a: Int): Result[Int, Int] = if (a % 3 == 0) Ok(a) else Fail(a)
    def fc(a: Int): Result[Int, Int] = if (a % 5 == 0) Ok(a) else Fail(a)
    "should not apply fa if the result is Fail" >> prop { n: Int =>
      attempt(AsyncResult.fail(n).flatMapR(fa)) must_=== Fail(n)
    }
    "should apply fa if the result is Ok" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).flatMapR(fa)) must_=== fa(n)
    }
    "should be associative" >> prop { n: Int =>
      attempt(AsyncResult.fromResult(fa(n)).flatMapR(fb).flatMapR(fc)) must_=== fa(n).flatMap(x => fb(x).flatMap(fc))
    }
  }

  "flatMapF" >> {
    def fa(a: Int): Future[Int] = Future(a + 1)
    def fb(a: Int): Future[Int] = Future(a + 2)
    def fc(a: Int): Future[Int] = Future(a + 3)
    "should not apply fa if the result is Fail" >> prop { n: Int =>
      attempt(AsyncResult.fail(n).flatMapF(fa)) must_=== Fail(n)
    }
    "should apply fa if the result is Ok" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).flatMapF(fa)).getOrElse(n - 1) must_=== attempt(fa(n))
    }
    "should be associative" >> prop { n: Int =>
      attempt(AsyncResult.fromFuture(fa(n)).flatMapF(fb).flatMapF(fc)).getOrElse(n - 1) must_=== attempt(fa(n).flatMap(x => fb(x).flatMap(fc)))
    }
  }

  "bimap" >> {
    "should apply fa if the result is Fail" >> prop { n: Int =>
      attempt(AsyncResult.fail(n).bimap(fa, fb)) must_=== Fail(fa(n))
    }
    "should apply fb if the result is Ok" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).bimap(fa, fb)) must_=== Ok(fb(n))
    }
  }

  "swap" >> {
    "should move Fail value to Ok" >> prop { n: Int =>
      attempt(AsyncResult.fail(n).swap) must_=== Ok(n)
    }
    "should move Ok value to Fail" >> prop { n: Int =>
      attempt(AsyncResult.ok(n).swap) must_=== Fail(n)
    }
  }

  "attemptRun" >> {
    "Returns a result after successfully evaluating the future" >> prop { r: Result[Int, Int] =>
      AsyncResult.fromResult(r).attemptRun must_=== Ok(r)
    }
    "Returns a failed result with an exception on the left after failing to evaluate the future" >> prop { s: String =>
      val e = new RuntimeException(s)
      AsyncResult.fromFuture(Future.failed(e)).attemptRun must_=== Fail(e)
    }
    "Maps an exception from a failed future into the type of the left hand side of the result" >> prop { s: String =>
      AsyncResult.fromFuture(Future.failed(new RuntimeException(s))).attemptRun(_.getMessage) must_=== Fail(s)
    }
  }

  "attemptRunFor" >> {
    def longOp = Future(Thread.sleep(1.second.toMillis))
    "Returns a result after successfully evaluating the future" >> prop { r: Result[Int, Int] =>
      AsyncResult.fromResult(r).attemptRunFor(1.millis) must_=== Ok(r)
    }

    "Fails with an exception on the left of a result when execution times out" >> {
      AsyncResult.fromFuture(longOp).attemptRunFor(1.millis) must beFail[Throwable].like {
        case a => a.getMessage must_=== "Futures timed out after [1 millisecond]"
      }
    }
    "Returns a failed result of the underlying result type when execution times out" >> {
      AsyncResult.fromFuture(longOp).attemptRunFor(_.getMessage, 1.millis) must_=== Fail("Futures timed out after [1 millisecond]")
    }
  }

  "StaticFunctions" >> {
    "fromResult" >> {
      "should create an AsyncResult of the same underlying types" >> prop { r: Result[Int, Int] =>
        attempt(AsyncResult.fromResult(r)) must_=== r
      }
    }

    "fromFuture" >> {
      "should create an Ok with the underlying future type" >> prop { n: Int =>
        attempt(AsyncResult.fromFuture(Future(n))) must_=== Ok(n)
      }
    }

    "fail" >> {
      "should create a Fail" >> prop { n: Int =>
        attempt(AsyncResult.fail(n)) must_=== Fail(n)
      }
    }

    "ok" >> {
      "should create an Ok" >> prop { n: Int =>
        attempt(AsyncResult.ok(n)) must_=== Ok(n)
      }
    }

    "attempt" >> {
      "should catch any NonFatal exception and return it as a Fail" >> prop { e: Exception =>
        def fails() = throw e
        attempt(AsyncResult.attempt(fails())) must_=== Fail(e)
      }
      "should not catch a Fatal exception" >> {
        def fails() = throw new StackOverflowError
        AsyncResult.attempt(fails()) must throwA[StackOverflowError]
      }
      "should wrap the result in an Ok no exception is thrown" >> prop { n: Int =>
        attempt(AsyncResult.attempt(f(n))) must_=== Ok(f(n))
      }
    }
  }
}