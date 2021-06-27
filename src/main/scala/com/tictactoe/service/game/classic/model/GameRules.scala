package com.tictactoe.service.game.classic.model

import com.tictactoe.service.game.classic.model.GameRules.{ColumnCount, RowCount, WinningCombinationLength}

final case class GameRules(
  columnCount: ColumnCount,
  rowCount: RowCount,
  winningCombinationLength: WinningCombinationLength
) {

  val finalPlaygroundSize: Int = columnCount.value * rowCount.value
  val determineWinnerTurnNumber: Int = (winningCombinationLength.value * 2) - 1
  val adjacentElementNumber: Int = winningCombinationLength.value - 1
  val offsets: List[Int] = (1 until winningCombinationLength.value).toList
}

object GameRules {

  final case class RowCount(value: Int) extends AnyVal

  final case class ColumnCount(value: Int) extends AnyVal

  final case class WinningCombinationLength(value: Int) extends AnyVal
}
