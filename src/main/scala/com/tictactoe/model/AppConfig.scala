package com.tictactoe.model

import com.tictactoe.model.AppConfig.ServerConfig
import com.tictactoe.model.AppConfig.ServerConfig.{Host, Port}

final case class AppConfig(server: ServerConfig)

object AppConfig {

  final case class ServerConfig(host: Host, port: Port)

  object ServerConfig {

    final case class Port(value: Int) extends AnyVal
    final case class Host(value: String) extends AnyVal
  }
}
