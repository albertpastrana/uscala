package uscala.collection

case class Headed[T](head: T, tail: List[T]) {

  val asList: List[T] = head :: tail

}

object Headed {

  /** will return Some(Headed(list.head, list.tail)) if the list is not empty or None otherwise */
  def fromList[T](list: List[T]): Option[Headed[T]] = list match {
    case x :: xs => Some(Headed(x, xs))
    case Nil => None
  }

  implicit class ListHeaded[T](list: List[T]) {
    /** will return Some(Headed(list.head, list.tail)) if the list is not empty or None otherwise */
    def toHeaded: Option[Headed[T]] = Headed.fromList(list)
  }


}
