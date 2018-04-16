package uscala.collection

case class Headed[T](head: T, tail: List[T]) {

  val asList: List[Any] = head :: tail

}

object Headed {

  implicit class ListHeaded[T](list: List[T]) {
    /** will return Some(Headed(list.head, list.tail)) if the list is not empty or None otherwise */
    def toHeaded: Option[Headed[T]] = list match {
      case x :: xs => Some(Headed(x, xs))
      case Nil => None
    }
  }


}
