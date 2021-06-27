package com.tictactoe.service.game.classic.freemonad

import cats.free.Free
import cats.free.Free.liftF
import com.tictactoe.service.game.classic.model.CellType.PlayerCellType
import com.tictactoe.service.game.classic.model.GameState.{Playground, TurnNumber, WinningCombination}
import com.tictactoe.service.game.classic.model.{GameState, Position}

sealed trait ClassicTicTacToe[A]

object ClassicTicTacToe {

  type ClassicTicTacToeFree[A] = Free[ClassicTicTacToe, A]

  final case class CheckPositionOutbound(position: Position) extends ClassicTicTacToe[Unit]

  def checkPositionOutbound(position: Position): ClassicTicTacToeFree[Unit] =
    liftF[ClassicTicTacToe, Unit](CheckPositionOutbound(position))

  final case class CalculatePlayground(position: Position) extends ClassicTicTacToe[Playground]

  def calculatePlayground(position: Position): ClassicTicTacToeFree[Playground] =
    liftF[ClassicTicTacToe, Playground](CalculatePlayground(position))

  final case class CheckWinCondition(position: Position) extends ClassicTicTacToe[Option[WinningCombination]]

  def checkWinCondition(position: Position): ClassicTicTacToeFree[Option[WinningCombination]] =
    liftF[ClassicTicTacToe, Option[WinningCombination]](CheckWinCondition(position))

  final case class CalculateTurnType() extends ClassicTicTacToe[PlayerCellType]

  def calculateTurnType(): ClassicTicTacToeFree[PlayerCellType] =
    liftF[ClassicTicTacToe, PlayerCellType](CalculateTurnType())

  final case class CalculateTurnNumber() extends ClassicTicTacToe[TurnNumber]

  def calculateTurnNumber(): ClassicTicTacToeFree[TurnNumber] =
    liftF[ClassicTicTacToe, TurnNumber](CalculateTurnNumber())

  final case class CalculateGameState(
    playground: Playground,
    maybeCombination: Option[WinningCombination],
    turnType: PlayerCellType,
    turnNumber: TurnNumber
  ) extends ClassicTicTacToe[GameState]

  def calculateGameState(
    playground: Playground,
    maybeCombination: Option[WinningCombination],
    turnType: PlayerCellType,
    turnNumber: TurnNumber
  ): ClassicTicTacToeFree[GameState] =
    liftF[ClassicTicTacToe, GameState](CalculateGameState(playground, maybeCombination, turnType, turnNumber))
}
