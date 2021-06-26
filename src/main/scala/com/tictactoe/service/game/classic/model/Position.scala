package com.tictactoe.service.game.classic.model

import com.tictactoe.service.game.classic.model.Position.{Column, Row}

final case class Position(column: Column, row: Row)

object Position {

  final case class Column(value: Int) extends AnyVal

  final case class Row(value: Int) extends AnyVal
}
