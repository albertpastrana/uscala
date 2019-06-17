package uscala.util

import scala.collection.BuildFrom
import scala.util.Try

object TryOps {
  implicit class IterableTry[A, M[X] <: IterableOnce[X]](xs: M[Try[A]]) {
    def sequence(implicit cbf: BuildFrom[M[Try[A]], A, M[A]]): Try[M[A]] =
      xs.iterator.foldLeft(Try[scala.collection.mutable.Builder[A, M[A]]](cbf.newBuilder(xs))) {
        (fr, fa) => for (r <- fr; a <- fa) yield r += a
      }.map(_.result())
  }
}
