# Result [![bintray](https://api.bintray.com/packages/albertpastrana/maven/uscala-result-async/images/download.svg) ](https://bintray.com/albertpastrana/maven/uscala-result-async/_latestVersion)

A right biased union type that holds a value for an asynchronous/future successful
computation or a value for a failed one.

It's a wrapper around `Future[Result[A, B]]`. Most of the functionality delegates to
the underlying `Result` which is inside of the future. It does provide a `flatMap`
instance which allows it to be used in a for comprehension.

It's an alternative to `Future` and very similar (but with much less functionality)
to `Task` in Scalaz.