# Retry [![bintray](https://api.bintray.com/packages/albertpastrana/maven/uscala-retry/images/download.svg) ](https://bintray.com/albertpastrana/maven/uscala-retry/_latestVersion)

A small utility that retries a computation until it is successful.
It can be useful in a number of different scenarios like, for
example, connecting to a remote server at startup time or trying
to get some result from an external api.

By default uses the exponential backoff strategy, but different
behaviours can be specified as a parameter.

## How to use it

```scala
> import uscala.util.Retry
> import scala.util.{Failure, Success, Try}
> import scala.concurrent.duration._

> var count = 0
> def op: Try[String] =
  if (count == 3) {
    count = 0
    Success(s"Done!")
  } else {
    count += 1
    Failure(new Exception("Ops..."))
  }


//Retry a maximum of two times
> Retry.retry(op, Some(2))
res1: scala.util.Try[String] = (java.lang.Exception: Ops...)

//This is equivalent
> count = 0
> Retry.until(op, 4)
res2: scala.util.Try[String] = Success(Done!)

//Retry forever
> Retry.retry(op, None)
res3: scala.util.Try[String] = Success(Done!)

//This is equivalent
> Retry.forever(op)
res4: scala.util.Try[String] = Success(Done!)

//Let's see what happens if a function always fails
var tries = 0
def fails: Try[String] = {
  tries += 1
  Failure(new Exception(s"Try #${tries-1}"))
}

//Retry a maximum of three times
> Retry.retry(fails, Some(3))
res5: scala.util.Try[String] = Failure(java.lang.Exception: Try #3)

//You can also retry without wait time or backoff strategy
> tries = 0
> def constant(retry: Int, interval: Duration) = interval
> Retry.retry(fails, wait = 0.millis, backoff = constant, maxRetries = Some(30000))
res6: scala.util.Try[String] = Failure(java.lang.Exception: Try #30000)

```

See more examples in the test directory.