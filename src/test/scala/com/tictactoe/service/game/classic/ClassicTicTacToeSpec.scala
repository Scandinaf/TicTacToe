package com.tictactoe.service.game.classic

import cats.Id
import com.tictactoe.service.game.classic.exception.{
  CellAlreadyOccupiedException,
  GameAlreadyFinishedException,
  PositionOutboundException
}
import com.tictactoe.service.game.classic.model.GameRules.{ColumnCount, RowCount, WinningCombinationLength}
import com.tictactoe.service.game.classic.model.{GameRules, Position}
import com.tictactoe.service.game.classic.model.Position.{Column, Row}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.syntax.either._
import cats.syntax.option._
import com.tictactoe.service.game.classic.model.CellType.EmptyCell
import com.tictactoe.service.game.classic.model.CellType.PlayerCellType.{TacCell, TicCell}
import com.tictactoe.service.game.classic.model.GameState.{FinishedGame, TurnNumber}

class ClassicTicTacToeSpec extends AnyFlatSpec with Matchers {

  "ClassicTicTacToe" should "throw PositionOutboundException" in new Scope {
    val gameRules: GameRules = GameRules(
      columnCount = ColumnCount(3),
      rowCount = RowCount(3),
      winningCombinationLength = WinningCombinationLength(3)
    )
    val inboundPosition: Position = Position(column = Column(-5), row = Row(53))

    classicTicTacToe.makeTurn(inboundPosition) shouldBe
      PositionOutboundException(inboundPosition, gameRules).asLeft
  }

  it should "throw CellAlreadyOccupiedException" in new Scope {
    val position: Position = Position(column = Column(1), row = Row(1))

    (for {
      classicTicTacToe <- classicTicTacToe.makeTurn(position)
      classicTicTacToe <- classicTicTacToe.makeTurn(position)
    } yield classicTicTacToe) shouldBe CellAlreadyOccupiedException(position).asLeft
  }

  it should "throw GameAlreadyFinishedException" in new Scope {
    val gameState: FinishedGame = FinishedGame(
      turnNumber = TurnNumber(5),
      turnType = TicCell,
      playground = List(
        List.fill(3)(TicCell),
        List.fill(2)(TacCell) :+ EmptyCell,
        List.fill(3)(EmptyCell)
      ),
      winningCombination = List(
        Position(column = Column(1), row = Row(1)),
        Position(column = Column(1), row = Row(2)),
        Position(column = Column(1), row = Row(3))
      ).some
    )

    (for {
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
    } yield classicTicTacToe) shouldBe GameAlreadyFinishedException(gameState).asLeft
  }

  it should "correctly handle the draw" in new Scope {
    val gameState: FinishedGame = FinishedGame(
      turnNumber = TurnNumber(9),
      turnType = TicCell,
      playground = List(
        List.fill(2)(TicCell) :+ TacCell,
        List.fill(2)(TacCell) :+ TicCell,
        List.fill(2)(TicCell) :+ TacCell
      ),
      winningCombination = None
    )

    (for {
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(3)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(2)))
    } yield classicTicTacToe.state) shouldBe gameState.asRight
  }

  it should "correctly handle the win #1" in new Scope {
    val gameState: FinishedGame = FinishedGame(
      turnNumber = TurnNumber(6),
      turnType = TacCell,
      playground = List(
        List.fill(2)(TicCell) :+ EmptyCell,
        List.fill(3)(TacCell),
        List.fill(2)(EmptyCell) :+ TicCell
      ),
      winningCombination = List(
        Position(column = Column(2), row = Row(1)),
        Position(column = Column(2), row = Row(2)),
        Position(column = Column(2), row = Row(3))
      ).some
    )

    (for {
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(3)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
    } yield classicTicTacToe.state) shouldBe gameState.asRight
  }

  it should "correctly handle the win #2" in new Scope {
    val gameState: FinishedGame = FinishedGame(
      turnNumber = TurnNumber(5),
      turnType = TicCell,
      playground = List(
        List(TicCell, TacCell, TacCell),
        List(EmptyCell, TicCell, EmptyCell),
        List(EmptyCell, EmptyCell, TicCell)
      ),
      winningCombination = List(
        Position(column = Column(1), row = Row(1)),
        Position(column = Column(2), row = Row(2)),
        Position(column = Column(3), row = Row(3))
      ).some
    )

    (for {
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(3)))
    } yield classicTicTacToe.state) shouldBe gameState.asRight
  }

  it should "correctly handle the win #3" in new Scope {
    val gameState: FinishedGame = FinishedGame(
      turnNumber = TurnNumber(6),
      turnType = TacCell,
      playground = List(
        List(TicCell, TicCell, TacCell),
        List(EmptyCell, TacCell, TicCell),
        List(TacCell, EmptyCell, EmptyCell)
      ),
      winningCombination = List(
        Position(column = Column(1), row = Row(3)),
        Position(column = Column(2), row = Row(2)),
        Position(column = Column(3), row = Row(1))
      ).some
    )

    (for {
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(1)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
      classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
    } yield classicTicTacToe.state) shouldBe gameState.asRight
  }

  trait Scope {

    val classicTicTacToe: ClassicTicTacToe[Id] = ClassicTicTacToe[Id]()
  }
}
