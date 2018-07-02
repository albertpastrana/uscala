package uscala.result

import uscala.result.Result.{Fail, Ok}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

sealed abstract class Result[+A, +B] extends Product with Serializable {

  def fold[C](fa: A => C, fb: B => C): C = this match {
    case Fail(a) => fa(a)
    case Ok(b) => fb(b)
  }

  def map[C](f: (B) => C): Result[A, C] = this match {
    case Ok(b) => Ok(f(b))
    case e @ Fail(_) => e
  }

  def leftMap[C](f: (A) => C): Result[C, B] = this match {
    case Fail(a) => Fail(f(a))
    case v @ Ok(_) => v
  }

  def mapOk[C](f: (B) => C): Result[A, C] = map(f)

  def mapFail[C](f: (A) => C): Result[C, B] = leftMap(f)

  def flatMap[AA >: A, D](f: B => Result[AA, D]): Result[AA, D] = this match {
    case fail @ Result.Fail(_) => fail
    case Result.Ok(b) => f(b)
  }

  def bimap[C, D](fa: A => C, fb: B => D): Result[C, D] = this match {
    case Fail(a) => Fail(fa(a))
    case Ok(b) => Ok(fb(b))
  }

  def filter[AA >: A](predicate: (B) => Boolean, orFailWith: => AA): Result[AA, B] = this match {
    case fail @ Result.Fail(_) => fail
    case ok @ Result.Ok(b) if predicate(b) => ok
    case _ => Fail(orFailWith)
  }

  def filterNot[AA >: A](predicate: (B) => Boolean, orFailWith: => AA): Result[AA, B] =
    filter(!predicate(_), orFailWith)

  def tap(sideEffect: B => Unit): Result[A, B] = this.map { x =>
    sideEffect(x)
    x
  }

  def tapOk(sideEffect: B => Unit): Result[A, B] = this.tap(sideEffect)

  def tapFail(sideEffect: A => Unit): Result[A, B] = this.mapFail { x =>
    sideEffect(x)
    x
  }

  def bitap[U](sideEffect: => U): Result[A, B] = this.bimap(a => { sideEffect; a }, b => { sideEffect; b })

  def swap: Result[B, A] = fold(Ok(_), Fail(_))

  def merge[AA >: A](implicit ev: B <:< AA): AA = fold(identity, ev.apply)

  def foreach(f: B => Unit): Unit = fold(_ => (), f)

  def getOrElse[BB >: B](default: => BB): BB = fold(_ => default, identity)

  def orElse[C, BB >: B](fallback: => Result[C, BB]): Result[C, BB] = this match {
    case Fail(_) => fallback
    case me @ Ok(_) => me
  }

  def recover[BB >: B](pf: PartialFunction[A, BB]): Result[A, BB] = this match {
    case Fail(a) if pf.isDefinedAt(a) => Ok(pf(a))
    case _ => this
  }
  def recoverWith[AA >: A, BB >: B](pf: PartialFunction[A, Result[AA, BB]]): Result[AA, BB] = this match {
    case Fail(a) if pf.isDefinedAt(a) => pf(a)
    case _ => this
  }

  def flatten[AA >: A, BB](implicit ev: B <:< Result[AA, BB]): Result[AA, BB] = this.flatMap(b => identity(ev(b)))

  def toEither: Either[A, B] = fold(Left(_), Right(_))

  def toOption: Option[B] = fold(_ => None, Some(_))

  def toList: List[B] = fold(_ => Nil, _ :: Nil)

  def toTry(implicit ev: A <:< Throwable): Try[B] = fold(Failure(_), Success(_))

  def isOk: Boolean

  def isFail: Boolean = !isOk

}

object Result extends ResultFunctions with ResultBuildFromImplicits {

  final case class Fail[+A](a: A) extends Result[A, Nothing] {
    override def isOk: Boolean = false
  }

  final case class Ok[+B](b: B) extends Result[Nothing, B] {
    override def isOk: Boolean = true
  }

  implicit class OptionResult[E, A](opt: Option[Result[E, A]]) {
    def sequence: Result[E, Option[A]] = opt.fold(Result.ok[E, Option[A]](Option.empty[A]))(_.map(Option.apply))
  }

  /**
    * Specialised version of the filters for boolean results (they read better and it's actually a common use-case, eg.
    * imagine checking whether something is true and wanting to fail on it.
    */
  implicit class ResultBooleanFilters[A](result: Result[A, Boolean]) {
    def orIfFalse[C >: A](failWith: => C): Result[C, Boolean] =
      result.filter(identity, failWith)

    def orIfTrue[C >: A](failWith: => C): Result[C, Boolean] =
      result.filterNot(identity, failWith)
  }

}

trait ResultFunctions {

  def fail[A, B](v: A): Result[A, B] = Fail(v)

  def ok[A, B](v: B): Result[A, B] = Ok(v)

  def fromEither[A, B](e: Either[A, B]): Result[A, B] = e.fold(fail, ok)

  def fromOption[A, B](o: Option[B], ifEmpty: => A): Result[A, B] = o.fold(fail[A, B](ifEmpty))(ok)

  def fromTry[B](t: Try[B]): Result[Throwable, B] = t match {
    case Success(v) => Ok(v)
    case Failure(e) => Fail(e)
  }

  def attempt[B](f: => B): Result[Throwable, B] = try Ok(f) catch {
    case NonFatal(t) => Fail(t)
  }

}
