package uscala.collection

import HeadedOps._
import org.specs2.Spec

class HeadedSpec extends Spec { def is = s2"""
 Headed
  asList should return a list with the head and the tail
  ${ Headed(1).asList must_=== List(1) }
  ${ Headed(1, List(2)).asList must_=== List(1, 2) }

  fromList should try to transform a List into a Headed
  ${ Headed.fromList(List.empty[Int]) must beNone }
  ${ Headed.fromList(List(1, 2)) must beSome(Headed(1, List(2))) }

 HeadedOps
  ListHeaded
    toHeaded should try to transform a List into a Headed
    ${ List.empty[Int].toHeaded must beNone }
    ${ List(1, 2).toHeaded must beSome(Headed(1, List(2))) }
"""

}
