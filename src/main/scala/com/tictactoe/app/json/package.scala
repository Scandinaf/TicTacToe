package com.tictactoe.app

import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage, UUID}
import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto.{
  deriveConfiguredDecoder,
  deriveConfiguredEncoder,
  deriveUnwrappedCodec
}
import io.circe.{Codec, Decoder, Encoder}

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

    (deriveConfiguredDecoder: Decoder[IncomingMessage], deriveConfiguredEncoder: Encoder[OutgoingMessage])
  }
}
