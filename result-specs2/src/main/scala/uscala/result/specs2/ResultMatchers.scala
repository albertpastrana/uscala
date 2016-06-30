package uscala.result.specs2

import org.specs2.matcher.{OptionLikeCheckedMatcher, OptionLikeMatcher, ValueCheck}
import uscala.result.Result

trait ResultMatchers {
  def beOk[T](t: ValueCheck[T]) = OkValidationCheckedMatcher(t)
  def beOk[T] = OkValidationMatcher[T]()

  def beFail[T](t: ValueCheck[T]) = FailValidationCheckedMatcher(t)
  def beFail[T] = FailValidationMatcher[T]()
}

object ResultMatchers extends ResultMatchers

case class OkValidationMatcher[T]() extends OptionLikeMatcher[({type l[a]=Result[_, a]})#l, T, T]("Ok", _.toOption)
case class OkValidationCheckedMatcher[T](check: ValueCheck[T]) extends OptionLikeCheckedMatcher[({type l[a]=Result[_, a]})#l, T, T]("Ok", _.toOption, check)

case class FailValidationMatcher[T]() extends OptionLikeMatcher[({type l[a]=Result[a, _]})#l, T, T]("Fail", _.toEither.left.toOption)
case class FailValidationCheckedMatcher[T](check: ValueCheck[T]) extends OptionLikeCheckedMatcher[({type l[a]=Result[a, _]})#l, T, T]("Fail", _.toEither.left.toOption, check)