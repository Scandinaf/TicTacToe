package com.tictactoe.model

import com.tictactoe.model.AppConfig.ServerConfig
import com.tictactoe.model.AppConfig.ServerConfig.Timeout.IdleTimeout
import com.tictactoe.model.AppConfig.ServerConfig.{Host, Port, Timeout}

import scala.concurrent.duration.FiniteDuration

final case class AppConfig(server: ServerConfig)

object AppConfig {

  final case class ServerConfig(
    host: Host,
    port: Port,
    timeout: Timeout
  )

  object ServerConfig {

    final case class Port(value: Int) extends AnyVal
    final case class Host(value: String) extends AnyVal

    final case class Timeout(idleTimeout: IdleTimeout)

    object Timeout {

      final case class IdleTimeout(value: FiniteDuration) extends AnyVal
    }
  }
}
