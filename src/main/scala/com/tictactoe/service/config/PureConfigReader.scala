package com.tictactoe.service.config

import cats.implicits.toBifunctorOps
import com.tictactoe.model.AppConfig
import com.tictactoe.service.config.PureConfigReader.Result
import pureconfig.ConfigObjectSource
import pureconfig.error.ConfigReaderException
import pureconfig.generic.auto._

class PureConfigReader(source: ConfigObjectSource) extends ConfigReader[Result] {

  override def readAppConfig(): Result[AppConfig] =
    source.load[AppConfig]
      .leftMap(ConfigReaderException(_))
}

object PureConfigReader {
  type Result[A] = Either[ConfigReaderException[A], A]

  def apply(source: ConfigObjectSource): PureConfigReader =
    new PureConfigReader(source)
}
