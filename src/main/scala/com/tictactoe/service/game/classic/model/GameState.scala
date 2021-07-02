package com.tictactoe.service.game.classic.model

import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.{CellType, Position}

sealed trait GameState {

  def turnType: PlayerCellType
}

object GameState {

  type Playground = List[List[CellType]]
  type WinningCombination = List[Position]

  final case class TurnNumber(value: Int) extends AnyVal

  final case class ActiveGame(
    turnNumber: TurnNumber,
    turnType: PlayerCellType,
    playground: Playground
  ) extends GameState

  final case class FinishedGame(
    turnNumber: TurnNumber,
    turnType: PlayerCellType,
    playground: Playground,
    winningCombination: Option[WinningCombination]
  ) extends GameState
}
