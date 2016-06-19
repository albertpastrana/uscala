package uscala.result

import uscala.result.Result.{Fail, Ok}

import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

sealed abstract class Result[+A, +B] extends Serializable {

  def fold[C](fa: A => C, fb: B => C): C = this match {
    case Fail(a) => fa(a)
    case Ok(b) => fb(b)
  }

  def map[C](f: (B) => C) = this match {
    case Ok(b) => Ok(f(b))
    case _ => this
  }

  def leftMap[C](f: (A) => C) = this match {
    case Fail(a) => Fail(f(a))
    case _ => this
  }

  def mapOk[C](f: (B) => C) = map(f)

  def mapFail[C](f: (A) => C) = leftMap(f)

  def bimap[C, D](fa: A => C, fb: B => D): Result[C, D] = this match {
    case Fail(a) => Fail(fa(a))
    case Ok(b) => Ok(fb(b))
  }

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

  def toEither: Either[A, B] = fold(Left(_), Right(_))

  def toOption: Option[B] = fold(_ => None, Some(_))

  def toList: List[B] = fold(_ => Nil, _ :: Nil)

  def toTry(implicit ev: A <:< Throwable): Try[B] = fold(Failure(_), Success(_))

}

object Result extends ResultFunctions {
  final case class Fail[+A](a: A) extends Result[A, Nothing]
  final case class Ok[+B](b: B) extends Result[Nothing, B]
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