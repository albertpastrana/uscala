package uscala.util

import org.specs2.specification.core.Fragment

import scala.concurrent.duration._

class EnvSpec extends org.specs2.mutable.Specification {

  "Env" >> {

    def verify[T: EnvConv](svalue: String, value: T, defaultValue: T)(implicit m: Manifest[T]): Fragment = {
      s"when the type of the value is $m" >> {
        "if the key exists" >> {
          "orNone should return Some(value converted)" >> {
            Env.orNone("key", Map("key" -> svalue)) must beSome(value)
          }
          "orElse should return the value converted" >> {
            Env.orElse("key", defaultValue, Map("key" -> svalue)) must_=== value
          }
          "orNoneSuffix should return the value converted" >> {
            Env.orNoneSuffix("key", "suffix", Map("key_suffix" -> svalue)) must beSome(value)
          }
          "orElseSuffix should return the value converted" >> {
            Env.orElseSuffix("key", "suffix", defaultValue, Map("key_suffix" -> svalue)) must_=== value
          }
        }
        "if the key doesn't exist" >> {
          "orElse should return the default value" >> {
            Env.orElse("key", defaultValue, Map.empty) must_=== defaultValue
          }
          "orNone should return None" >> {
            Env.orNone("key", Map.empty) must beNone
          }
          "if the parent key exists" >> {
            "orNoneSuffix should return the value converted" >> {
              Env.orNoneSuffix("key", "suffix", Map("key" -> svalue)) must beSome(value)
            }
            "orElseSuffix should return the value converted" >> {
              Env.orElseSuffix("key", "suffix", defaultValue, Map("key" -> svalue)) must_=== value
            }
          }
          "if the parent key doesn't exist" >> {
            "orNoneSuffix should return the default value" >> {
              Env.orNoneSuffix("key", "suffix", Map.empty) must beNone
            }
            "orElseSuffix should return the default value" >> {
              Env.orElseSuffix("key", "suffix", defaultValue, Map.empty) must_=== defaultValue
            }
          }
        }
      }
    }

    verify("value", "value", "default-value")

    verify("1", 1, 2)

    verify("1", 1L, 2L)

    verify("1.0", 1.0f, 2.0f)

    verify("1.0", 1.0d, 2.0f)

    verify("1 day", Duration("1 day"), Duration("2 hours"))

    verify("a,b,c", List("a", "b", "c"), List("default-value"))

    verify("1, 2, 3", List(1, 2, 3), List(1))

    verify("1, 2, 3", List(1L, 2L, 3L), List(1L))

    verify("1.0, 2.0, 3.0", List(1.0f, 2.0f, 3.0f), List(1.0f))

    verify("1.0, 2.0, 3.0", List(1.0d, 2.0d, 3.0d), List(1.0d))

    verify("1d, 2d", List(Duration("1d"), Duration("2d")), List(Duration("2 hours")))

    "key should append the key and the suffix with an underscore separating them" >> {
      Env.key("key", "suffix") must_=== "key_suffix"
    }
  }

}
