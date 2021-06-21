package com.tictactoe.service.pingpong

import cats.effect.IO
import cats.syntax.option._
import com.tictactoe.model.Message.IncomingMessage.Ping
import com.tictactoe.model.Message.OutgoingMessage.Pong
import com.tictactoe.model.Message.UUID
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimplePingPongServiceSpec extends AnyFlatSpec with Matchers {

  "SimplePingPongService" should "correctly react on Pong" in new Scope {
    val messageId = UUID("1234-1234-1234")
    val ping = Ping(messageId.some)
    val result = Pong(messageId.some)
    pingPongService.pong(ping).unsafeRunSync() shouldBe result
  }

  trait Scope {

    val pingPongService = SimplePingPongService[IO]()
  }
}
