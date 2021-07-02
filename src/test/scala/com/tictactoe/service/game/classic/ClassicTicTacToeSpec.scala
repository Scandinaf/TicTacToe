package com.tictactoe.service.game.classic

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.option._
import com.tictactoe.model.CellType.EmptyCell
import com.tictactoe.model.CellType.PlayerCellType.{TacCell, TicCell}
import com.tictactoe.model.Position
import com.tictactoe.model.Position.{Column, Row}
import com.tictactoe.service.game.classic.exception.GameException
import com.tictactoe.service.game.classic.exception.GameException.{
  CellAlreadyOccupiedException,
  PositionOutboundException
}
import com.tictactoe.service.game.classic.model.GameRules
import com.tictactoe.service.game.classic.model.GameRules.{ColumnCount, RowCount, WinningCombinationLength}
import com.tictactoe.service.game.classic.model.GameState.{FinishedGame, TurnNumber}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class ClassicTicTacToeSpec extends AnyFlatSpec with Matchers {

  "ClassicTicTacToe" should "throw PositionOutboundException" in new Scope {
    val gameRules: GameRules = GameRules(
      columnCount = ColumnCount(3),
      rowCount = RowCount(3),
      winningCombinationLength = WinningCombinationLength(3)
    )
    val inboundPosition: Position = Position(column = Column(-5), row = Row(53))

    classicTicTacToe.makeTurn(inboundPosition).value.unsafeRunSync() shouldBe
      PositionOutboundException(inboundPosition, gameRules).asLeft
  }

  it should "throw CellAlreadyOccupiedException" in new Scope {
    val position: Position = Position(column = Column(1), row = Row(1))

    (
      for {
        classicTicTacToe <- classicTicTacToe.makeTurn(position).rethrowT
        classicTicTacToe <- classicTicTacToe.makeTurn(position).value
        _ = classicTicTacToe shouldBe CellAlreadyOccupiedException(position).asLeft
      } yield ()
    ).unsafeRunSync()
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

    assertThrows[GameException](
      (
        for {
          classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
          classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(1)))
          classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
          classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
          classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
          _ = classicTicTacToe.state shouldBe gameState
          _ <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
        } yield ()
      ).rethrowT
        .unsafeRunSync()
    )
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

    (
      for {
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(3)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(2)))
        _ = classicTicTacToe.state shouldBe gameState
      } yield ()
    ).rethrowT
      .unsafeRunSync()
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

    (
      for {
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(3)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
        _ = classicTicTacToe.state shouldBe gameState
      } yield ()
    ).rethrowT
      .unsafeRunSync()
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

    (
      for {
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(3)))
        _ = classicTicTacToe.state shouldBe gameState
      } yield ()
    ).rethrowT
      .unsafeRunSync()
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

    (
      for {
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(3), row = Row(1)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(2)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(2), row = Row(3)))
        classicTicTacToe <- classicTicTacToe.makeTurn(Position(column = Column(1), row = Row(3)))
        _ = classicTicTacToe.state shouldBe gameState
      } yield ()
    ).rethrowT
      .unsafeRunSync()
  }

  trait Scope {

    val classicTicTacToe: ClassicTicTacToe[IO] = ClassicTicTacToe[IO]()
  }
}
