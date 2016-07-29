package minotaur.model

sealed trait Direction {
  val orientation: Orientation = this match {
    case (North | South) => Vertical
    case (East | West) => Horizontal
  }
}
object Direction {
  def all = Seq(North, South, East, West)
}
case object North extends Direction
case object South extends Direction
case object East extends Direction
case object West extends Direction
