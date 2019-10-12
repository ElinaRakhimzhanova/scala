package week3.tasks.algebraic

case class Water(size: Int, source: Source, carbonated: Boolean) {

}

trait Source {
//  def well();
//  def spring();
//  def tap();
}

case class Well() extends Source

