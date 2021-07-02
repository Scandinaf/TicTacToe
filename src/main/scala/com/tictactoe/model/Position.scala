package com.tictactoe.model

import cats.Show
import com.tictactoe.model.Position.{Column, Row}
import com.tictactoe.service.game.classic.model.GameRules

final case class Position(column: Column, row: Row)

object Position {

  implicit val show: Show[Position] =
    position =>
      s"Position[Column - ${position.column.value}; Row - ${position.row.value}]"

  implicit val ordering: Ordering[Position] =
    (x: Position, y: Position) =>
      if (x.column.value == y.column.value)
        x.row.value.compareTo(y.row.value)
      else
        x.column.value.compareTo(y.column.value)

  implicit class PositionCompanion(position: Position) {

    def isOutbound(rules: GameRules): Boolean =
      (position.column.value <= 0 || position.column.value > rules.columnCount.value) ||
        (position.row.value <= 0 || position.row.value > rules.rowCount.value)
  }

  final case class Column(value: Int) extends AnyVal

  final case class Row(value: Int) extends AnyVal
}
