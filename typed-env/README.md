# Typed-env [![bintray](https://api.bintray.com/packages/albertpastrana/maven/uscala-typed-env/images/download.svg) ](https://bintray.com/albertpastrana/maven/uscala-typed-env/_latestVersion)

Safely retrieve typed environment variables values with this scala micro library with no dependencies for the JVM.

## How to use it

```
export N=1 D=1day L=1,2,3 N_S=2 sbt typed-env/console
```

```scala
> import uscala.util.Env
> import scala.concurrent.duration._

> Env.orElse("N", "default")
  res0: String = 1
> Env.orElse("N", 3)
  res1: Int = 1
> Env.orElse("NN", 3)
  res2: Int = 3
> Env.orNone[Int]("N")
  res3: Option[Int] = Some(1)
> Env.orNone[Int]("NN")
  res4: Option[Int] = None
> Env.orElse[Duration]("D", 3.days)
  res5: scala.concurrent.duration.Duration = 1 day
> Env.orElse[Duration]("DD", 3.days)
  res6: scala.concurrent.duration.Duration = 3 days
> Env.orElse("L", List(5, 6, 7))
  res7: List[Int] = List(1, 2, 3)
> Env.orElse("LL", List(5, 6, 7))
  res8: List[Int] = List(5, 6, 7)
> Env.orElseSuffix("N", "S", 3)
  res9: Int = 2
> Env.orElseSuffix("N", "SU", 3)
  res10: Int = 1
> Env.orElseSuffix("NN", "SU", 3)
  res11: Int = 3
```
