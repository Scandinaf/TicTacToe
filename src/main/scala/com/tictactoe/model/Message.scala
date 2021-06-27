package com.tictactoe.model

import com.tictactoe.model.Message.OutgoingMessage.Error.{ErrorType, Reason}
import com.tictactoe.model.Message.UUID
import enumeratum._

sealed trait Message {

  def messageId: Option[UUID]
}

object Message {

  final case class UUID(value: String) extends AnyVal

  sealed trait IncomingMessage extends Message

  object IncomingMessage {

    final case class Ping(messageId: Option[UUID]) extends IncomingMessage
  }

  sealed trait OutgoingMessage extends Message

  object OutgoingMessage {

    final case class Pong(messageId: Option[UUID]) extends OutgoingMessage

    final case class Error(
      errorType: ErrorType,
      reason: Reason,
      messageId: Option[UUID]
    ) extends OutgoingMessage

    object Error {

      final case class Reason(value: String) extends AnyVal

      sealed trait ErrorType extends EnumEntry
      object ErrorType extends Enum[ErrorType] with CirceEnum[ErrorType] {

        final case object TransmittedDataError extends ErrorType
        final case object InternalError extends ErrorType

        val values: IndexedSeq[ErrorType] = findValues
      }
    }
  }
}
