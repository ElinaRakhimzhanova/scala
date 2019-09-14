package week3.model


// Algebraic Data Type: Sum type
sealed trait LinkedList

case class Node(value: Int, var tail: LinkedList) extends LinkedList {
  def append(num: Int): Unit = tail match {
    case node: Node =>
      node.append(num)
    case LinkedListNil =>
      tail = Node(num, LinkedListNil)
  }

  def find(elem: Int): LinkedList =
    if (value == elem) this
    else {
      tail match {
        case node: Node => node.find(elem)
        case LinkedListNil => LinkedListNil
    }
  }
}
case object LinkedListNil extends LinkedList


// head => tail (LinkedList)
