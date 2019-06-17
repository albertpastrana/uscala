package uscala.concurrent.result

import java.util.concurrent.TimeoutException

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.concurrent.ExecutionEnv
import org.specs2.mutable.Specification
import uscala.concurrent.result.AsyncResult.{attemptFuture, attemptSync, fromFuture, fromResult, fail => asyncFail, ok => asyncOk}
import uscala.result.Result
import uscala.result.Result.{Fail, Ok}
import uscala.result.specs2.ResultMatchers

import scala.concurrent.Future
import scala.concurrent.duration._

class AsyncResultSpec(implicit ee: ExecutionEnv) extends Specification with ScalaCheck with ResultMatchers {

  def resultGen: Gen[Result[Int, Int]] = Gen.posNum[Int].flatMap(i => Gen.oneOf(Ok[Int](i), Fail[Int](i)))

  implicit def arbResult: Arbitrary[Result[Int, Int]] = Arbitrary(resultGen)

  def f(n: Int) = n + 1
  def fa(n: Int) = f(n)
  def fb(n: Int) = n + 2
  def longOp = Future(Thread.sleep(1.second.toMillis))

  "fold" >> {
    "should apply fa if the result is Fail" >> prop { n: Int =>
      asyncFail(n).fold(fa, fb) must be_===(fa(n)).await
    }
    "should apply fb if the result is Ok" >> prop { n: Int =>
      asyncOk(n).fold(fa, fb) must be_===(fb(n)).await
    }
  }

  "map" >> {
    "should not apply f if the result is Fail" >> prop { n: Int =>
      asyncFail(n).map(f).underlying must beFail(n).await
    }
    "should apply f if the result is Ok" >> prop { n: Int =>
      asyncOk(n).map(f).underlying must beOk(f(n)).await
    }
  }

  "leftMap" >> {
    "should apply f if the result is Fail" >> prop { n: Int =>
      asyncFail(n).leftMap(f).underlying must beFail(f(n)).await
    }
    "should not apply f if the result is Ok" >> prop { n: Int =>
      asyncOk(n).leftMap(f).underlying must beOk(n).await
    }
  }

  "mapOk" >> {
    "should be an alias of map" >> prop { n: Int =>
      asyncOk(n).mapOk(f).underlying must be_===(Ok(n).map(f)).await
      asyncFail(n).mapOk(f).underlying must be_===(Fail(n).map(f)).await
    }
  }

  "mapFail" >> {
    "should be an alias of leftMap" >> prop { n: Int =>
      asyncOk(n).mapFail(f).underlying must be_===(Ok(n).leftMap(f)).await
      asyncFail(n).mapFail(f).underlying must be_===(Fail(n).leftMap(f)).await
    }
  }

  "flatMap" >> {
    def fa(a: Int): AsyncResult[Int, Int] = if (a % 2 == 0) asyncOk(a) else asyncFail(a)
    def fb(a: Int): AsyncResult[Int, Int] = if (a % 3 == 0) asyncOk(a) else asyncFail(a)
    def fc(a: Int): AsyncResult[Int, Int] = if (a % 5 == 0) asyncOk(a) else asyncFail(a)
    "should not apply fa if the result is Fail" >> prop { n: Int =>
      asyncFail(n).flatMap(fa).underlying must beFail(n).await
    }
    "should apply fa if the result is Ok" >> prop { n: Int =>
      asyncOk(n).flatMap(fa).underlying.flatMap(res =>
        fa(n).underlying.map(res === _)
      ).await
    }
    "should be associative" >> prop { n: Int =>
      fa(n).flatMap(fb).flatMap(fc).underlying.flatMap(res =>
        fa(n).flatMap(x => fb(x).flatMap(fc)).underlying.map(res === _)
      ).await
    }
  }

