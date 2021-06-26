package com.tictactoe.service.game.classic.model

import com.tictactoe.service.game.classic.model.GameRules.{ColumnCount, RowCount, WinningCombinationLength}

final case class GameRules(
  columnCount: ColumnCount,
  rowCount: RowCount,
  winningCombinationLength: WinningCombinationLength
) {

  val finalPlaygroundSize = columnCount.value * rowCount.value
}

object GameRules {

  final case class RowCount(value: Int) extends AnyVal

  final case class ColumnCount(value: Int) extends AnyVal

  final case class WinningCombinationLength(value: Int) extends AnyVal
}
