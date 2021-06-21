package com.tictactoe.service.config

import com.tictactoe.model.AppConfig
import com.tictactoe.model.AppConfig.ServerConfig.{Host, Port}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource

class PureConfigReaderSpec extends AnyFlatSpec with Matchers with EitherValues {

  "PureConfigReader" should "correctly read AppConfig from application.conf" in new Scope {

    val appConfigResult = pureConfigReader.readAppConfig().value

    appConfigResult shouldBe a[AppConfig]
    appConfigResult.server.port shouldBe Port(8080)
    appConfigResult.server.host shouldBe Host("127.0.0.1")
  }

  trait Scope {

    def pureConfigReader = {

      val source = ConfigSource.defaultApplication
      PureConfigReader(source)
    }
  }
}
