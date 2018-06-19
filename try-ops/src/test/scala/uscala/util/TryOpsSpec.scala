package uscala.util

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import uscala.util.TryOps._

import scala.util.{Failure, Try}

class TryOpsSpec extends Specification with ScalaCheck {

  "TryOps" >> {
    "sequence" >> {
      "should transform an empty list of tries into a success of empty list" >> {
        List.empty[Try[Int]].sequence must beSuccessfulTry(List.empty[Int])
      }
      "should transform a list of success into a success of list" >> prop { xs: Seq[Int] =>
        xs.map(Try(_)).sequence must beSuccessfulTry(xs)
      }
      "should transform a list of Failure into a failure with the first exception" >> prop {
        xs: Seq[Exception] => xs.nonEmpty ==> {
          xs.map(x => Try[Int](throw x)).sequence must beAFailedTry(xs.head)
        }
      }
      "should transform a list with successes and failures into a failure with the first exception" >> {
        val err = new Exception()
        List(Try(1), Failure(err), Try(2)).sequence must beAFailedTry(err)
      }
    }
  }

}
