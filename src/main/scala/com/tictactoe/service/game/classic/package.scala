package com.tictactoe.service.game

import com.tictactoe.service.game.classic.model.{GameRules, Position}

package object classic {

  object OrderingOps {

    implicit val positionOrdering: Ordering[Position] =
      (x: Position, y: Position) =>
        if (x.column.value == y.column.value)
          x.row.value.compareTo(y.row.value)
        else
          x.column.value.compareTo(y.column.value)
  }

  object CompanionOps {

    implicit class PositionCompanion(position: Position) {

      def isOutbound(rules: GameRules): Boolean =
        (position.column.value <= 0 || position.column.value > rules.columnCount.value) ||
          (position.row.value <= 0 || position.row.value > rules.rowCount.value)
    }
  }
}
