package week3.tasks

case class Rectangle(side1: Int, side2: Int) extends Rectangular {

  override def getSides(): Int = sides

  override def area(): Int = side1*side2

  override def perimeter(): Int = (side1+side2)*2

}
