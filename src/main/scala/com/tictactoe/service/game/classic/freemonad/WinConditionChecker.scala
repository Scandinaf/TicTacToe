package com.tictactoe.service.game.classic.freemonad

import com.tictactoe.service.game.classic.CompanionOps.PositionCompanion
import com.tictactoe.service.game.classic.model.{GameRules, Position}
import com.tictactoe.service.game.classic.model.GameState.{ActiveGame, WinningCombination}
import com.tictactoe.service.game.classic.model.Position.{Column, Row}

class WinConditionChecker(gameRules: GameRules, activeGame: ActiveGame) {

  def check(position: Position): Option[WinningCombination] = {

    def checkUpDown(position: Position): List[Position] =
      collectAdjacentElement(offset =>
        Position(Column(position.column.value), Row(position.row.value + offset))
      ) :::
        collectAdjacentElement(offset =>
          Position(Column(position.column.value), Row(position.row.value - offset))
        )

    def checkLeftRight(position: Position): List[Position] =
      collectAdjacentElement(offset =>
        Position(Column(position.column.value - offset), Row(position.row.value))
      ) :::
        collectAdjacentElement(offset =>
          Position(Column(position.column.value + offset), Row(position.row.value))
        )

    def checkLeftDiagonal(position: Position): List[Position] =
      collectAdjacentElement(offset =>
        Position(Column(position.column.value - offset), Row(position.row.value + offset))
      ) :::
        collectAdjacentElement(offset =>
          Position(Column(position.column.value + offset), Row(position.row.value - offset))
        )

    def checkRightDiagonal(position: Position): List[Position] =
      collectAdjacentElement(offset =>
        Position(Column(position.column.value + offset), Row(position.row.value + offset))
      ) :::
        collectAdjacentElement(offset =>
          Position(Column(position.column.value - offset), Row(position.row.value - offset))
        )

    def checkAdjacentElementLength(combination: List[Position]): Option[WinningCombination] =
      Option.when(combination.length >= gameRules.adjacentElementNumber)(combination)

    def collectAdjacentElement(buildPositionF: Int => Position): List[Position] =
      gameRules.offsets.map(offset => {
        val checkPosition = buildPositionF(offset)
        Option.when(checkCellType(checkPosition))(checkPosition)
      }).takeWhile(_.isDefined).flatten

    def checkCellType(position: Position): Boolean =
      !position.isOutbound(gameRules) &&
        activeGame.playground(position.column.value - 1)(
          position.row.value - 1
        ) == activeGame.turnType

    checkAdjacentElementLength(checkUpDown(position))
      .orElse(checkAdjacentElementLength(checkLeftRight(position)))
      .orElse(checkAdjacentElementLength(checkLeftDiagonal(position)))
      .orElse(checkAdjacentElementLength(checkRightDiagonal(position)))
      .map(_ :+ position)
  }
}

object WinConditionChecker {

  def apply(gameRules: GameRules, activeGame: ActiveGame): WinConditionChecker =
    new WinConditionChecker(gameRules, activeGame)
}
