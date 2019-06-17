package uscala.result

import java.util.concurrent.atomic.{AtomicBoolean, AtomicInteger}

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import uscala.result.Result._

import scala.util.{Failure, Success}

// Shim for Scala 2.12/2.13 compatibility.
import uscala.result.MapViewShim._

class ResultSpec extends Specification with ScalaCheck {

  private def f(n: Int) = n + 1
  private def fa(n: Int) = f(n)
  private def fb(n: Int) = n + 2
  val fail: Result[String, Int] = Fail("fail")

  "fold" >> {
    "should apply fa if the result is Fail" >> prop { n: Int =>
      Fail(n).fold(fa, fb) must_=== fa(n)
    }
    "should apply fb if the result is Ok" >> prop { n: Int =>
      Ok(n).fold(fa, fb) must_=== fb(n)
    }
  }

  "map" >> {
    "should not apply f if the result is Fail" >> prop { n: Int =>
      Fail(n).map(f) must_=== Fail(n)
    }
    "should apply f if the result is Ok" >> prop { n: Int =>
      Ok(n).map(f) must_=== Ok(f(n))
    }
  }

  "leftMap" >> {
    "should apply f if the result is Fail" >> prop { n: Int =>
      Fail(n).leftMap(f) must_=== Fail(f(n))
    }
    "should not apply f if the result is Ok" >> prop { n: Int =>
      Ok(n).leftMap(f) must_=== Ok(n)
    }
  }

  "mapOk" >> {
    "should be an alias of map" >> prop { n: Int =>
      Ok(n).mapOk(f) must_=== Ok(n).map(f)
      Fail(n).mapOk(f) must_=== Fail(n).map(f)
    }
  }

  "mapFail" >> {
    "should be an alias of leftMap" >> prop { n: Int =>
      Ok(n).mapFail(f) must_=== Ok(n).leftMap(f)
      Fail(n).mapFail(f) must_=== Fail(n).leftMap(f)
    }
  }

  "flatMap" >> {
    def fa(a: Int): Result[Int, Int] = if (a % 2 == 0) Ok(a) else Fail(a)
    def fb(a: Int): Result[Int, Int] = if (a % 3 == 0) Ok(a) else Fail(a)
    def fc(a: Int): Result[Int, Int] = if (a % 5 == 0) Ok(a) else Fail(a)
    "should not apply fa if the result is Fail" >> prop { n: Int =>
      Fail(n).flatMap(fa) must_=== Fail(n)
    }
    "should apply fa if the result is Ok" >> prop { n: Int =>
      Ok(n).flatMap(fa) must_=== fa(n)
                                                 }
    "should be associative" >> prop { n: Int =>
      fa(n).flatMap(fb).flatMap(fc) must_=== fa(n).flatMap(x => fb(x).flatMap(fc))
    }
  }

  "bimap" >> {
    "should apply fa if the result is Fail" >> prop { n: Int =>
      Fail(n).bimap(fa, fb) must_=== Fail(fa(n))
    }
    "should apply fb if the result is Ok" >> prop { n: Int =>
      Ok(n).bimap(fa, fb) must_=== Ok(fb(n))
    }
  }

  "filter" >> {
    "should not apply the predicate if the result is already Fail" >> {
      fail.filter(_ => true, "predicate fail") must_=== Fail("fail")
    }
    "should return Fail if the predicate returns false" >> prop { n: Int =>
      Ok(n).filter(_ != n, "predicate fail") must_=== Fail("predicate fail")
    }
    "should return Ok if the predicate returns true" >> prop { n: Int =>
      Ok(n).filter(_ == n, "predicate fail") must_=== Ok(n)
    }
  }

  "filterNot" >> {
    "should not apply the predicate if the result is already Fail" >> {
      fail.filterNot(_ => true, "predicate fail") must_=== Fail("fail")
    }
    "should return Fail if the predicate returns true" >> prop { n: Int =>
      Ok(n).filterNot(_ == n, "predicate fail") must_=== Fail("predicate fail")
    }
    "should return Ok if the predicate returns false" >> prop { n: Int =>
      Ok(n).filterNot(_ != n, "predicate fail") must_=== Ok(n)
    }
  }

  "tap" >> {
    "should not execute the given f if Fail" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      val fail: Result[Int, Int] = Fail(n)
      fail.tap(_ => executed.set(true))
      executed.get must beFalse
    }

