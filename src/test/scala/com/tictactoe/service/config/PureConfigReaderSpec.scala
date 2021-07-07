package com.tictactoe.service.config

import com.tictactoe.model.AppConfig
import com.tictactoe.model.AppConfig.DBConfig.{Password, User}
import com.tictactoe.model.AppConfig.ServerConfig.Timeout.{IdleTimeout, ResponseHeaderTimeout}
import com.tictactoe.model.AppConfig.{Host, Port}
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import pureconfig.ConfigSource

import scala.concurrent.duration._

class PureConfigReaderSpec extends AnyFlatSpec with Matchers with EitherValues {

  "PureConfigReader" should "correctly read AppConfig from application.conf" in new Scope {

    val appConfigResult = pureConfigReader.readAppConfig().value

    appConfigResult shouldBe a[AppConfig]

    appConfigResult.server.port shouldBe Port(8080)
    appConfigResult.server.host shouldBe Host("127.0.0.1")
    appConfigResult.server.timeout.idleTimeout shouldBe IdleTimeout(15.seconds)
    appConfigResult.server.timeout.responseHeaderTimeout shouldBe ResponseHeaderTimeout(10.seconds)

    appConfigResult.db.port shouldBe Port(5432)
    appConfigResult.db.host shouldBe Host("localhost")
    appConfigResult.db.user shouldBe User("admin")
    appConfigResult.db.password shouldBe Password("admin")
  }

  trait Scope {

    def pureConfigReader = {

      val source = ConfigSource.defaultApplication
      PureConfigReader(source)
    }
  }
}
