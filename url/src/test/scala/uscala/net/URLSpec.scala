package uscala.net

import java.net.URI

import org.scalacheck.{Arbitrary, Gen}
import org.specs2.ScalaCheck
import org.specs2.mutable.Specification
import org.specs2.specification.core.Fragment

import URL._

class URLSpec extends Specification with ScalaCheck with TestData {

  "apply(String)" >> {
    "should fail" >> {
      "when it's an invalid url" >> {
        URL("not-valid-url:") must beAFailedTry
      }
      "when the url has no scheme" >> {
        URL("//host/path") must beAFailedTry
      }
      "when the url has no host" >> {
        URL("/relative") must beAFailedTry
      }
    }
    Fragment.foreach(testUrls) { case (raw, url) =>
      val result = URL(raw)
      s"when the url is $raw" >> {
        s"the scheme should be ${url.scheme}" >> {
          result must beASuccessfulTry.which(_.scheme must_== url.scheme)
        }
        s"the user info should be ${url.userInfo}" >> {
          result must beASuccessfulTry.which(_.userInfo must_== url.userInfo)
        }
        s"the host should be ${url.host}" >> {
          result must beASuccessfulTry.which(_.host must_== url.host)
        }
        s"the port should be ${url.port}" >> {
          result must beASuccessfulTry.which(_.port must_== url.port)
        }
        s"the rawPath should be ${url.rawPath}" >> {
          result must beASuccessfulTry.which(_.rawPath must_== url.rawPath)
        }
        s"the path should be ${url.path}" >> {
          result must beASuccessfulTry.which(_.path must_== url.path)
        }
        s"the params should be ${url.query}" >> {
          result must beASuccessfulTry.which(_.query must_== url.query)
        }
        s"the fragment should be ${url.fragment}" >> {
          result must beASuccessfulTry.which(_.fragment must_== url.fragment)
        }
      }
    }
  }

  "apply(URI)" >> {
    "should be equivalent to apply(String)" >> prop { u: StringUrl =>
      URL(new URI(u)) must_=== URL(u)
    }
  }

  "apply(JURL)" >> {
    "should be equivalent to apply(String)" >> prop { u: StringUrl =>
      URL(new JURL(u)) must_=== URL(u)
    }
  }

  "path" >> {
    "should decode the rawPath correctly" >> {
      "when it has a space" >> {
        URL("http://example.com/hello%20world/").map(_.path) must beASuccessfulTry(Some("/hello world/"))
      }
      "when it has an encoded `/` and `?` in it" >> {
        URL("http://example.com/a%2Fb%3Fc").map(_.path) must beASuccessfulTry(Some("/a/b?c"))
      }
    }
  }

  def equivalentURI(u: StringUrl)(res: URI) = {
    val expected = new URI(u)
    res.getScheme must_== expected.getScheme  aka "scheme"
    res.getUserInfo must_== expected.getUserInfo
    res.getHost must_== expected.getHost
    res.getPort must_== expected.getPort
    res.getRawPath must_== expected.getRawPath
    res.getPath must_== expected.getPath
    Option(res.getQuery).map(_.split('&').toSet) must_== Option(expected.getQuery).map(_.split('&').toSet)
    res.getFragment must_== expected.getFragment
  }

  def equivalentJURL(u: StringUrl)(res: JURL) = {
    def sameQuery(q1: Option[String], q2: Option[String]) =
      q1.map(URL.decode).map(_.split('&').toSet) must_== q2.map(URL.decode).map(_.split('&').toSet)
    val expected = new JURL(u)
    res.getProtocol must_== expected.getProtocol
    res.getUserInfo must_== expected.getUserInfo
    res.getHost must_== expected.getHost
    res.getPort must_== expected.getPort
    res.getAuthority must_== expected.getAuthority
    res.getPath must_== expected.getPath
    sameQuery(Option(res.getQuery), Option(expected.getQuery))
    res.getRef must_== expected.getRef
  }

  "asString should provide an equivalent URI" >> prop { u: StringUrl =>
    URL(u).map(u => new URI(u.asString)) must beASuccessfulTry.which(equivalentURI(u))
  }

  "asString should provide an equivalent JURL" >> prop { u: StringUrl =>
    URL(u).map(u => new JURL(u.asString)) must beASuccessfulTry.which(equivalentJURL(u))
  }

  "asURI should be equivalent to new URI" >> prop { u: StringUrl =>
    URL(u).map(_.asURI) must beASuccessfulTry.which(equivalentURI(u))
  }

  "asJURL should be equivalent to new URL" >> prop { u: StringUrl =>
    URL(u).map(_.asJURL) must beASuccessfulTry.which(equivalentJURL(u))
  }

  "param" >> {
    "of a url without query string" >> {
      val url = "http://example.com"
      "should add the param" >> {
        val expected = URL(s"$url?with=param")
        val noParams = URL(url)
        noParams.map(_.param("with", "param")) must_=== expected
      }
      "should add the multi param" >> {
        val expected = URL(s"$url?with=param&with=other")
        val noParams = URL(url)
        noParams.map(_.param("with", List("param", "other"))) must_=== expected
      }
      "should be able to call param repeatedly" >> {
        val expected = URL(s"$url?p1=v1&p2=v2")
        val noParams = URL(url)
        noParams.map(_.param("p1", "v1").param("p2", "v2")) must_=== expected
      }
    }
    "of a url with query string" >> {
      val url = "http://example.com?already=here"
      "should add the param" >> {
        val expected = URL(s"$url&with=param")
        val noParams = URL(url)
        noParams.map(_.param("with", "param")) must_=== expected
      }
      "should add the multi param" >> {
        val expected = URL(s"$url&with=param&with=other")
        val noParams = URL(url)
        noParams.map(_.param("with", List("param", "other"))) must_=== expected
      }
      "should be able to call param repeatedly" >> {
        val expected = URL(s"$url&p1=v1&p2=v2")
        val noParams = URL(url)
        noParams.map(_.param("p1", "v1").param("p2", "v2")) must_=== expected
      }
    }
  }

}

trait TestData {
  //TODO include some examples from: https://www.talisman.org/~erlkonig/misc/lunatech%5ewhat-every-webdev-must-know-about-url-encoding/
  val testUrls = List(
    "http://host" -> URL(scheme = "http", host = "host", rawPath = Some("")),
    "https://host?" -> URL(scheme = "https", host = "host", rawPath = Some(""), query = NonEmptyQuery(Map.empty)),
    "ftp://example.com/" -> URL(scheme = "ftp", host = "example.com", rawPath = Some("/")),
    "http://host/a%20path" -> URL(scheme = "http", host = "host", rawPath = Some("/a%20path")),
    "http://user:password@example.com:77/p/a/t/h?foo=bar&key=value#foo"
      -> URL(scheme = "http", userInfo = Some("user:password"), host = "example.com", port = Some(77),
             rawPath = Some("/p/a/t/h"), query = NonEmptyQuery(Map("foo" -> List("bar"), "key" -> List("value"))),
             fragment = Some("foo")),
    "http://example.com?foo=bar&encoded=%2F-(4)+%26+a"
      -> URL(scheme = "http", host = "example.com", rawPath = Some(""),
             query = NonEmptyQuery(Map("foo" -> List("bar"), "encoded" -> List("/-(4) & a"))))
  )

  val testUris = testUrls.map { case (u, _) => new URI(u) }

  type StringUrl = String

  def abStringUrlGen = Gen.oneOf(testUrls.map { case (u, _) => u })

  implicit def abStrings: Arbitrary[StringUrl] = Arbitrary(abStringUrlGen)

}