package com.tictactoe.service.pingpong

import com.tictactoe.model.Message.{Ping, Pong}

trait PingPongService[F[_]] {

  def ping(pong: Pong): F[Ping]

  def pong(ping: Ping): F[Pong]
}
