package com.tictactoe.service.game.classic.model

import com.tictactoe.service.game.classic.model.CellType.PlayerCellType

sealed trait GameState

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
