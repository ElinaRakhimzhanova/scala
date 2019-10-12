package week3.tasks

object Draw extends App {

  def apply(shape: Shape): String = shape match {
      case r: Rectangle => s"A rectangle of width ${r.side1} cm and height ${r.side2} cm"
      case s: Square => s"A square of side ${s.side} cm"
      case c: Circle => s"A circle of radius ${c.radius} cm"
    }

  println(Draw(Circle(10)))

  println(Draw(Rectangle(3, 4)))

  println(Draw(Square(5)))

}

