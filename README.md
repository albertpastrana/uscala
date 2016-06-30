# μscala  [![Build Status](https://img.shields.io/travis/albertpastrana/uscala/master.svg)](https://travis-ci.org/albertpastrana/uscala) [![Coverage Status] (https://img.shields.io/coveralls/albertpastrana/uscala.svg)](https://coveralls.io/github/albertpastrana/uscala?branch=master) [![codecov](https://codecov.io/gh/albertpastrana/uscala/branch/master/graph/badge.svg)](https://codecov.io/gh/albertpastrana/uscala)

Set of general purpose micro libraries in scala.

## What can I find in here?

In this project you'll find a set of general purpose micro libraries written
in scala.

List is as follows:

- [i18n](i18n/): message internationalization scala micro library
  with no dependencies for the JVM.
- [result](result/): a right biased union type that holds a value
  for a successful computation or a value for a failed one.
- [result-specs2](result-specs2/): specs2 matchers for the result type

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