  "flatMapR" >> {
    def fa(a: Int): Result[Int, Int] = if (a % 2 == 0) Ok(a) else Fail(a)
    def fb(a: Int): Result[Int, Int] = if (a % 3 == 0) Ok(a) else Fail(a)
    def fc(a: Int): Result[Int, Int] = if (a % 5 == 0) Ok(a) else Fail(a)
    "should not apply fa if the result is Fail" >> prop { n: Int =>
      asyncFail(n).flatMapR(fa).underlying must beFail(n).await
    }
    "should apply fa if the result is Ok" >> prop { n: Int =>
      asyncOk(n).flatMapR(fa).underlying must be_===(fa(n)).await
    }
    "should be associative" >> prop { n: Int =>
      fromResult(fa(n)).flatMapR(fb).flatMapR(fc).underlying must be_===(fa(n).flatMap(x => fb(x).flatMap(fc))).await
    }
  }

  "flatMapF" >> {
    def fa(a: Int): Future[Int] = Future(a + 1)
    def fb(a: Int): Future[Int] = Future(a + 2)
    def fc(a: Int): Future[Int] = Future(a + 3)
    "should not apply fa if the result is Fail" >> prop { n: Int =>
      asyncFail(n).flatMapF(fa).underlying must beFail(n).await
    }
    "should apply fa if the result is Ok" >> prop { n: Int =>
      asyncOk(n).flatMapF(fa).underlying.flatMap(res =>
        fa(n).map(res === Ok(_))
      ).await
    }
    "should be associative" >> prop { n: Int =>
      fromFuture(fa(n)).flatMapF(fb).flatMapF(fc).underlying.flatMap(res =>
        fa(n).flatMap(x => fb(x).flatMap(fc)).map(res === Ok(_))
      ).await
    }
  }

  "bimap" >> {
    "should apply fa if the result is Fail" >> prop { n: Int =>
      asyncFail(n).bimap(fa, fb).underlying must beFail(fa(n)).await
    }
    "should apply fb if the result is Ok" >> prop { n: Int =>
      asyncOk(n).bimap(fa, fb).underlying must beOk(fb(n)).await
    }
  }

  "merge" >> {
    "should return the value of Fail if it's a Fail" >> prop { n: Int =>
      asyncFail(n).merge must be_===(n).await
    }
    "should return the value of Ok if it's an Ok" >> prop { n: Int =>
      asyncOk(n).merge must be_===(n).await
    }
  }

  "tap" >> {
    "should not execute the given f if Fail and leave the left untouched" >> prop { n: Int =>
      var executed = false
      val fail: AsyncResult[Int, Int] = asyncFail(n)
      fail.tap(_ => executed = true).underlying must beFail(n).await
      executed must beFalse
    }

    "should execute the given f if Ok, passing the Ok value and leaving the result untouched" >> prop { n: Int =>
      var received: Option[Int] = None
      asyncOk(n).tap(x => received = Some(x)).underlying must beOk(n).await
      received must beSome(n)
    }
  }

  "tapOk" >> {
    "should be an alias of tap" >> prop { n: Int =>
      var tapExecuted = 0
      def tap(x: Int): Unit = tapExecuted += x
      var tapOkExecuted = 0
      def tapOk(x: Int): Unit = tapOkExecuted += x
      asyncOk(n).tapOk(tapOk).flatMap(x => asyncOk(n).tap(tap).map(_ == x)).underlying must beOk(beTrue).await
      asyncFail(n).tapOk(tapOk).underlying must beFail.which { x: Int =>
        asyncFail(n).tap(tap).underlying must beFail(be_===(x)).await
      }.await
      tapOkExecuted must_=== tapExecuted
    }
  }

  "tapFail" >> {
    "should not execute the given f if ok" >> prop { n: Int =>
      var executed = false
      val ok: AsyncResult[Int, Int] = asyncOk(n)
      ok.tapFail(_ => executed = true).underlying must beOk(n).await
      executed must beFalse
    }

    "should execute the given f if Fail, passing the Fail value and leaving the result untouched" >> prop { n: Int =>
      var received: Option[Int] = None
      asyncFail(n).tapFail(x => received = Some(x)).underlying must beFail(n).await
      received must beSome(n)
    }
  }

