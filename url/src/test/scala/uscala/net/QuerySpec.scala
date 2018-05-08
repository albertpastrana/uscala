package uscala.net

import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

class QuerySpec extends Specification with ScalaCheck with TestQueries {

  "apply(String)" >> {
    "should return an empty Query" >> {
      "when the query is null" >> {
        Query(null) must beASuccessfulTry.which(_.hasQuery must_=== false)
      }
      "when the query is empty" >> {
        Query("") must beASuccessfulTry.which(_.hasQuery must_=== false)
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

  "asString" >> {
    "should return empty string if it's an empty query" >> {
      Query("").map(_.asString) must beASuccessfulTry("")
    }
    "should add the ? at the beginning of the query" >> {
      Query("key=value").map(_.asString) must beASuccessfulTry("?key=value")
    }
    "should return only the key if there is no value" >> {
      Query("key=").map(_.asString) must beASuccessfulTry("?key")
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
    "asString should return an empty string" >> {
      Empty.asString must_=== ""
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
  val testQueries: List[(String, Map[String, List[String]])] = List(
    "q" -> Map("q" -> Nil),
    "q=" -> Map("q" -> Nil),
    "k:ey=a=a" -> Map("k:ey" -> List("a=a")),
    "k:ey=a?a" -> Map("k:ey" -> List("a?a")),
    "k=a&k=b&k=c" -> Map("k" -> List("a", "b", "c")),
    "foo=bar&key=value" -> Map("foo" -> List("bar"), "key" -> List("value")),
    "foo=bar&encoded=%2F-(4)+%26+a" -> Map("foo" -> List("bar"), "encoded" -> List("/-(4) & a")),
    "%3Cmy_tag_2f0e5c3f94df95106fbdd455f9abb89640/%3E=" -> Map("%3Cmy_tag_2f0e5c3f94df95106fbdd455f9abb89640/%3E" -> Nil)
  )

}
