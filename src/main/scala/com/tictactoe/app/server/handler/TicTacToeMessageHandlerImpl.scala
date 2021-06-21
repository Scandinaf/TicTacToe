package com.tictactoe.app.server.handler

import com.tictactoe.model.Message
import com.tictactoe.model.Message.IncomingMessage.Ping
import com.tictactoe.model.Message.OutgoingMessage
import com.tictactoe.service.pingpong.PingPongService

class TicTacToeMessageHandlerImpl[F[+_]](pingPongService: PingPongService[F])
  extends TicTacToeMessageHandler[F] {

  override def handle(incomingMessage: Message.IncomingMessage): F[OutgoingMessage] =
    incomingMessage match {
      case ping: Ping =>
        pingPongService.pong(ping)
    }
}

object TicTacToeMessageHandlerImpl {

  def apply[F[+_]](pingPongService: PingPongService[F]): TicTacToeMessageHandlerImpl[F] =
    new TicTacToeMessageHandlerImpl(pingPongService)
}
