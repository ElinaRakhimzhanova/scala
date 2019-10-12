package week3.tasks

import week3.tasks.recursion.{End,  Node}
//import week3.tasks.recursion.{GenericEnd, GenericList, GenericNode}

object TestList extends App {

  val intList1 = Node(1, Node(2, Node(3, Node(4, End))))

  assert(intList1.length == 4)
  assert(intList1.tail.length == 3)
  assert(End.length == 0)


  val intList2 = Node(1, Node(2, Node(3, Node(4, End))))

  assert(intList2.product == 1 * 2 * 3 * 4)
  assert(intList2.tail.product == 2 * 3 * 4)
  assert(End.product == 1)


  val intList3 = Node(1, Node(2, Node(3, Node(4, End))))

  assert(intList3.double == Node(1 * 2, Node(2 * 2, Node(3 * 2, Node(4 * 2, End)))))
  assert(intList3.tail.double == Node(4, Node(6, Node(8, End))))
  assert(End.double == End)

  val intList4 = Node(1, Node(2, Node(3, Node(4, End))))

  assert(intList4.map(x => x * 3) == Node(1 * 3, Node(2 * 3, Node(3 * 3, Node(4 * 3, End)))))
  assert(intList4.map(x => 5 - x) == Node(5 - 1, Node(5 - 2, Node(5 - 3, Node(5 - 4, End)))))

  println(Node(1 * 3, Node(2 * 3, Node(3 * 3, Node(4 * 3, End)))))
  println(intList4.map(x => x * 3))

  /*val genericList: GenericList[Int] = GenericNode(1, GenericNode(2, GenericNode(3, GenericEnd())))

  assert(genericList.map(x => x + 8) == GenericNode(1 + 8, GenericNode(2 + 8, GenericNode(3 + 8, GenericEnd()))))
  assert(genericList.map(x => x.toString) == GenericNode("1", GenericNode("2", GenericNode("3", GenericEnd()))))
*/
}

