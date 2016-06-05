package profile

import minotaur.model.{BoardType,Location,Wall,Horizontal}
import minotaur.model.{North,South,East,West}
import minotaur.model.{White,Black}
import minotaur.search.{AStar,BFS}
import minotaur.io.BoardReader

object Benchmark {
  val bt = BoardType(9)

  def main(args: Array[String]): Unit = {
    profileBoardWithLocations
    profileContains
    mapCaseClassVsListId
    searches
  }

  def profileBoardWithLocations = {
    val locationList: List[Location] = (for(i <- 0 to 80) yield Location(i, bt)).toList
    val locationVector: Vector[Location] = locationList.toVector

    Profiler.clear
    Profiler.profileMany("List access (2)", locationList(2))
    Profiler.profileMany("List access (73)", locationList(73))

    Profiler.profileMany("Vector access (2)", locationVector(2))
    Profiler.profileMany("Vector access (73)", locationVector(73)) // this is bad
    Profiler.printShort
  }

  def profileContains = {
    val wallSet: Set[Wall] = bt.possibleWalls.toSet
    val wallList: List[Wall] = wallSet.toList.sortBy(_.location.location)
    val wallVector: Vector[Wall] = wallList.toVector
    val wall = Wall(Location(40, bt), Horizontal)

    Profiler.clear
    Profiler.profileMany("Set contains", wallSet contains wall)
    Profiler.profileMany("List contains", wallList contains wall) // not so bad
    Profiler.profileMany("Vector contains", wallVector contains wall) // this is bad
    Profiler.printShort
  }

  def mapCaseClassVsListId = {
    val mapCaseClass = Map(
      North -> Location(31, bt),
      South -> Location(49, bt),
      East -> Location(41, bt),
      West -> Location(39, bt)
    )
    val listInts = List(
      Location(31, bt),
      Location(49, bt),
      Location(41, bt),
      Location(39, bt)
    )
    val vectorInts = Vector(
      Location(31, bt),
      Location(49, bt),
      Location(41, bt),
      Location(39, bt)
    )

    Profiler.clear
    Profiler.profileMany("Directions case class", {
      mapCaseClass(South)
      mapCaseClass(West)
    }) // about 4 times slower that list/vector
    Profiler.profileMany("Directions list ints", {
      listInts(1)
      listInts(3)
    })
    Profiler.profileMany("Directions vector ints", {
      vectorInts(1)
      vectorInts(3)
    })
    Profiler.printShort
  }

  def searches = {
    val file = "src/test/resources/board.txt"
    val board = BoardReader.fromFile(file)

    Profiler.clear
    Map("A*" -> AStar, "BFS" -> BFS).foreach( _ match {
      case (name, algo) =>
        Profiler.profileMany(name, 10000, {
          algo.findPath(board, board.pawnLocation(White), South)
          algo.findPath(board, board.pawnLocation(Black), North)
        })
    })
    Profiler.printShort
  }
}
