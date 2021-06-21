package com.tictactoe.service.config

import com.tictactoe.model.AppConfig

trait ConfigReader[F[_]] {

  def readAppConfig(): F[AppConfig]
}
