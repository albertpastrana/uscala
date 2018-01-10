package uscala.i18n

import java.io.File
import java.nio.charset.Charset
import java.nio.file.Path
import java.text.MessageFormat
import java.util.{Locale, Properties}

import scala.io.{Codec, Source}
import scala.util.Try

class I18N private(private[i18n] val locales: Map[Locale, Properties]) {

  /**
    * Returns true if the locale is defined in this scope (i.e. if there is
    * some properties file with messages for the locale)
    */
  def isDefined(locale: Locale): Boolean = locales.contains(locale)

  /**
    * Returns value for the key and the locale specified if exists, None if
    * either the locale or the key don't exist.
    */
  def get(locale: Locale)(key: String): Option[String] = locales.get(locale).flatMap(p => Option(p.getProperty(key)))

  /**
    * Same as get but it does params interpolation according to MessageFormat rules.
    */
  def getf(locale: Locale)(key: String, args: AnyRef*): Option[String] =
    locales.get(locale).flatMap(p => Option(p.getProperty(key)).map { msg =>
      val fmt = new MessageFormat(msg, locale)
      fmt.format(args.toArray)
    })

  /**
    * Returns value for the key and the locale specified if exists, returns the
    * value of the key if either the locale or the value don't exist. It is effectively
    * calling get().getOrElse().
    */
  def getk(locale: Locale)(key: String): String = get(locale)(key).getOrElse(key)

  /**
    * Returns the formatted value for the key and the locale specified if exists,
    * returns the value of the key if either the locale or the value don't exist.
    * It is effectively calling getf().getOrElse().
    */
  def getkf(locale: Locale)(key: String, args: AnyRef*): String = getf(locale)(key, args : _*).getOrElse(key)
}

object I18N {

  val utf8 = Charset.forName("UTF-8")
  val LocaleRegex = ".*_([^.]+).*".r

  def load(dir: Path)(implicit codec: Codec = Codec.UTF8): Try[I18N] = Try {
    def fromFilename(filename: File): Option[Locale] = filename.getName match {
      case LocaleRegex(locale) => Some(Locale.forLanguageTag(locale))
      case _ => None
    }
    def loadProperties(file: File): Properties = {
      val prp = new Properties
      prp.load(Source.fromFile(file).bufferedReader())
      prp
    }

    val map = dir.toFile.listFiles.flatMap { f: File =>
      fromFilename(f).map { locale =>
        locale -> loadProperties(f)
      }
    }.toMap
    new I18N(map)
  }

}
