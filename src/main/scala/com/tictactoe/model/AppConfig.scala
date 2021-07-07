package com.tictactoe.model

import com.tictactoe.model.AppConfig.DBConfig.{Password, User}
import com.tictactoe.model.AppConfig.ServerConfig.Timeout
import com.tictactoe.model.AppConfig.ServerConfig.Timeout.{IdleTimeout, ResponseHeaderTimeout}
import com.tictactoe.model.AppConfig.{DBConfig, ServerConfig}

import scala.concurrent.duration.FiniteDuration

final case class AppConfig(server: ServerConfig, db: DBConfig)

object AppConfig {

  final case class Port(value: Int) extends AnyVal
  final case class Host(value: String) extends AnyVal

  final case class ServerConfig(
    host: Host,
    port: Port,
    timeout: Timeout
  )

  object ServerConfig {

    final case class Timeout(idleTimeout: IdleTimeout, responseHeaderTimeout: ResponseHeaderTimeout)

    object Timeout {

      final case class IdleTimeout(value: FiniteDuration) extends AnyVal
      final case class ResponseHeaderTimeout(value: FiniteDuration) extends AnyVal
    }
  }

  final case class DBConfig(
    host: Host,
    port: Port,
    user: User,
    password: Password
  )

  object DBConfig {

    final case class User(value: String) extends AnyVal
    final case class Password(value: String) extends AnyVal
  }
}
