package com.tictactoe.service.config

import cats.implicits.toBifunctorOps
import com.tictactoe.model.AppConfig
import com.tictactoe.service.config.PureConfigReader.Result
import pureconfig.error.ConfigReaderException
import pureconfig.generic.ProductHint
import pureconfig.generic.auto._
import com.tictactoe.service.config.PureConfigReader.Hint.camelCaseHint
import pureconfig.{CamelCase, ConfigFieldMapping, ConfigObjectSource}

class PureConfigReader(source: ConfigObjectSource) extends ConfigReader[Result] {

  override def readAppConfig(): Result[AppConfig] =
    source.load[AppConfig]
      .leftMap(ConfigReaderException(_))
}

object PureConfigReader {
  type Result[A] = Either[ConfigReaderException[A], A]

  object Hint {

    implicit def camelCaseHint[A]: ProductHint[A] =
      ProductHint[A](ConfigFieldMapping(CamelCase, CamelCase))
  }

  def apply(source: ConfigObjectSource): PureConfigReader =
    new PureConfigReader(source)
}
