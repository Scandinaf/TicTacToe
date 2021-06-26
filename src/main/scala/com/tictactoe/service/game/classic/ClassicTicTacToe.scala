package com.tictactoe.service.game.classic

import cats.Applicative
import cats.data.StateT
import cats.syntax.either._
import cats.syntax.option._
import com.tictactoe.service.game.classic.CompanionOps._
import com.tictactoe.service.game.classic.OrderingOps._
import com.tictactoe.service.game.classic.exception.{
  CellAlreadyOccupiedException,
  GameAlreadyFinishedException,
  GameException,
  PositionOutboundException
}
import com.tictactoe.service.game.classic.model.CellType.PlayerCellType.TicCell
import com.tictactoe.service.game.classic.model.CellType.{EmptyCell, PlayerCellType}
import com.tictactoe.service.game.classic.model.GameRules.{ColumnCount, RowCount, WinningCombinationLength}
import com.tictactoe.service.game.classic.model.GameState._
import com.tictactoe.service.game.classic.model.Position.{Column, Row}
import com.tictactoe.service.game.classic.model.{CellType, GameRules, GameState, Position}

class ClassicTicTacToe[F[_] : Applicative](
  val rules: GameRules,
  val state: GameState
) {

  type Result[A] = Either[GameException, A]

  def makeTurn(position: Position): F[Result[ClassicTicTacToe[F]]] =
    Applicative[F].pure(
      state match {

        case activeGame: GameState.ActiveGame =>
          (
            for {
              _ <- checkPositionOutbound(position)
              playground <- calculatePlayground(position)
              winingCombination <- checkWinCondition(position, rules)
              turnType <- calculateTurnType()
              turnNumber <- calculateTurnNumber()
              gameState <- calculateGameState(playground, winingCombination, turnType, turnNumber)
            } yield gameState
          ).run(activeGame).map {
            case (_, newState) =>
              new ClassicTicTacToe[F](rules, newState)
          }

        case finishedGame: GameState.FinishedGame =>
          GameAlreadyFinishedException(finishedGame).asLeft
      }
    )

  protected def checkPositionOutbound(position: Position): StateT[Result, ActiveGame, Unit] =
    StateT.inspectF {
      _ =>
        if (position.isOutbound(rules))
          PositionOutboundException(position, rules).asLeft
        else
          Either.unit
    }

  protected def calculatePlayground(position: Position): StateT[Result, ActiveGame, Playground] =
    StateT.inspectF {
      activeGame =>
        val column = activeGame.playground(position.column.value - 1)
        column(position.row.value - 1) match {

          case CellType.EmptyCell =>
            val updatedColumn = column.updated(position.row.value - 1, activeGame.turnType)
            activeGame.playground
              .updated(position.column.value - 1, updatedColumn)
              .asRight

          case _ =>
            CellAlreadyOccupiedException(position).asLeft
        }
    }

  protected def checkWinCondition(
    position: Position,
    rules: GameRules
  ): StateT[Result, ActiveGame, Option[WinningCombination]] = {

    def checkUpDown(offsets: List[Int], activeGame: ActiveGame): List[Position] = {
      collectAdjacentElement(
        offsets,
        activeGame,
        offset => Position(Column(position.column.value), Row(position.row.value + offset))
      ) :::
        collectAdjacentElement(
          offsets,
          activeGame,
          offset => Position(Column(position.column.value), Row(position.row.value - offset))
        )
    }

    def checkLeftRight(offsets: List[Int], activeGame: ActiveGame): List[Position] = {
      collectAdjacentElement(
        offsets,
        activeGame,
        offset => Position(Column(position.column.value - offset), Row(position.row.value))
      ) :::
        collectAdjacentElement(
          offsets,
          activeGame,
          offset => Position(Column(position.column.value + offset), Row(position.row.value))
        )
    }

    def checkLeftDiagonal(offsets: List[Int], activeGame: ActiveGame): List[Position] = {
      collectAdjacentElement(
        offsets,
        activeGame,
        offset => Position(Column(position.column.value - offset), Row(position.row.value + offset))
      ) :::
        collectAdjacentElement(
          offsets,
          activeGame,
          offset => Position(Column(position.column.value + offset), Row(position.row.value - offset))
        )
    }

    def checkRightDiagonal(offsets: List[Int], activeGame: ActiveGame): List[Position] = {
      collectAdjacentElement(
        offsets,
        activeGame,
        offset => Position(Column(position.column.value + offset), Row(position.row.value + offset))
      ) :::
        collectAdjacentElement(
          offsets,
          activeGame,
          offset => Position(Column(position.column.value - offset), Row(position.row.value - offset))
        )
    }

    def checkWinningCombinationLength(combination: List[Position]): Option[WinningCombination] =
      Option.when(combination.length >= (rules.winningCombinationLength.value - 1))(combination)

    def collectAdjacentElement(
      offsets: List[Int],
      activeGame: ActiveGame,
      buildPositionF: Int => Position
    ): List[Position] =
      offsets.map(offset => {
        val checkPosition = buildPositionF(offset)
        Option.when(checkCellType(checkPosition, activeGame))(checkPosition)
      }).takeWhile(_.isDefined).flatten

    def checkCellType(position: Position, activeGame: ActiveGame): Boolean =
      !position.isOutbound(rules) &&
        activeGame.playground(position.column.value - 1)(position.row.value - 1) == activeGame.turnType

    StateT.inspect {
      activeGame =>
        if (activeGame.turnNumber.value >= (rules.winningCombinationLength.value * 2) - 1) {
          val offsets = (1 until rules.winningCombinationLength.value).toList
          checkWinningCombinationLength(checkUpDown(offsets, activeGame))
            .orElse(checkWinningCombinationLength(checkLeftRight(offsets, activeGame)))
            .orElse(checkWinningCombinationLength(checkLeftDiagonal(offsets, activeGame)))
            .orElse(checkWinningCombinationLength(checkRightDiagonal(offsets, activeGame)))
            .map(_ :+ position)
        } else
          Option.empty
    }
  }

  protected def calculateTurnType(): StateT[Result, ActiveGame, PlayerCellType] =
    StateT.inspect(_.turnType match {
      case PlayerCellType.TicCell =>
        PlayerCellType.TacCell
      case PlayerCellType.TacCell =>
        PlayerCellType.TicCell
    })

  protected def calculateTurnNumber(): StateT[Result, ActiveGame, TurnNumber] =
    StateT.inspect {
      activeGame => TurnNumber(activeGame.turnNumber.value + 1)
    }

  protected def calculateGameState(
    playground: Playground,
    maybeCombination: Option[WinningCombination],
    turnType: PlayerCellType,
    turnNumber: TurnNumber
  ): StateT[Result, ActiveGame, GameState] = {
    StateT.inspect {
      activeGame: ActiveGame =>
        maybeCombination match {

          case Some(winingCombination) =>
            FinishedGame(
              turnNumber = activeGame.turnNumber,
              turnType = activeGame.turnType,
              playground = playground,
              winningCombination = winingCombination.sorted.some
            )

          case None if activeGame.turnNumber.value < (rules.finalPlaygroundSize) =>
            ActiveGame(turnNumber = turnNumber, turnType = turnType, playground = playground)

          case _ =>
            FinishedGame(
              turnNumber = activeGame.turnNumber,
              turnType = activeGame.turnType,
              playground = playground,
              winningCombination = None
            )
        }
    }
  }
}

object ClassicTicTacToe {

  def apply[F[_] : Applicative](): ClassicTicTacToe[F] = {

    val gameRules = GameRules(
      columnCount = ColumnCount(3),
      rowCount = RowCount(3),
      winningCombinationLength = WinningCombinationLength(3)
    )

    val state = ActiveGame(
      turnNumber = TurnNumber(1),
      turnType = TicCell,
      playground = List.fill(gameRules.columnCount.value, gameRules.rowCount.value)(EmptyCell)
    )

    new ClassicTicTacToe[F](gameRules, state)
  }
}