  "bitap" >> {
    "should execute the given effect if Fail, leaving the result untouched" >> prop { n: Int =>
      var executed = false
      asyncFail(n).bitap { executed = true }.underlying must beFail(n).await
      executed must beTrue
    }

    "should execute the given effect if Ok, leaving the result untouched" >> prop { n: Int =>
      var executed = false
      asyncOk(n).bitap { executed = true }.underlying must beOk(n).await
      executed must beTrue
    }
  }

  "flatten" >> {
    "should transform a Ok(Ok(x)) into a Ok(x)" >> prop { n: Int =>
      asyncOk(asyncOk(n)).flatten.underlying must beOk(n).await
    }

    "should transform a Ok(Fail(x)) into a Fail(x)" >> prop { n: Int =>
      asyncOk(asyncFail(n)).flatten.underlying must beFail(n).await
    }

    "should transform a Fail(x) into a Fail(x)" >> prop { n: Int =>
      asyncFail(n).flatten.underlying must beFail(n).await
    }
  }

  "swap" >> {
    "should move Fail value to Ok" >> prop { n: Int =>
      asyncFail(n).swap.underlying must beOk(n).await
    }
    "should move Ok value to Fail" >> prop { n: Int =>
      asyncOk(n).swap.underlying must beFail(n).await
    }
  }

  "attemptRun" >> {
    "Returns a result after successfully evaluating the future" >> prop { r: Result[Int, Int] =>
      fromResult(r).attemptRun must_=== Ok(r)
    }
    "Returns a failed result with an exception on the left after failing to evaluate the future" >> prop { s: String =>
      val e = new RuntimeException(s)
      fromFuture(Future.failed(e)).attemptRun must_=== Fail(e)
    }
    "Maps an exception from a failed future into the type of the left hand side of the result" >> prop { s: String =>
      fromFuture(Future.failed(new RuntimeException(s))).attemptRun(_.getMessage) must_=== Fail(s)
    }
  }

  "attemptRunFor(Duration)" >> {
    "Returns a result after successfully evaluating the future" >> prop { r: Result[Int, Int] =>
      fromResult(r).attemptRunFor(1.millis) must_=== Ok(r)
    }

    "Fails with an exception on the left of a result when execution times out" >> {
      fromFuture(longOp).attemptRunFor(1.millis) must beFail[Throwable].like {
        // Scala < 2.13 the message is "Futures timed out" (note plural)
        // Scala >= 2.13 the message is "Future timed out"
        case a => a.getMessage must endWith("timed out after [1 millisecond]")
      }
    }
  }

  "attemptRunFor(Throwable => AA, Duration)" >> {
    "Returns a result after successfully evaluating the future" >> prop { r: Result[Int, Int] =>
      fromResult(r).attemptRunFor(_ => -1, 1.millis) must_=== r
    }
    "Returns a failed result of the underlying result type when execution times out" >> {
      fromFuture(longOp).attemptRunFor(_.getMessage, 1.millis) must beFail[String].like {
        // Scala < 2.13 the message is "Futures timed out" (note plural)
        // Scala >= 2.13 the message is "Future timed out"
        case a => a must endWith("timed out after [1 millisecond]")
      }
    }
  }

