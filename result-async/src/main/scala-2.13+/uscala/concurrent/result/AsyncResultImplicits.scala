package uscala.concurrent.result

import scala.collection.BuildFrom
import scala.collection.mutable
import scala.concurrent.ExecutionContext

trait AsyncResultImplicits {
  import scala.language.higherKinds
  implicit class IterableAsyncResult[E, A, M[X] <: IterableOnce[X]](xs: M[AsyncResult[E, A]]) {
    def sequence(implicit cbf: BuildFrom[M[AsyncResult[E, A]], A, M[A]], executor: ExecutionContext): AsyncResult[E, M[A]] =
      xs.iterator.foldLeft(AsyncResult.ok[E, mutable.Builder[A, M[A]]](cbf.newBuilder(xs))) {
        (fr, fa) => for (r <- fr; a <- fa) yield r += a
      }.map(_.result())(executor)
  }

}
