package com.tictactoe.app.json

import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage, Ping, Pong, UUID}
import io.circe.parser.decode
import io.circe.syntax._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MessageCodecSpec extends AnyFlatSpec with Matchers with EitherValues {

  "IncomingMessageDecoder" should "correctly parse Ping message" in {
    val json = """{"type": "ping", "message_id": "unique identifier"}"""
    val uuid = UUID("unique identifier")
    val result = Ping(messageId = uuid)

    decode[IncomingMessage](json).value shouldBe result
  }

  it should "correctly parse Pong message" in {
    val json = """{"type": "pong", "message_id": "unique identifier"}"""
    val uuid = UUID("unique identifier")
    val result = Pong(messageId = uuid)

    decode[IncomingMessage](json).value shouldBe result
  }

  "OutgoingMessageEncoder" should "correctly convert Pong entity" in {
    val uuid = UUID("unique identifier")
    val pong: OutgoingMessage = Pong(messageId = uuid)
    val result = """{"message_id":"unique identifier","type":"pong"}"""

    pong.asJson.noSpaces shouldBe result
  }

  it should "correctly convert Ping entity" in {
    val uuid = UUID("unique identifier")
    val ping: OutgoingMessage = Ping(messageId = uuid)
    val result = """{"message_id":"unique identifier","type":"ping"}"""

    ping.asJson.noSpaces shouldBe result
  }
}
