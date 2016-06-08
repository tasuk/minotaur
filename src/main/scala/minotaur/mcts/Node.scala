package minotaur.mcts

import Math.{sqrt,log}
import scala.collection.mutable.ListBuffer
import util.Random

import minotaur.model.{GameState,Move,Player}

trait Node {
  val gameState: GameState
  val parent: Option[Node]
  val wins: Boolean

  override def toString =
    f"${gameState.onTurn.other} " +
    f"confidence: ${winCount.toDouble/visited}%1.2f ($winCount / $visited)"

  override lazy val hashCode = gameState.hashCode

  var winCount = 0
  var visited = 0

  private lazy val unexploredChildren: Iterator[MoveNode] =
    Random.shuffle(gameState.getPossibleMoves).toIterator
      .filter(_.isValid)
      .map(new MoveNode(_, this))

  def isFullyExplored: Boolean =
    unexploredChildren.isEmpty

  val children: ListBuffer[MoveNode] = ListBuffer[MoveNode]()

  def selectChild: MoveNode =
    children.maxBy(_.UCT)

  def expand: MoveNode = {
    val next = unexploredChildren.next
    children += next
    next
  }

  def update(winner: Player): Unit = {
    visited += 1
    if (winner == gameState.onTurn.other) winCount += 1
  }
}

class RootNode(gs: GameState) extends Node {
  val gameState = gs
  val parent = None
  val wins = false
}

class MoveNode(val move: Move, parentNode: Node) extends Node {
  val gameState = move.play
  val parent = Some(parentNode)
  val wins = move.wins

  def UCT: Double = {
    (winCount.toDouble / visited) +
      sqrt(log(parentNode.visited.toDouble) / visited)
  }
}
