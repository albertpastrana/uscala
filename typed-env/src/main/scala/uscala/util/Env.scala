package uscala.util

import scala.collection.convert.Wrappers.JMapWrapper
import scala.concurrent.duration.Duration
import scala.util.{Success, Try}
import uscala.util.TryOps._

object Env {

  type Env = Map[String, String]

  /** The default map of environment variables will come from `System.getenv` */
  val DefaultEnv: Env = JMapWrapper(System.getenv).toMap

  /**
    * Will try to get the environment variable named `name_suffix` and convert it.
    * If it fails (either because the variable is not set or the value can't be converted)
    * it will then try to get the environment variable without the suffix (`name`).
    *
    * See: [[key]], [[orNone]].
    */
  def orNoneSuffix[T: EnvConv](name: String, suffix: String, env: Env = DefaultEnv): Option[T] =
    orNone(key(name, suffix), env).orElse(Env.orNone(name, env))

  /**
    * Same as `orNoneSuffix.getOrElse(default)`
    * See [[orNoneSuffix]]
    */
  def orElseSuffix[T: EnvConv](name: String, suffix: String, default: => T, env: Env = DefaultEnv): T =
    orNoneSuffix(name, suffix, env).getOrElse(default)

  /**
    * Same as `orNone.getOrElse(default)`
    * See [[orNone]]
    */
  def orElse[T: EnvConv](name: String, default: => T, env: Env = DefaultEnv): T =
    orNone(name, env).getOrElse(default)

  /**
    * Gets the value of the environment variable with name `name` and
    * tries to convert it to the type `T` if possible.
    * Will return Some(value) if the env variable is set and can be converted
    * to the specific type or None otherwise.
    */
  def orNone[T: EnvConv](name: String, env: Env = DefaultEnv): Option[T] =
    convert(name, env).flatMap(_.toOption)

  private def convert[T: EnvConv](name: String, env: Env): Option[Try[T]] =
    env.get(name).map(implicitly[EnvConv[T]].fromString)

  /** Constructs a key by appending `k`, `_` and `suffix` -> `{k}_{suffix}` */
  def key(k: String, suffix: String): String = k + "_" + suffix

}

trait EnvConv[T] {
  def fromString(value: String): Try[T]
}

object EnvConv {

  implicit val EnvReadString: EnvConv[String] = new EnvConv[String] {
    override def fromString(value: String): Try[String] = Success(value)
  }

  implicit val EnvReadDuration: EnvConv[Duration] = new EnvConv[Duration] {
    override def fromString(value: String): Try[Duration] = Try(Duration(value))
  }

  implicit val EnvReadInt: EnvConv[Int] = new EnvConv[Int] {
    override def fromString(value: String): Try[Int] = Try(value.toInt)
  }

  implicit val EnvReadLong: EnvConv[Long] = new EnvConv[Long] {
    override def fromString(value: String): Try[Long] = Try(value.toLong)
  }

  implicit val EnvReadFloat: EnvConv[Float] = new EnvConv[Float] {
    override def fromString(value: String): Try[Float] = Try(value.toFloat)
  }

  implicit val EnvReadDouble: EnvConv[Double] = new EnvConv[Double] {
    override def fromString(value: String): Try[Double] = Try(value.toDouble)
  }

  implicit def EnvReadList[T: EnvConv]: EnvConv[List[T]] = new EnvConv[List[T]] {
    override def fromString(value: String): Try[List[T]] = for {
      strings <- Try(value.split("\\s*,\\s*"))
      ts <- strings.map(implicitly[EnvConv[T]].fromString).toList.sequence
    } yield ts
  }

}

object Test extends App {
  println(Env.orElse("JAVA_HOME", "JH"))
  println(Env.orNone[Int]("JAVA_HOME"))
  println(Env.orElseSuffix("JAVA", "HOME", "JH"))
  println(Env.orNoneSuffix[String]("JAVA", "HOME"))
}