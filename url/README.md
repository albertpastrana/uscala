# URL [![bintray](https://api.bintray.com/packages/albertpastrana/maven/uscala-url/images/download.svg) ](https://bintray.com/albertpastrana/maven/uscala-url/_latestVersion)

An immutable URL class with some useful methods to construct it,
get the params, convert it to other types...

It's an alternative to java.net.URL, java.net.URI and many of the URLBuilder
classes that are in projects like apache http.

The module contains also a Query class that it's useful if you just want
to deal with the query string and its parameters.

## How to use it

Note that the most common way of constructing a URL involves calling
the `apply` method of the companion object. This method returns a `Try`
as it can fail in case the url is malformed.

```scala
import uscala.net._

//construct it
val url = URL("http://example.com")

//add a parameter
url.map(_.param("foo", "bar"))

//add a multi value parameter
url.map(_.param("foo", List("bar", "barr")))

//get the query
url.map(_.query)

//get the url as a String
url.map(_.asString)

//get the url as a java.net.URL
url.map(_.asJURL)

//get the url as a java.net.URI
url.map(_.asURI)
```

See the tests for more examples.