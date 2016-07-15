package uscala.net

import java.net.{URI, URL => JURL}

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

class QuerySpec extends Specification with ScalaCheck with TestQueries {

  "apply(String)" >> {
    "when the query is null" >> {
      "should return an empty Query" >> {
        Query(null) must beASuccessfulTry.which(_.hasQuery must_=== false)
      }
    }

    Fragment.foreach(testQueries) { case (raw, params) =>
      s"when the query is [$raw]" >> {
        s"the params should be $params" >> {
          Query(raw) must beASuccessfulTry.like {
            case NonEmptyQuery(p) => p must_== params
          }
        }
      }
    }
  }

  "Empty" >> {
    "hasQuery should always return false" >> {
      Empty.hasQuery must_== false
    }
    "get should always return None" >> {
      Empty.get("a") must beNone
    }
    "getOrElse should always return the default value" >> {
      Empty.getOrElse("a", List("b")) must_=== List("b")
    }
  }

  "NonEmptyQuery" >> {
    "hasQuery should always return true" >> {
      NonEmptyQuery(Map.empty).hasQuery must_== true
      NonEmptyQuery(Map("a" -> List("b"))).hasQuery must_== true
    }
    "get should return the value of the underlying map" >> {
      NonEmptyQuery(Map.empty).get("a") must beNone
      NonEmptyQuery(Map("a" -> List("b"))).get("a") must beSome(List("b"))
    }
    "getOrElse" >> {
      "when the key exists" >> {
        "should return the value of the underlying map" >> {
          NonEmptyQuery(Map("a" -> List("b"))).getOrElse("a", List.empty) must_=== List("b")
        }
      }
      "when the key doesn't exist" >> {
        "should return the default value" >> {
          NonEmptyQuery(Map.empty).getOrElse("a", List("b")) must_=== List("b")
        }
      }
    }
  }

}

trait TestQueries {
  val testQueries = List(
    "" -> Map.empty,
    "q" -> Map("q" -> Nil),
    "q=" -> Map("q" -> Nil),
    "k:ey=a=a" -> Map("k:ey" -> List("a=a")),
    "k:ey=a?a" -> Map("k:ey" -> List("a?a")),
    "k=a&k=b&k=c" -> Map("k" -> List("a", "b", "c")),
    "foo=bar&key=value" -> Map("foo" -> List("bar"), "key" -> List("value")),
    "foo=bar&encoded=%2F-(4)+%26+a" -> Map("foo" -> List("bar"), "encoded" -> List("/-(4) & a"))
  )

}