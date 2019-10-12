/*
package week3.task.recursion


sealed trait GenericList[A] {
  <!-- -->

  def length: Int = {
    def len(genericList: GenericList, accumulator: Int = 0): Int = genericList match {
      case node: GenericNode => len(node.tail, accumulator + 1)
      case GenericEnd => accumulator
    }
    len(this)
  }

  def map[B](f: A => B): GenericList[B] = {
    def innerMap(genericList: GenericList) : GenericList  = genericList match {
      case GenericEnd => GenericEnd;
      case node: GenericNode => GenericNode(f(node.head), innerMap(node.tail));
    }
    innerMap(this)
  }

}

case class GenericEnd() extends GenericList[A]
case class GenericNode(head: Int, tail: GenericList[A]) extends GenericList[A]
*/
