package com.tictactoe.model

import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage}

case class GameInfo(
  gameId: GameId,
  incomingMessage: IncomingMessage,
  outgoingMessage: OutgoingMessage
)
