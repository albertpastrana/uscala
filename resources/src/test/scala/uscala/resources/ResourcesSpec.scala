package uscala.resources

import org.specs2.mutable.Specification

import scala.io.{Codec, Source}
import scala.util.Try

class ResourcesSpec extends Specification {

  val nonExistingResource = "this/does/not/exist"

  "when an existing resource is provided" >> {
    val resource = "/exists/resource1"

    "asURL should return Some(url)" >> {
      Resources.asURL(resource) must beSome.which { url =>
        url.toString must endWith(resource)
      }
    }
    "asStream should return Some(stream)" >> {
      Resources.asStream(resource) must beSome.which { stream =>
        Source.fromInputStream(stream).mkString must_=== "content1"
      }
    }
    "listAsPaths should return None" >> {
      Resources.listAsPaths(resource) must beNone
    }
    "listAsStreams should return None" >> {
      Resources.listAsStreams(resource) must beNone
    }
  }

  "when an existing package is provided" >> {
    "and it is in a classpath directory" >> {
      val pakage = "/exists"

      "asURL should return the url of the resource" >> {
        Resources.asURL(pakage) must beSome.which { url =>
          url.toString must endWith("exists")
        }
      }
      "asStream should return the contents of the package" >> {
        Resources.asStream(pakage) must beSome.which { stream =>
          Source.fromInputStream(stream).mkString must_=== "inside\nresource1\nresource2\n"
        }
      }
      "listAsPaths should return a list with all the resources in package" >> {
        Resources.listAsPaths(pakage) must beSome.which { it =>
          it.map(_.getFileName.toString).toList must containTheSameElementsAs(List("resource1", "resource2"))
        }
      }
      "listAsStreams should return a list with readers pointing to all resources in the package" >> {
        Resources.listAsStreams(pakage) must beSome.which { it =>
          it.map(Source.fromInputStream).map(_.mkString).toList must containTheSameElementsAs(List("content1", "content2"))
        }
      }
    }
    "and it is in a classpath jar" >> {
      val pakage = "/org/specs2"

      "asURL should return the url of the resource" >> {
        Resources.asURL(pakage) must beSome.which { url =>
          url.toString must endWith(pakage)
        }
      }
      "asStream returns an invalid stream" >> {
        Resources.asStream(pakage) must beSome.which { stream =>
          Try(stream.read()) must beAFailedTry
        }
      }
      "listAsPaths should return a list with all the resources in package" >> {
        Resources.listAsPaths(pakage) must beSome.which { it =>
          val contents = it.map(_.getFileName.toString).toList
          contents must contain(allOf("Spec.class", "Specification.class"))
          contents must not(contain("specification"))
        }
      }
      "listAsStreams should return a list with readers pointing to all resources in the package" >> {
        Resources.listAsStreams(pakage)(Codec.ISO8859) must beSome.which { it =>
          val first = it.next()
          first.read() must_=== 0xCA
          first.read() must_=== 0xFE
          first.read() must_=== 0xBA
          first.read() must_=== 0xBE
        }
      }
    }
  }

  "when a non existing resource is provided" >> {
    "asURL should return None" >> {
      Resources.asURL(nonExistingResource) must beNone
    }
    "asStream should return None" >> {
      Resources.asStream(nonExistingResource) must beNone
    }
    "listAsPaths should return None" >> {
      Resources.listAsPaths(nonExistingResource) must beNone
    }
    "listAsStreams should return None " >> {
      Resources.listAsPaths(nonExistingResource) must beNone
    }
  }

}
