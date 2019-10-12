package week3.tasks

sealed trait Shape {

  def getSides(): Int

  def area(): Int

  def perimeter(): Int

}

case class Circle(radius: Int) extends Shape {

  override def getSides(): Int = radius

  override def area(): Int = (radius*radius*math.Pi).toInt

  override def perimeter(): Int = (2*math.Pi*radius).toInt

}

trait Rectangular extends Shape {

  val sides = 4

}