    "should execute the given f if Ok, passing the Ok value" >> prop { n: Int =>
      val received = new AtomicInteger(n)
      Ok(n).tap(x => received.set(x+1))
      received.get must_=== (n+1)
    }

    "should execute the given f if Ok, leaving the result untouched" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      Ok(n).tap(_ => executed.set(true)) must_=== Ok(n)
      executed.get must beTrue
    }
  }

  "tapOk" >> {
    "should be an alias of tap" >> prop { n: Int =>
      val tapExecuted = new AtomicInteger(0)
      def tap(x: Int): Unit = { tapExecuted.addAndGet(x); () }
      val tapOkExecuted = new AtomicInteger(0)
      def tapOk(x: Int): Unit = { tapOkExecuted.addAndGet(x); () }
      Ok(n).tapOk(tapOk) must_=== Ok(n).tap(tap)
      Fail(n).tapOk(tapOk) must_=== Fail(n).tap(tap)
      tapOkExecuted.get must_=== tapExecuted.get
    }
  }

  "tapFail" >> {
    "should not execute the given f if ok" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      val ok: Result[Int, Int] = Ok(n)
      ok.tapFail(_ => executed.set(true))
      executed.get must beFalse
    }

    "should execute the given f if Fail, passing the Fail value" >> prop { n: Int =>
      val received = new AtomicInteger(n)
      Fail(n).tapFail(x => received.set(x+1))
      received.get must_=== n+1
    }

    "should execute the given f if Fail, leaving the result untouched" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      Fail(n).tapFail(_ => executed.set(true)) must_=== Fail(n)
      executed.get must beTrue
    }
  }

  "bitap" >> {
    "should execute the given effect if Fail, leaving the result untouched" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      Fail(n).bitap { executed.set(true) } must_=== Fail(n)
      executed.get must beTrue
    }

    "should execute the given effect if Ok, leaving the result untouched" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      Ok(n).bitap { executed.set(true) } must_=== Ok(n)
      executed.get must beTrue
    }
  }

  "swap" >> {
    "should move Fail value to Ok" >> prop { n: Int =>
      Fail(n).swap must_=== Ok(n)
    }
    "should move Ok value to Fail" >> prop { n: Int =>
      Ok(n).swap must_=== Fail(n)
    }
  }

  "merge" >> {
    "should return the value of Fail if it's a Fail" >> prop { n: Int =>
      Fail(n).merge must_=== n
    }
    "should return the value of Ok if it's an Ok" >> prop { n: Int =>
      Ok(n).merge must_=== n
    }
  }

  "foreach" >> {
    "should not apply f if it's a Fail" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      val fail: Result[Int, Int] = Fail(n)
      fail.foreach(_ => executed.set(true))
      executed.get must beFalse
    }
    "should apply f if it's an Ok" >> prop { n: Int =>
      val executed = new AtomicBoolean(false)
      Ok(n).foreach(_ => executed.set(true))
      executed.get must beTrue
    }
  }

  "getOrElse" >> {
    "should return the default if it's a Fail" >> prop { (n1: Int, n2: Int) =>
      Fail(n1).getOrElse(n2) must_=== n2
    }
    "should return the ok value it's an Ok" >> prop { (n1: Int, n2: Int) =>
      Ok(n1).getOrElse(n2) must_=== n1
    }
  }

  "orElse" >> {
    "should return the fallback result if it's a Fail" >> prop { (n1: Int, n2: Int) =>
      Fail(n1).orElse(Ok(n2)) must_=== Ok(n2)
    }
    "should return the ok value it's an Ok" >> prop { (n1: Int, n2: Int) =>
      Ok(n1).orElse(Ok(n2)) must_=== Ok(n1)
    }
  }

  "recover" >> {
    "should recover if it's a Fail and the function is defined for the value" >> prop { n: Int =>
      val recovered = Fail(n).recover {
        case i if i % 2 == 0 => i
      }
      if (n % 2 == 0) recovered must_=== Ok(n)
      else recovered must_=== Fail(n)
    }
    "should ignore it if it's an Ok" >> prop { n: Int =>
      Result.attempt(n).recover {
       case _ => 1
      } must_=== Ok(n)
    }
  }

  "recoverWith" >> {
    "should recover if it's a Fail and the function is defined for the value" >> prop { n: Int =>
      val recovered = Fail(n).recoverWith {
        case i if i % 2 == 0 => Ok(i)
        case i if i % 3 == 0 => Fail(i)
      }
      if (n % 2 == 0) recovered must_=== Ok(n)
      else if (n % 3 == 0) recovered must_=== Fail(n)
      else recovered must_=== Fail(n)
    }
    "should ignore it if it's an Ok" >> prop { n: Int =>
      Result.attempt(n).recover {
       case _ => 1
      } must_=== Ok(n)
    }
  }

  "flatten" >> {
    "should transform a Ok(Ok(x)) into a Ok(x)" >> prop { n: Int =>
      Ok(Ok(n)).flatten.toEither must beRight(n)
    }
    "should transform a Ok(Fail(x)) into a Fail(x)" >> prop { n: Int =>
      Ok(Fail(n)).flatten.toEither must beLeft(n)
    }
    "should transform a Fail(x) into a Fail(x)" >> prop { n: Int =>
      Fail(n).flatten.toEither must beLeft(n)
    }
  }

  "toEither" >> {
    "should put the Fail value on the left" >> prop { n: Int =>
      Fail(n).toEither must beLeft(n)
    }
    "should put the Ok value on the right" >> prop { n: Int =>
      Ok(n).toEither must beRight(n)
    }
  }

  "toOption" >> {
    "should return None if it's a Fail" >> prop { n: Int =>
      Fail(n).toOption must beNone
    }
    "should return Some(value) if it's an Ok" >> prop { n: Int =>
      Ok(n).toOption must beSome(n)
    }
  }

  "toList" >> {
    "should return Nil if it's a Fail" >> prop { n: Int =>
      Fail(n).toList must beEmpty
    }
    "should return List(value) if it's an Ok" >> prop { n: Int =>
      Ok(n).toList must_=== List(n)
    }
  }

  "toTry" >> {
    "should return Failure[B] if it's a Fail[A <:< Exception, B]" >> prop { e: Throwable =>
      Fail(e).toTry must beAFailedTry(e)
    }
    "should return Success[B] if it's n Ok[A <:< Exception, B]" >> prop { n: Int =>
      Ok(n).toTry must beASuccessfulTry(n)
    }
  }

  "isOk" >> {
    "should return true for an Ok Result" >> {
      Ok(1).isOk must_=== true
    }
    "should return false for a Fail Result" >> {
      Fail(1).isOk must_=== false
    }
  }

  "isFail" >> {
    "should return false for an Ok Result" >> {
      Ok(1).isFail must_=== false
    }
    "should return truefor a Fail Result" >> {
      Fail(1).isFail must_=== true
    }
  }

  "toResult" >> {
    "for Option" >> {
      "should create a Fail if it's None" >> prop { n: Int =>
        Option.empty[Int].toResult(n) must_=== Fail(n)
      }
      "should create an Ok if it's Some" >> prop { n: Int =>
        Option(n).toResult(1) must_=== Ok(n)
      }
    }

    "for Either" >> {
      "should put the Left value on the Fail" >> prop { n: Int =>
        Left(n).toResult must_=== Fail(n)
      }
      "should put the Right value on the Ok" >> prop { n: Int =>
        Right(n).toResult must_=== Ok(n)
      }
    }

    "for Try" >> {
      "should create a Fail if it's Failure" >> prop { e: Exception =>
        Failure(e).toResult must_=== Fail(e)
      }
      "should create an Ok if it's Success" >> prop { n: Int =>
        Success(n).toResult must_=== Ok(n)
      }
    }
  }

  "sequence for results of options" >> {
    "should transform a Fail into a Some(Fail)" >> prop { x: Int =>
      Result.fail[Int, Option[Int]](x).sequence must beSome(Result.fail[Int, Int](x))
    }
    "should transform a Ok(None) into a None" >> {
      Ok(None).sequence must beNone
    }
    "should transform Ok(Some) into an Some(Ok)" >> prop { x: Int =>
      Result.ok[Int, Option[Int]](Some(x)).sequence must beSome(Result.ok[Int, Int](x))
    }
  }

  "sequence for options of results" >> {
    "should transform a Some(Fail) into a Fail" >> prop { x: Int =>
      Some(Fail(x)).sequence must_=== Fail(x)
    }
    "should transform a Some(Ok) into an Ok(Some)" >> prop { x: Int =>
      Some(Ok(x)).sequence must_=== Ok(Some(x))
    }
    "should transform None into an Ok(None)" >> {
      Option.empty[Result[Int, String]].sequence must_=== Ok(None)
    }
  }

  "ops on traversables" >> {
    "split" >> {
      "should split a Seq[Result[E, A]] into a (Seq[E], Seq[A]) " >> prop { xs: Seq[Int] => xs.nonEmpty ==> {
        val (fails, oks) = xs.map(x => if (x < 0) Fail(x) else Ok(x)).split
        fails must contain(be_<(0)).foreach
        oks must contain(be_>=(0)).foreach
      }}
      "should split an empty Seq into a (Seq.empty, Seq.empty)" >> {
        val (fails, oks) = Seq.empty[Result[Int, String]].split
        fails must_=== Seq.empty[Int]
        oks must_=== Seq.empty[String]
      }
    }
    "sequence" >> {
      "should transform a Seq(Fail) into a Fail" >> prop { xs: Seq[Int] => xs.nonEmpty ==>
        (xs.map(Result.fail[Int, Int]).sequence must_=== Fail(xs.head))
      }
      "should transform a Seq(Ok) into an Ok(Seq)" >> prop { xs: Seq[Int] => xs.nonEmpty ==>
        (xs.map(Result.ok[Int, Int]).sequence must_=== Ok(xs))
      }
      "should transform an empty Seq into an Ok(Seq.empty)" >> {
        Seq.empty[Result[Int, String]].sequence must_=== Ok(Seq.empty[String])
      }
    }
  }

  "sequence for maps" >> {
    "should transform a Map(K -> Fail) into a Fail" >> prop { xs: Map[Int, Int] => xs.nonEmpty ==>
      (xs.mapValuesShim(Result.fail[Int, Int]).toMap.sequence must_=== Fail(xs.values.head))
    }
    "should transform a Map(K -> Ok(V) into an Ok(Map(K -> V))" >> prop { xs: Map[Int, Int] => xs.nonEmpty ==>
      (xs.mapValuesShim(Result.ok[Int, Int]).toMap.sequence must_=== Ok(xs))
    }
    "should transform an empty Map into an Ok(Map.empty)" >> {
      Map.empty[Int, Result[Int, Int]].sequence must_=== Ok(Map.empty[Int, Int])
    }
  }

  "boolean filters" >> {
    "orIfFalse" >> {
      "should leave an Ok(true) value alone" >> {
        Ok(true).orIfFalse("predicate fail") must_== Ok(true)
      }

      "should transform an Ok(false) value to a Fail" >> {
        Ok(false).orIfFalse("predicate fail") must_== Fail("predicate fail")
      }
    }

    "orIfTrue" >> {
      "should leave an Ok(false) value alone" >> {
        Ok(false).orIfTrue("predicate fail") must_== Ok(false)
      }

      "should transform an Ok(true) value to a Fail" >> {
        Ok(true).orIfTrue("predicate fail") must_== Fail("predicate fail")
      }
    }
  }

  "ResultFunctions" >> {
    "fail" >> {
      "should create a Fail" >> prop { n: Int =>
        Result.fail(n) must_=== Fail(n)
      }
    }

    "ok" >> {
      "should create an Ok" >> prop { n: Int =>
        Result.ok(n) must_=== Ok(n)
      }
    }

    "fromEither" >> {
      "should put the Left value on the Fail" >> prop { n: Int =>
        Result.fromEither(Left(n)) must_=== Fail(n)
      }
      "should put the Right value on the Ok" >> prop { n: Int =>
        Result.fromEither(Right(n)) must_=== Ok(n)
      }
    }

    "fromOption" >> {
      "should create a Fail if it's None" >> {
        Result.fromOption(None, 1) must_=== Fail(1)
      }
      "should create an Ok if it's Some" >> prop { n: Int =>
        Result.fromOption(Some(n), 1) must_=== Ok(n)
      }
    }

    "fromTry" >> {
      "should create a Fail if it's Failure" >> prop { e: Exception =>
        Result.fromTry(Failure(e)) must_=== Fail(e)
      }
      "should create an Ok if it's Success" >> prop { n: Int =>
        Result.fromTry(Success(n)) must_=== Ok(n)
      }
    }

    "attempt" >> {
      "should catch any NonFatal exception and return it as a Fail" >> prop { e: Exception =>
        def fails() = throw e
        Result.attempt(fails()) must_=== Fail(e)
      }
      "should not catch a Fatal exception" >> {
        def fails() = throw new StackOverflowError
        Result.attempt(fails()) must throwA[StackOverflowError]
      }
      "should wrap the result in an Ok no exception is thrown" >> prop { n: Int =>
        Result.attempt(f(n)) must_=== Ok(f(n))
      }
    }
  }
}
