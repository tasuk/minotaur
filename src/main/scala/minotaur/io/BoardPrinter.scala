package minotaur.io

import minotaur.model._

object BoardPrinter {
  private def printWithCellContent(
    board: Board,
    cellContent: Option[Location] => String
  ) = {
    val boardType = board.boardType

    def getOptionalLocation(position: Int): Option[Location] =
      Option(position)
        .filter(boardType.containsLocation)
        .map(Location(_, boardType))

    def shouldPrintWall(
      location: Option[Location],
      direction: Direction
    ): Boolean =
      location
        .filter(l => !l.isBorder(direction) && !board.canMove(l, direction))
        .forall(_ => false)

    val oddLines = (-1 until board.size)
      .map(row => (0 until board.size).map(column => {
        val optLoc = getOptionalLocation(row*board.size + column)
        if (shouldPrintWall(optLoc, South)) "+   "
        else "+---"
      }).mkString + "+")

    val evenLines = (0 until board.size)
      .map(row => (0 until board.size).map(column => {
        val optLoc = getOptionalLocation(row*board.size + column)
        val side = if (shouldPrintWall(optLoc, West)) " " else "|"

        side + cellContent(optLoc)
      }).mkString.replaceAll("""\s+$""",""))

    List(oddLines, evenLines).flatMap(_.zipWithIndex)
      .sortBy(_._2).map(_._1).mkString("\n") + "\n"
  }

  def print(board: Board): String =
    "\n" + printWithCellContent(board, (optLoc: Option[Location]) =>
      board.pawns
        .find { case(location, _) => optLoc.contains(location) }
        .map { case(_, player) => s" ${player.pawn} " }
        .getOrElse("   ")
    )

  def printWithCoords(board: Board): String = {
    val boardSize = board.boardType.size
    val coordinates = Coordinates(board.boardType)

    val padTo = boardSize * 4 + 1
    val lines: List[String] = print(board).trim.split("\\n").toList.map(
      (line) => String.format("%1$-" + padTo + "s", line)
    )

    val numberedBoard = lines.zipWithIndex.map{ case (line, index) =>
      val coord: Char =
        if (index % 2 == 0 && index != 0 && index != boardSize * 2)
          coordinates.horizontal(index / 2 - 1)
        else
          ' '

      def getDirectionLabel(label: String): Char =
        if (index > boardSize - 3 && index < boardSize + 2)
          label.toList(index - (boardSize - 2))
        else
          ' '

      val assembled = getDirectionLabel("WEST") + "  " +
        coord + " " + line + "  " +
        getDirectionLabel("EAST")

      assembled.replaceAll("""\s+$""","")
    }.mkString("\n")

    val center = List.fill(5 + boardSize*2 - 2)(" ").mkString
    val before = center + "NORTH\n\n      " +
      coordinates.vertical.take(boardSize - 1).map("   " + _).mkString + "\n"
    val after = "\n\n" + center + "SOUTH\n"

    "\n" + before + numberedBoard + after
  }

  def printSearchNodes[SN <: SearchNode](board: Board, nodes: Set[SN]): String =
    printWithCellContent(board, (optLoc: Option[Location]) =>
      optLoc.flatMap(loc => nodes.find(_.location == loc))
        .map(n => f"${n.cost}%2d ")
        .getOrElse("   ")
    )
}
