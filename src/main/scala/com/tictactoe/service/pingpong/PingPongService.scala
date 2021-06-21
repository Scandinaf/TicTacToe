package com.tictactoe.service.pingpong

import com.tictactoe.model.Message.IncomingMessage.Ping
import com.tictactoe.model.Message.OutgoingMessage.Pong

trait PingPongService[F[_]] {

  def pong(ping: Ping): F[Pong]
}
