package uscala.result

import uscala.result.Result.{Fail, Ok}

import scala.collection.generic.CanBuildFrom

trait ResultBuildFromImplicits {
  import scala.language.higherKinds
  
  implicit class TraversableResult[E, A, M[X] <: TraversableOnce[X]](xs: M[Result[E, A]]) {
    def sequence(implicit cbf: CanBuildFrom[M[Result[E, A]], A, M[A]]): Result[E, M[A]] =
      xs.foldLeft(Result.ok[E, scala.collection.mutable.Builder[A, M[A]]](cbf(xs))) { (fr, fa) =>
        for {
          r <- fr
          a <- fa
        } yield r += a
      }.map(_.result())

    def split(implicit bfe : CanBuildFrom[Nothing, E, M[E]], bfa : CanBuildFrom[Nothing, A, M[A]]): (M[E], M[A]) = {
      val be = bfe()
      val ba = bfa()
      xs.foreach {
        case Ok(v) => ba += v
        case Fail(e) => be += e
      }
      (be.result, ba.result)
    }
  }

  /**
    * Add ability to sequence results in a Map, really useful for preserving keys but mapping values with a function
    * that can fail.
    */
  implicit class MapResult[E, A, K, M[X, Y] <: scala.collection.Map[X, Y]](xs: M[K, Result[E, A]]) {
    def sequence(implicit cbf: CanBuildFrom[M[K, Result[E, A]], (K, A), M[K, A]]): Result[E, M[K, A]] =
      xs.foldLeft(Result.ok[E, scala.collection.mutable.Builder[(K, A), M[K, A]]](cbf(xs))) { case (fa, (k, fr)) =>
        for {
          a <- fa
          r <- fr
        } yield a +=((k, r))
      }.map(_.result())
  }
}
