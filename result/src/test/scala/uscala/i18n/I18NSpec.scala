package uscala.i18n

import java.nio.charset.Charset
import java.nio.file.Paths
import java.util.{Date, Locale}

import scala.io.Codec

class I18NSpec extends org.specs2.mutable.Specification {

  val enLocale = Locale.forLanguageTag("en")
  val caLocale = Locale.forLanguageTag("ca")
  val plLocale = Locale.forLanguageTag("pl")

  val i18n = I18N.load(Paths.get(getClass.getResource("/utf8").toURI))

  "load" >> {
    "should load only the property files that match the pattern {name}_{locale}.{extension}" >> {
      i18n must beASuccessfulTry.which { i18n =>
        i18n.locales.keySet must_=== Set(enLocale, caLocale)
      }
    }
    "should load the properties file in UTF8 and handle correctly values with special chars" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.get(enLocale)("special.chars") must beSome("àéîoüç€£¥πµ~")
      )
    }
    "should load the properties file in ISO-8859-1 and handle correctly values with special chars" >> {
      val dir = Paths.get(getClass.getResource("/iso-8859-1").toURI)
      val i18n = I18N.load(dir)(Codec.ISO8859)
      i18n must beASuccessfulTry.which(i18n =>
        i18n.get(caLocale)("key") must beSome("caçadors de bolets")
      )
    }
  }
  "get" >> {
    "should return the value if both the locale and the key exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.get(enLocale)("key") must beSome("a message")
      )
    }
    "should return None if the locale doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.get(plLocale)("key") must beNone
      )
    }
    "should return None if the key doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.get(enLocale)("non-existing-key") must beNone
      )
    }
  }
  "getf" >> {
    "should return the formatted message if both the locale and the key exist" >> {
      i18n must beASuccessfulTry.which { i18n =>
        i18n.getf(enLocale)("format", "John", "25") must beSome("John is 25")
        i18n.getf(caLocale)("format", "Joan", "25") must beSome("25 anys té en Joan")
      }
    }
    "should take into account the locale when formatting the message with numeric fields" >> {
      i18n must beASuccessfulTry.which { i18n =>
        val num: java.lang.Float = 2500.87f
        i18n.getf(enLocale)("format.number", num) must beSome("2,500.87 is a big number")
        i18n.getf(caLocale)("format.number", num) must beSome("2.500,87 és un número gran")
      }
    }
    "should take into account the locale when formatting the message with date fields" >> {
      i18n must beASuccessfulTry.which { i18n =>
        val date = new Date()
        date.setTime(0)
        i18n.getf(enLocale)("format.date", date) must beSome("1/1/70 is a date")
        i18n.getf(caLocale)("format.date", date) must beSome("01/01/70 és una data")
                                       }
    }
    "should return None if the locale doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getf(plLocale)("key") must beNone
      )
    }
    "should return None if the key doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getf(enLocale)("non-existing-key") must beNone
      )
    }
  }
  "getk" >> {
    "should return the value if both the locale and the key exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getk(enLocale)("key") must_== "a message"
      )
    }
    "should return the key if the locale doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getk(enLocale)("non-existing-key") must_== "non-existing-key"
      )
    }
    "should return the key if the key doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getk(enLocale)("non-existing-key") must_== "non-existing-key"
      )
    }
  }
  "getkf" >> {
    "should return the value formatted if both the locale and the key exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getkf(enLocale)("format", "John", "25") must_== "John is 25"
      )
    }
    "should return the key if the locale doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getkf(enLocale)("non-existing-key", "arg") must_== "non-existing-key"
      )
    }
    "should return the key if the key doesn't exist" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.getkf(enLocale)("non-existing-key", "arg") must_== "non-existing-key"
      )
    }
  }
  "isDefined" >> {
    "should return true if the locale has some keys defined" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.isDefined(enLocale) must_== true
      )
    }
    "should return false if the locale is not defined" >> {
      i18n must beASuccessfulTry.which(i18n =>
        i18n.isDefined(plLocale) must_== false
      )
    }
  }
}