  "StaticFunctions" >> {
    "fromResult" >> {
      "should create an AsyncResult of the same underlying types" >> prop { r: Result[Int, Int] =>
        fromResult(r).underlying must be_===(r).await
      }
    }

    "fromFuture" >> {
      "should create an Ok with the underlying future type" >> prop { n: Int =>
        fromFuture(Future(n)).underlying must beOk(n).await
      }
    }

    "fail" >> {
      "should create a Fail" >> prop { n: Int =>
        asyncFail(n).underlying must beFail(n).await
      }
    }

    "ok" >> {
      "should create an Ok" >> prop { n: Int =>
        asyncOk(n).underlying must beOk(n).await
      }
    }

    "attemptSync" >> {
      "should catch any NonFatal exception and return it as a Fail" >> prop { e: Exception =>
        def fails() = throw e
        attemptSync(fails()).underlying must beFail[Throwable](e).await
      }
      "should not catch a Fatal exception" >> {
        def fails() = throw new StackOverflowError("this is a test")
        attemptSync(fails()) must throwA[StackOverflowError]
      }
      "should wrap the result in an Ok no exception is thrown" >> prop { n: Int =>
        attemptSync(f(n)).underlying must beOk(f(n)).await
      }
    }

    "attempt" >> {
      "should catch any NonFatal exception and return it as a Fail" >> prop { e: Exception =>
        def fails = throw e
        AsyncResult.attempt(fails).underlying must beFail[Throwable](e).await
      }
      "should not catch a Fatal exception" >> {
        def fails = throw new OutOfMemoryError("this is a test")

        AsyncResult.attempt(fails).attemptRunFor(1.second) must beFail(beAnInstanceOf[TimeoutException])
      }
      "should wrap the result in an Ok no exception is thrown" >> prop { n: Int =>
        AsyncResult.attempt(f(n)).underlying must beOk(f(n)).await
      }
    }

    "attemptFuture" >> {
      "should wrap the exception of a failed future in te result" >> prop { e: Exception =>
        attemptFuture(Future.failed(e)).underlying must beFail[Throwable](e).await
      }
      "should not catch a Fatal exception" >> {
        def willFail = Future(throw new OutOfMemoryError("this is a test"))

        attemptFuture(willFail).attemptRunFor(100.millis) must beFail(beAnInstanceOf[TimeoutException])
      }
      "should wrap the result in an Ok no exception is thrown" >> prop { n: Int =>
        attemptFuture(Future.successful(n)).underlying must beOk(n).await
      }
    }
  }

  "ResultOps functions" >> {
    import AsyncResult.ResultOps
    "async" >> {
      "should transform a failed result into an async result" >> prop { n: Int =>
        Result.fail[Int, Int](n).async.underlying.flatMap(res =>
          asyncFail(n).underlying.map(_ === res)
        ).await
      }
      "should transform a failed result into an async result" >> prop { n: Int =>
        Result.ok[Int, Int](n).async.underlying.flatMap(res =>
          asyncOk(n).underlying.map(_ === res)
        ).await
      }
    }
    "sequence" >> {
      "should transform a Seq(Fail) into a Fail(Seq)" >> prop { xs: Seq[Int] => xs.nonEmpty ==>
        (xs.map(asyncFail[Int, Int]).sequence.underlying must beFail(xs.head).await)
      }
      "should transform a Seq(Ok) into an Ok(Seq)" >> prop { xs: Seq[Int] => xs.nonEmpty ==>
        (xs.map(asyncOk[Int, Int]).sequence.underlying must beOk(xs).await)
      }
      "should transform an empty Seq into an Ok(Seq.empty)" >> {
        Seq.empty[AsyncResult[Int, String]].sequence.underlying must beOk(Seq.empty[String]).await
      }
    }
  }

  "FutureOps functions" >> {
    import AsyncResult.FutureOps
    "toFuture" >> {
      "should transform a successful future into an ok async result" >> prop { n: Int =>
        Future.successful(n).toResult.underlying must beOk(n).await
      }
      "should transform a failed future into a failed async result with the exception" >> prop { e: Exception =>
        Future.failed(e).toResult.underlying must beFail[Throwable](e).await
      }
    }
    "toFuture(recover)" >> {
      val recover = (tr: Throwable) => tr.getMessage

      "should transform a successful future into an ok async result" >> prop { n: Int =>
        Future.successful(n).toResult(recover).underlying must beOk(n).await
      }
      "should transform a failed future into a failed async result with the result of the recover function" >> prop { e: Exception =>
        Future.failed(e).toResult(recover).underlying must beFail(e.getMessage).await
      }
    }
  }
}
