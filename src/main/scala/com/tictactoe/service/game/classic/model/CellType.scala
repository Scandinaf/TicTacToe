package com.tictactoe.service.game.classic.model

sealed trait CellType

object CellType {

  final case object EmptyCell extends CellType

  sealed trait PlayerCellType extends CellType

  object PlayerCellType {

    final case object TicCell extends PlayerCellType

    final case object TacCell extends PlayerCellType
  }
}
