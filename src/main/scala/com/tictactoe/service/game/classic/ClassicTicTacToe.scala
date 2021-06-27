package com.tictactoe.service.game.classic

import cats.Applicative
import cats.syntax.either._
import com.tictactoe.service.game.classic.exception.{GameAlreadyFinishedException, GameException}
import com.tictactoe.service.game.classic.freemonad.ClassicTicTacToe._
import com.tictactoe.service.game.classic.freemonad.PureCompiler.pureCompiler
import com.tictactoe.service.game.classic.model.CellType.EmptyCell
import com.tictactoe.service.game.classic.model.CellType.PlayerCellType.TicCell
import com.tictactoe.service.game.classic.model.GameRules.{ColumnCount, RowCount, WinningCombinationLength}
import com.tictactoe.service.game.classic.model.GameState._
import com.tictactoe.service.game.classic.model.{GameRules, GameState, Position}

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
              winingCombination <- checkWinCondition(position)
              turnType <- calculateTurnType()
              turnNumber <- calculateTurnNumber()
              gameState <- calculateGameState(playground, winingCombination, turnType, turnNumber)
            } yield gameState
          ).foldMap(pureCompiler)
            .run((rules, activeGame)).map {
              case (_, newState) =>
                new ClassicTicTacToe[F](rules, newState)
            }

        case finishedGame: GameState.FinishedGame =>
          GameAlreadyFinishedException(finishedGame).asLeft
      }
    )
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
