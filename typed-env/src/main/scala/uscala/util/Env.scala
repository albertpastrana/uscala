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
  def orNoneSuffix[T: EnvConv](name: String, suffix: String)(env: Env = DefaultEnv): Option[T] =
    orNone(key(name, suffix))(env).orElse(Env.orNone(name)(env))

  /**
    * Same as `orNoneSuffix.getOrElse(default)`
    * See [[orNoneSuffix]]
    */
  def orElseSuffix[T: EnvConv](name: String, suffix: String, default: => T)(env: Env = DefaultEnv): T =
    orNoneSuffix(name, suffix)(env).getOrElse(default)

  /**
    * Same as `orNone.getOrElse(default)`
    * See [[orNone]]
    */
  def orElse[T: EnvConv](name: String, default: => T)(env: Env = DefaultEnv): T =
    orNone(name)(env).getOrElse(default)

  /**
    * Gets the value of the environment variable with name `name` and
    * tries to convert it to the type `T` if possible.
    * Will return Some(value) if the env variable is set and can be converted
    * to the specific type or None otherwise.
    */
  def orNone[T: EnvConv](name: String)(env: Env = DefaultEnv): Option[T] =
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

  implicit object EnvReadString extends EnvConv[String] {
    override def fromString(value: String): Try[String] = Success(value)
  }

  implicit object EnvReadDuration extends EnvConv[Duration] {
    override def fromString(value: String): Try[Duration] = Try(Duration(value))
  }

  implicit object EnvReadInt extends EnvConv[Int] {
    override def fromString(value: String): Try[Int] = Try(value.toInt)
  }

  implicit object EnvReadLong extends EnvConv[Long] {
    override def fromString(value: String): Try[Long] = Try(value.toLong)
  }

  implicit object EnvReadFloat extends EnvConv[Float] {
    override def fromString(value: String): Try[Float] = Try(value.toFloat)
  }

  implicit object EnvReadDouble extends EnvConv[Double] {
    override def fromString(value: String): Try[Double] = Try(value.toDouble)
  }

  implicit def EnvReadList[T: EnvConv]: EnvConv[List[T]] = new EnvConv[List[T]] {
    override def fromString(value: String): Try[List[T]] = for {
      strings <- Try(value.split("\\s*,\\s*"))
      ts <- strings.map(implicitly[EnvConv[T]].fromString).toList.sequence
    } yield ts
  }

}
