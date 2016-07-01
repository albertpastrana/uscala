package uscala.result.async

import uscala.result.Result

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.Try

final case class AsyncResult[+A, +B](underlying: Future[Result[A, B]]) {
  def flatMap[AA >: A, D](f: B => AsyncResult[AA, D])(implicit ec: ExecutionContext): AsyncResult[AA, D] =
    AsyncResult(
      map(f).underlying.flatMap(
        _.fold(
          err => Future(Result.fail(err)),
          _.underlying
        )
      )
    )

  def flatMapR[AA >: A, D](f: B => Result[AA, D])(implicit ec: ExecutionContext): AsyncResult[AA, D] =
    AsyncResult(
      underlying.map(
        _.flatMap(f)
      )
    )


  def flatMapF[C](f: B => Future[C])(implicit ec: ExecutionContext): AsyncResult[A, C] =
    AsyncResult(
      underlying.flatMap(
        _.fold(
          err => Future(Result.fail(err)),
          f(_).map(Result.ok)
        )
      )
    )

  def map[C](f: B => C)(implicit ec: ExecutionContext): AsyncResult[A, C] =
    AsyncResult(
      underlying.map(
        _.map(f)
      )
    )

  def mapOk[C](f: B => C)(implicit ec: ExecutionContext): AsyncResult[A, C] = map(f)

  def leftMap[C](f: A => C)(implicit ec: ExecutionContext): AsyncResult[C, B] =
    AsyncResult(
      underlying.map(
        _.leftMap(f)
      )
    )

  def mapFail[C](f: A => C)(implicit ec: ExecutionContext): AsyncResult[C, B] = leftMap(f)

  def bimap[C, D](fa: A => C, fb: B => D)(implicit ec: ExecutionContext): AsyncResult[C, D] =
    AsyncResult(
      underlying.map(
        _.bimap(fa, fb)
      )
    )

  def swap(implicit ec: ExecutionContext): AsyncResult[B, A] = AsyncResult(
    underlying.map(
      _.swap
    )
  )

  def fold[C](fa: A => C, fb: B => C)(implicit ec: ExecutionContext): Future[C] = underlying.map(_.fold(fa, fb))

  def attemptRun[AA >: A](implicit f: Throwable => AA, ec: ExecutionContext): Result[AA, B] =
    Result.fromTry(
      Try(Await.result(underlying, Duration.Inf))
    ).leftMap(f).flatMap(identity)
}

object AsyncResult {

  def fromFuture[A, B](f: Future[B])(implicit ex: ExecutionContext): AsyncResult[A, B] = AsyncResult(f.map(Result.ok))

  def fromResult[A, B](r: Result[A, B])(implicit ex: ExecutionContext): AsyncResult[A, B] = AsyncResult(Future(r))

  def now[A, B](b: B)(implicit ex: ExecutionContext): AsyncResult[A, B] = AsyncResult(Future(Result.ok(b)))

  def ok[A, B](b: B)(implicit ex: ExecutionContext): AsyncResult[A, B] = now(b)

  def fail[A, B](a: A)(implicit ex: ExecutionContext): AsyncResult[A, B] = AsyncResult(Future(Result.fail(a)))

}
