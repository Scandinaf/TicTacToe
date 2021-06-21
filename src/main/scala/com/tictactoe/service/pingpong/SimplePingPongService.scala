package com.tictactoe.service.pingpong

import cats.Applicative
import com.tictactoe.model.Message.IncomingMessage.Ping
import com.tictactoe.model.Message.OutgoingMessage.Pong

class SimplePingPongService[F[_] : Applicative] extends PingPongService[F] {

  override def pong(ping: Ping): F[Pong] =
    Applicative[F].pure(Pong(ping.messageId))
}

object SimplePingPongService {

  def apply[F[_] : Applicative](): SimplePingPongService[F] =
    new SimplePingPongService()
}
