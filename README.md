# μscala

[![Build Status](https://img.shields.io/travis/albertpastrana/uscala/master.svg)](https://travis-ci.org/albertpastrana/uscala) [![Coverage Status](https://coveralls.io/repos/github/albertpastrana/uscala/badge.svg?branch=master)](https://coveralls.io/github/albertpastrana/uscala?branch=master) [![codecov](https://codecov.io/gh/albertpastrana/uscala/branch/master/graph/badge.svg)](https://codecov.io/gh/albertpastrana/uscala) [![Codacy Badge](https://api.codacy.com/project/badge/Grade/ceb17c0f52dd400bb675d6d143b965c1)](https://www.codacy.com/app/albertpastrana/uscala)

μscala is a set of general purpose micro libraries written in Scala.

## What can I find in here?

In this project you'll find a set of general purpose micro libraries written
in scala.

List is as follows:

- [headed](headed/): an ultra simple "list" that ensures the existence
  of at least an element (i.e. a non-empty list).
- [i18n](i18n/): message internationalization scala micro library
  with no dependencies for the JVM.
- [resources](resources/): helps dealing with resources in the classpath, allowing
  null-safe access and listing resources from packages.
- [result](result/): a right biased union type that holds a value
  for a successful computation or a value for a failed one.
- [result-async](result-async/): a right biased union type that holds a value
  for an asynchronous/future successful computation or a value for a failed one.
- [result-specs2](result-specs2/): specs2 matchers for the result type
- [retry](retry/): small utility that retries a computation until it is
  successful using a backoff algorithm (exponential backoff by default).
- [timeout](timeout/): class that allows to query if a specific amount
  of time has elapsed or not.
- [try-ops](try-ops/): small library that adds some useful methods to `Try`
  like `sequence`.
- [typed-env](typed-env/): allows to retrieve environment variables safely
   and with types.
- [url](url/): immutable URL class with some useful methods to construct
  it, get the params, convert it to other types...

## Why?

Sometimes the Scala/Java ecosystem forces you to include huge libraries
when you only want to get a small piece of functionality. It's not strange
to find projects that include Cats, Scalaz, Guava or Apache Commons (or all
of them!) just because they want to use a simple piece of functionality from
each of those projects.

The idea behind μscala is to create a set of very small libraries that offer
some of the most common used functionality that is missing from the standard
Scala & Java standard libraries.

It's important to note that μscala is *not* and will never be a replacement
for any of the above libraries. They are excellent libraries that I use
in lots of projects.

## What defines a μ-library?

In order to be included in μscala, a library must fulfill the following
requisites:

- Shall be composed by a single functionality (oversimplifying, 1 single file)
- Shall not have any external dependency, except for:
  - Test libraries
  - A maximum of 2 μ-libraries
- Shall cover a very common functionality
- Shall be well tested
- Shall be MIT licensed unless for licensing compatibility issues a
  different open source license is needed.

## Can I contribute?

Yes, of course, any contribution is welcome. You can contribute either by:

1. Adding a new μ-library
2. Adding some function to an existing one

Just fork the repo and raise a PR.
