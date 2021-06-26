package com.tictactoe.service.game.classic.exception

import com.tictactoe.service.game.classic.model.{GameRules, Position}

final case class PositionOutboundException(position: Position, rules: GameRules) extends GameException
