package minotaur.io

import minotaur.model.{Board,Location}
import minotaur.model.{Direction,South,West}
import minotaur.model.SearchNode

object BoardPrinter {
  private def printWithCellContent(
    board: Board,
    cellContent: Option[Location] => String
  ) = {
    val boardType = board.boardType

    def getOptionalLocation(position: Int): Option[Location] =
      Option(position)
        .filter(boardType.containsLocation(_))
        .map(Location(_, boardType))

    def shouldPrintWall(
      location: Option[Location],
      direction: Direction
    ): Boolean =
      location
        .filter(l => !l.isBorder(direction) && !board.canMove(l, direction))
        .map(l => false).getOrElse(true)

    val oddLines = (-1 to board.size - 1)
      .map(row => (0 to board.size - 1).map(column => {
        val optLoc = getOptionalLocation(row*board.size + column)
        if (shouldPrintWall(optLoc, South)) "+   "
        else "+---"
      }).mkString + "+")

    val evenLines = (0 to board.size - 1)
      .map(row => (0 to board.size - 1).map(column => {
        val optLoc = getOptionalLocation(row*board.size + column)
        val side = if (shouldPrintWall(optLoc, West)) " " else "|"

        side + cellContent(optLoc)
      }).mkString.replaceAll("""\s+$""",""))

    List(oddLines, evenLines).flatMap(_.zipWithIndex)
      .sortBy(_._2).map(_._1).mkString("\n") + "\n"
  }

  def print(board: Board): String =
    printWithCellContent(board, (optLoc: Option[Location]) =>
      board.pawns
        .find { case(location, _) => optLoc == Some(location) }
        .map { case(_, player) => s" ${player.pawn} " }
        .getOrElse("   ")
    )

  def printSearchNodes(board: Board, nodes: Set[SearchNode]): String =
    printWithCellContent(board, (optLoc: Option[Location]) =>
      optLoc.flatMap(loc => nodes.find(_.location == loc))
        .map(n => f"${n.cost}%2d ")
        .getOrElse("   ")
    )
}
