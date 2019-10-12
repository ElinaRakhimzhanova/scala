package week3.tasks

case class Square(side: Int) extends Rectangular {

  override def getSides(): Int = sides

  override def area(): Int = side*side

  override def perimeter(): Int = 4*side
}
