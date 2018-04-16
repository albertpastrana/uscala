package uscala.result.specs2

import org.specs2.Spec
import uscala.result.Result._

class ResultMatchersSpec extends Spec with ResultMatchers { def is = s2"""
 The ResultMatchers trait provides matchers to check Result instances

  beOk checks if an element is Ok(_)
  ${ Ok(1) must beOk(1) }
  ${ Ok(1) must beOk((i: Int) => i must be_>(0)) }
  ${ Ok(1) must beOk(Seq(true, true)) }
  ${ Ok(1) must beOk(===(1)) }
  ${ Ok(Seq(1)) must beOk(===(Seq(1))) }
  ${ Ok(1) must beOk.like { case i => i must be_>(0) } }
  ${ (Ok(1) must beOk.like { case i => i must be_<(0) }) returns "1 is not less than 0" }
  beFail checks if an element is Fail(_)
  ${ Fail(1) must beFail(1) }
  ${ Fail(1) must beFail(===(1)) }
  ${ Fail(1) must beFail.like { case i => i must be_>(0) } } """

}