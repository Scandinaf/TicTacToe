package com.tictactoe.app.json

import cats.syntax.option._
import com.tictactoe.model.Message.IncomingMessage.Ping
import com.tictactoe.model.Message.OutgoingMessage.Error.{ErrorType, Reason}
import com.tictactoe.model.Message.OutgoingMessage.{Error, Pong}
import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage, UUID}
import io.circe.parser.decode
import io.circe.syntax._
import org.scalatest.EitherValues
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class MessageCodecSpec extends AnyFlatSpec with Matchers with EitherValues {

  "IncomingMessageDecoder" should "correctly parse Ping message" in {
    val json = """{"type": "ping", "message_id": "unique identifier"}"""
    val uuid = UUID("unique identifier")
    val result = Ping(messageId = uuid.some)

    decode[IncomingMessage](json).value shouldBe result
  }

  "OutgoingMessageEncoder" should "correctly convert Pong entity" in {
    val uuid = UUID("unique identifier")
    val pong: OutgoingMessage = Pong(messageId = uuid.some)
    val result = """{"message_id":"unique identifier","type":"pong"}"""

    pong.asJson.noSpaces shouldBe result
  }

  it should "correctly convert Error entity" in {
    val uuid = UUID("unique identifier")
    val error: OutgoingMessage = Error(
      errorType = ErrorType.InternalError,
      reason = Reason(s"Reason"),
      messageId = uuid.some
    )
    val result =
      """{"error_type":"InternalError","reason":"Reason","message_id":"unique identifier","type":"error"}""".stripMargin

    error.asJson.noSpaces shouldBe result
  }
}
