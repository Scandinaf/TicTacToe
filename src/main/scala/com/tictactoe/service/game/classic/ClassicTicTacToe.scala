package com.tictactoe.service.game.classic

import cats.Applicative
import cats.data.EitherT
import cats.syntax.either._
import com.tictactoe.model.CellType.EmptyCell
import com.tictactoe.model.CellType.PlayerCellType.TicCell
import com.tictactoe.model.Position
import com.tictactoe.service.game.classic.exception.GameException
import com.tictactoe.service.game.classic.exception.GameException.GameAlreadyFinishedException
import com.tictactoe.service.game.classic.freemonad.ClassicTicTacToe._
import com.tictactoe.service.game.classic.freemonad.PureCompiler.pureCompiler
import com.tictactoe.service.game.classic.model.GameRules.{ColumnCount, RowCount, WinningCombinationLength}
import com.tictactoe.service.game.classic.model.GameState._
import com.tictactoe.service.game.classic.model.{GameRules, GameState}

class ClassicTicTacToe[F[_] : Applicative](
  val rules: GameRules,
  val state: GameState
) {

  def makeTurn(position: Position): EitherT[F, GameException, ClassicTicTacToe[F]] =
    EitherT.fromEither[F](
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

        case _: GameState.FinishedGame =>
          GameAlreadyFinishedException.asLeft
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
