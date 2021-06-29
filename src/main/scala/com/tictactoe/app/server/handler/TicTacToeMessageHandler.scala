package com.tictactoe.app.server.handler

import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage}
import com.tictactoe.model.Session.SessionId

trait TicTacToeMessageHandler[F[_]] {

  def handle(incomingMessage: IncomingMessage)(implicit sessionId: SessionId): F[OutgoingMessage]
}
