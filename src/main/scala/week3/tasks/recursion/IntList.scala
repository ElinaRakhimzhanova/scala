package week3.tasks.recursion

sealed trait IntList {

  def length: Int = {
    def len(intList: IntList, accumulator: Int = 0): Int = intList match {
      case node: Node => len(node.tail, accumulator + 1)
      case End => accumulator
    }
    len(intList = this)
  }

  def product: Int = {
    def prod(intList: IntList, accumulator: Int = 1): Int = intList match {
      case node: Node => prod(node.tail, accumulator * node.head)
      case End => accumulator
    }
    prod(this)
  }

  def double: IntList = {
    def doubleList(intList: IntList): IntList = intList match {
      case End => End
      case node: Node => Node(node.head * 2, doubleList(node.tail))
    }
    doubleList(this)
  }

  def map(f: Int => Int): IntList = {
    def innerMap(intList: IntList) : IntList = intList match {
      case End => End;
      case node: Node => Node(f(node.head), innerMap(node.tail));
    }
    innerMap(this)
  }


}

case object End extends IntList
case class Node(head: Int, tail: IntList) extends IntList