package minotaur.model

sealed trait Move {
  val gameState: GameState
  val priority: Double
  val play: GameState
  val wins: Boolean
  val isValid: Boolean
}

case class WallPlacement(
  wall: Wall,
  gameState: GameState
) extends Move {
  private val gs = gameState
  val priority = 1.0

  override def toString = s"(WallPlacement: $wall)"

  lazy val play: GameState = {
    // blocked movements
    val movementUpdates: Seq[(Location, Seq[Direction])] =
      wall.blocksMovement.map {
        case (location, direction) => (
          location,
          gs.board.allowedMovements(location).filterNot(_ == direction)
        )
      }

    // make paths potential
    val paths = gs.board.shortestPath.map {
      case (player, Some(path)) => player -> Some(path.potentialize)
      case (player, None) => player -> None
    }

    gs.copy(
      board = gs.board.copy(
        walls = gs.board.walls + wall,
        placeableWalls = gs.board.placeableWalls - wall -- wall.overlaps,
        allowedMovements = gs.board.allowedMovements ++ movementUpdates
      )(cachedPaths = paths),
      walls = gs.walls + (gs.onTurn -> (gs.walls(gs.onTurn) - 1)),
      onTurn = gs.onTurn.other
    )
  }

  // make sure each player can reach their goal
  lazy val isValid: Boolean = play.board.isValid

  val wins = false
}

case class PawnMovement(
  location: Location,
  gameState: GameState
) extends Move {
  private val gs = gameState
  val priority = 2.0

  override def toString = s"(PawnMovement: ${gs.onTurn} to $location)"

  lazy val play: GameState = {
    // update paths with current advance if following
    val paths = gs.board.shortestPath + (
      gs.onTurn -> gs.board.shortestPath(gs.onTurn)
        .filter(_.startsWith(location)).map(_.forward)
    )

    gs.copy(
      board = gs.board.copy(
        pawns = gs.board.pawns
          - gs.board.pawnLocation(gs.onTurn)
          + (location -> gs.onTurn)
      )(cachedPaths = paths),
      onTurn = gs.onTurn.other
    )
  }

  val isValid = true

  lazy val wins: Boolean = {
    val player = gs.onTurn

    play.board.pawnLocation(player).isBorder(player.destination)
  }
}
