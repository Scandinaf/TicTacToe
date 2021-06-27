package com.tictactoe.service.game.classic.freemonad

import cats.data.StateT
import cats.syntax.either._
import cats.syntax.option._
import cats.~>
import com.tictactoe.service.game.classic.CompanionOps._
import com.tictactoe.service.game.classic.OrderingOps._
import com.tictactoe.service.game.classic.exception.{
  CellAlreadyOccupiedException,
  GameException,
  PositionOutboundException
}
import com.tictactoe.service.game.classic.model.CellType.PlayerCellType
import com.tictactoe.service.game.classic.model.GameState.{ActiveGame, FinishedGame, TurnNumber}
import com.tictactoe.service.game.classic.model.{CellType, GameRules}

object PureCompiler {

  type Result[A] = Either[GameException, A]
  type Context = (GameRules, ActiveGame)
  type ClassicTicTacToeFreeState[A] = StateT[Result, Context, A]
  val pureCompiler: ClassicTicTacToe ~> ClassicTicTacToeFreeState =
    new (ClassicTicTacToe ~> ClassicTicTacToeFreeState) {

      override def apply[A](fa: ClassicTicTacToe[A]): ClassicTicTacToeFreeState[A] =
        fa match {
          case ClassicTicTacToe.CheckPositionOutbound(position) =>
            StateT.inspectF {
              case (gameRules, _) =>
                if (position.isOutbound(gameRules))
                  PositionOutboundException(position, gameRules).asLeft
                else
                  Either.unit
            }

          case ClassicTicTacToe.CalculatePlayground(position) =>
            StateT.inspectF {
              case (_, activeGame) =>
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

          case ClassicTicTacToe.CheckWinCondition(position) =>
            StateT.inspect {
              case (gameRules, activeGame) =>
                if (activeGame.turnNumber.value >= gameRules.determineWinnerTurnNumber)
                  WinConditionChecker(gameRules, activeGame)
                    .check(position)
                else
                  Option.empty
            }

          case ClassicTicTacToe.CalculateTurnType() =>
            StateT.inspect {
              case (_, activeGame) => activeGame.turnType match {
                  case PlayerCellType.TicCell =>
                    PlayerCellType.TacCell
                  case PlayerCellType.TacCell =>
                    PlayerCellType.TicCell
                }
            }

          case ClassicTicTacToe.CalculateTurnNumber() =>
            StateT.inspect {
              case (_, activeGame) => TurnNumber(activeGame.turnNumber.value + 1)
            }

          case ClassicTicTacToe.CalculateGameState(playground, maybeCombination, turnType, turnNumber) =>
            StateT.inspect {
              case (gameRules, activeGame) =>
                maybeCombination match {

                  case Some(winingCombination) =>
                    FinishedGame(
                      turnNumber = activeGame.turnNumber,
                      turnType = activeGame.turnType,
                      playground = playground,
                      winningCombination = winingCombination.sorted.some
                    )

                  case None if activeGame.turnNumber.value < gameRules.finalPlaygroundSize =>
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
}
