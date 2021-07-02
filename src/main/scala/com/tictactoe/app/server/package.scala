package com.tictactoe.app

import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage, UUID}
import com.tictactoe.model.Position
import com.tictactoe.model.Position.{Column, Row}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Codec, Decoder, Encoder, KeyEncoder}
import org.http4s.{Headers, Request}
import com.tictactoe.model.CellType.PlayerCellType.circeEncoder
import com.tictactoe.exception.AppException.{ErrorCode, ParameterKey, PrettyMessage}
import com.tictactoe.model.Message.OutgoingMessage.GameFinished.WinnerInfo

package object server {

  object JsonOps {

    implicit val (
      incomingMessageDecoder: Decoder[IncomingMessage],
      outgoingMessageEncoder: Encoder[OutgoingMessage]
    ) = {
      val discriminatorFieldName = "type"
      implicit val derivationConfig: Configuration =
        Configuration.default
          .withSnakeCaseConstructorNames
          .withSnakeCaseMemberNames
          .withDiscriminator(discriminatorFieldName)

      implicit val uuidCodec: Codec[UUID] = deriveUnwrappedCodec
      implicit val gameIdEncoder: Codec[GameId] = deriveUnwrappedCodec
      implicit val columnEncoder: Codec[Column] = deriveUnwrappedCodec
      implicit val rowEncoder: Codec[Row] = deriveUnwrappedCodec
      implicit val positionEncoder: Codec[Position] = deriveConfiguredCodec
      implicit val winnerInfoEncoder: Encoder[WinnerInfo] = deriveConfiguredEncoder
      implicit val prettyMessageEncoder: Encoder[PrettyMessage] = deriveUnwrappedEncoder
      implicit val errorCodeEncoder: Encoder[ErrorCode] = deriveUnwrappedEncoder
      implicit val parameterKeyEncoder: KeyEncoder[ParameterKey] =
        KeyEncoder.encodeKeyString.contramap(_.entryName)

      (deriveConfiguredDecoder: Decoder[IncomingMessage], deriveConfiguredEncoder: Encoder[OutgoingMessage])
    }
  }

  object CompanionOps {

    implicit class RequestCompanion[F[_]](req: Request[F]) {

      val showHeaders: String =
        req.headers
          .redactSensitive(Headers.SensitiveHeaders.contains)
          .toList
          .mkString("Headers(", ", ", ")")

      val show: String =
        s"""
           |-------------------->
           |-------------------->
           |-------------------->
           |${req.httpVersion} ${req.method} ${req.uri} $showHeaders"""
          .stripMargin
    }
  }
}
