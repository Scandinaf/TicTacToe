package com.tictactoe.service.pingpong

import cats.Applicative
import com.tictactoe.model.Message.{Ping, Pong}

class SimplePingPongService[F[_] : Applicative] extends PingPongService[F] {

  override def pong(ping: Ping): F[Pong] =
    Applicative[F].pure(Pong(ping.messageId))

  override def ping(pong: Pong): F[Ping] =
    Applicative[F].pure(Ping(pong.messageId))
}

object SimplePingPongService {

  def apply[F[_] : Applicative](): SimplePingPongService[F] =
    new SimplePingPongService()
}
