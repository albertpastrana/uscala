package uscala.concurrent.result

import scala.collection.generic.CanBuildFrom
import scala.collection.mutable
import scala.concurrent.ExecutionContext

trait AsyncResultImplicits {
  import scala.language.higherKinds
  implicit class TraversableAsyncResult[E, A, M[X] <: TraversableOnce[X]](xs: M[AsyncResult[E, A]]) {
    def sequence(implicit cbf: CanBuildFrom[M[AsyncResult[E, A]], A, M[A]], executor: ExecutionContext): AsyncResult[E, M[A]] =
      xs.foldLeft(AsyncResult.ok[E, mutable.Builder[A, M[A]]](cbf(xs))) {
        (fr, fa) => for (r <- fr; a <- fa) yield r += a
      }.map(_.result())(executor)
  }

}
