package com.tictactoe.app.server.handler

import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage}

trait TicTacToeMessageHandler[F[_]] {

  def handle(incomingMessage: IncomingMessage): F[OutgoingMessage]
}
