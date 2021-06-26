package com.tictactoe.service.game.classic.exception

import com.tictactoe.service.game.classic.model.Position

final case class CellAlreadyOccupiedException(position: Position) extends GameException
