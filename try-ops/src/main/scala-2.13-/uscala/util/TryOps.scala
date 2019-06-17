package uscala.util

import scala.collection.generic.CanBuildFrom
import scala.util.Try

object TryOps {
  import scala.language.higherKinds

  implicit class TraversableTry[A, M[X] <: TraversableOnce[X]](xs: M[Try[A]]) {
    def sequence(implicit cbf: CanBuildFrom[M[Try[A]], A, M[A]]): Try[M[A]] =
      xs.foldLeft(Try[scala.collection.mutable.Builder[A, M[A]]](cbf(xs))) {
        (fr, fa) => for (r <- fr; a <- fa) yield r += a
      }.map(_.result())
  }

}
