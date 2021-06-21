package com.tictactoe.app

import com.tictactoe.model.Message.OutgoingMessage.Error.Reason
import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage, UUID}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
  deriveUnwrappedCodec,
  deriveUnwrappedEncoder
}
import io.circe.{Codec, Decoder, Encoder}
import com.tictactoe.model.Message.OutgoingMessage.Error.ErrorType.circeEncoder

package object json {

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
    implicit val reasonEncoder: Encoder[Reason] = deriveUnwrappedEncoder

    (deriveConfiguredDecoder: Decoder[IncomingMessage], deriveConfiguredEncoder: Encoder[OutgoingMessage])
  }
}
