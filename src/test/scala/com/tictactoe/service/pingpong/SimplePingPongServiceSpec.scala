package com.tictactoe.service.pingpong

import cats.effect.IO
import com.tictactoe.model.Message.{Ping, Pong, UUID}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimplePingPongServiceSpec extends AnyFlatSpec with Matchers {

  "SimplePingPongService" should "correctly react on Ping" in new Scope {
    val messageId = UUID("1234-1234-1234")
    val pong = Pong(messageId)
    val result = Ping(messageId)
    pingPongService.ping(pong).unsafeRunSync() shouldBe result
  }

  it should "correctly react on Pong" in new Scope {
    val messageId = UUID("1234-1234-1234")
    val ping = Ping(messageId)
    val result = Pong(messageId)
    pingPongService.pong(ping).unsafeRunSync() shouldBe result
  }

  trait Scope {

    val pingPongService = SimplePingPongService[IO]()
  }
}
