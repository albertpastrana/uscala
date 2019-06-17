package uscala.net

import java.net.{URI, URLDecoder, URLEncoder}

import scala.collection.immutable.{Map, List}
import scala.util.{Failure, Success, Try}

// Shim for Scala 2.12/2.13 compatibility.
import uscala.net.MapViewShim._

case class URL(scheme: String,
               userInfo: Option[String] = None,
               host: String,
               port: Option[Int] = None,
               rawPath: Option[String] = None,
               query: Query = Query.empty,
               fragment: Option[String] = None) {

  import URL._

  lazy val path: Option[String] = rawPath.map(URL.decode)

  lazy val asURI: URI = new URI(asString)

  lazy val asJURL: JURL = new JURL(asString)

  lazy val asString: String =
    s"""
      |$scheme://${userInfo.map(u => s"$u@").getOrElse("")}$host
      |${port.map(p => s":$p").getOrElse("")}
      |${rawPath.getOrElse("")}
      |${query.asString}
      |${fragment.map(f => s"#$f").getOrElse("")}
      |""".stripMargin.replaceAll("\n", "")

  def param(name: String, value: String): URL = param(name, List(value))

  def param(name: String, values: List[String]): URL = copy(query = query.param(name, values))

}

object URL {

  type JURL = java.net.URL

  def apply(url: String): Try[URL] = Try(new URI(url)).flatMap(URL(_))

  def apply(jurl: JURL): Try[URL] = apply(jurl.toURI)

  def apply(uri: URI): Try[URL] = {
    def tryArg(s: String, name: String): Try[String] =
      Option(s).map(Success(_))
        .getOrElse(Failure(new IllegalArgumentException(s"Need to specify a $name")))

    for {
      scheme   <- tryArg(uri.getScheme, "scheme")
      host     <- tryArg(uri.getHost, "host")
      port     <- if (uri.getPort == -1) Success(None) else Success(Some(uri.getPort))
      params   <- Query(uri.getRawQuery)
    } yield URL(scheme,
                Option(uri.getUserInfo),
                host,
                port,
                Option(uri.getRawPath),
                params,
                Option(uri.getFragment))
  }
  @inline
  private[net] def encode(s: String): String = URLEncoder.encode(s, "UTF-8")

  @inline
  private[net] def decode(s: String): String = URLDecoder.decode(s, "UTF-8")

}

sealed abstract class Query {

  val hasQuery: Boolean

  def get(key: String): Option[List[String]]

  def getOrElse(key: String, default: => List[String]): List[String]

  def + (kv: (String, List[String])): Query

  def param(key: String, values: scala.List[String]): Query = this + (key -> values)

  def asString: String

}

case object Empty extends Query {

  override val hasQuery = false

  override def get(key: String): Option[scala.List[String]] = None

  override def getOrElse(key: String, default: => List[String]): List[String] = default

  override def +(kv: (String, scala.List[String])): Query = NonEmptyQuery(Map(kv))

  override def asString: String = ""

}

final case class NonEmptyQuery(underlying: Map[String, List[String]]) extends Query {

  override val hasQuery = true

  override def get(key: String): Option[scala.List[String]] = underlying.get(key)

  override def +(kv: (String, scala.List[String])): Query = NonEmptyQuery(underlying + kv)

  override def getOrElse(key: String, default: => List[String]): List[String] = underlying.getOrElse(key, default)

  override def asString: String =
    "?" + underlying.flatMap {
      case (key, Nil) => List(key)
      case (key, values) => values.map(v => s"$key=${URL.encode(v)}")
    }.mkString("&")

}

object Query {

  val empty: Empty.type = Empty

  def apply(rawQuery: String): Try[Query] = Try {
    Option(rawQuery).collect { case query if query.nonEmpty =>
      query.split('&').map(_.split('=')).collect {
        case Array(key, value) => key -> URL.decode(value)
        case Array(key) => key -> ""
        case a @ Array(key, _*) => key -> a.tail.mkString("=")
      }.groupBy { case (key, _) => key }
       .mapValuesShim(_.toList.map{ case (_, v) => v }.filter(_.nonEmpty))
       .toMap
    }.fold[Query](Empty)(NonEmptyQuery)
  }

}
