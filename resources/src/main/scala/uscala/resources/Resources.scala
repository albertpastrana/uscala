package uscala.resources

import java.io.{FileInputStream, InputStream}
import java.net.{URI, URL}
import java.nio.file._
import java.util

import scala.collection.convert.Wrappers.JIteratorWrapper
import scala.util.Try

object Resources {

  private val EmptyJMap = new util.HashMap[String, Object]()

  /**
    * Null-safe alternative to `getClass.getResource(name)` as it
    * basically wraps its result in an `Option`.
    * See java.lang.Class#getResource for more information.
    */
  def asURL(name: String): Option[URL] = Option(getClass.getResource(name))

  /**
    * Null-safe alternative to `getClass.getResourceAsStream(name)` as it
    * basically wraps its result in an `Option`.
    *
    * Sadly, the method `Class#getResourceAsStream` when called with a valid package name
    * it returns a non-null input stream of type `JarURLConnection.JarURLInputStream` that
    * has been initialized with a null `InputStream`.
    *
    * Given the methods we have available in all the hierarchy of `JarURLInputStream`
    * (that includes `InputStream` & `FilterInputStream`), can't find a way fo knowing if
    * the stream is valid or not, the only way is trying to read it (and it will blow up
    * in your face with a `NullPointerException`.
    *
    * So, consumers of this method, be careful.
    *
    * See `java.lang.Class#getResourceAsStream` for more information.
    */
  def asStream(name: String): Option[InputStream] = Option(getClass.getResourceAsStream(name))

  /**
    * List all resources (files) under a package (one level deep only).
    *
    * It does not return the packages (directories) inside it.
    *
    * If the package does not exists, then None is returned.
    */
  def listAsPaths(pakage: String): Option[Iterator[Path]] = {
    def filesystem(uri: URI): Option[FileSystem] =
      Try(FileSystems.getFileSystem(uri)).recoverWith {
        case e: FileSystemNotFoundException => Try(FileSystems.newFileSystem(uri, EmptyJMap))
      }.toOption

    asURL(pakage)
      .map(_.toURI)
      .flatMap { uri =>
        if (isJar(uri)) filesystem(uri).map(_.getPath(pakage))
        else Some(Paths.get(uri))
      }
      .collect {
        case path if Files.isDirectory(path) =>
          JIteratorWrapper(Files.list(path).iterator()).filter(Files.isRegularFile(_))
      }
  }

  /**
    * Same as [[listAsPaths]] but mapping each element into an `InputStream`.
    */
  def listAsStreams(pakage: String): Option[Iterator[InputStream]] =
    listAsPaths(pakage).map { it =>
      it.collect {
        case path if isJar(path.toUri) => getClass.getResourceAsStream(path.toString)
        case path => new FileInputStream(path.toString)
      }
    }

  private def isJar(uri: URI) = uri.getScheme == "jar"
}
