# Result [![bintray](https://api.bintray.com/packages/albertpastrana/maven/uscala-result/images/download.svg) ](https://bintray.com/albertpastrana/maven/uscala-result/_latestVersion)

A right biased union type that holds a value for a successful computation
or a value for a failed one.

It's an alternative to `Either`, the equivalent of `Xor` in Cats or `\/` in
Scalaz and its name is inspired by Rust's `Result`.

Note that `Either` in scala 2.12 will be right biased so Result won't be probably
needed anymore.