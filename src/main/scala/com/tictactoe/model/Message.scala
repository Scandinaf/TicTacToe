package com.tictactoe.model

import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Message.OutgoingMessage.Error.{ErrorType, Reason}
import com.tictactoe.model.Message.UUID
import com.tictactoe.service.game.classic.model.Position
import enumeratum._

sealed trait Message {

  def messageId: Option[UUID]
}

object Message {

  final case class UUID(value: String) extends AnyVal

  sealed trait IncomingMessage extends Message

  object IncomingMessage {

    final case class Ping(messageId: Option[UUID]) extends IncomingMessage

    final case class CreateClassicGame(messageId: Option[UUID]) extends IncomingMessage

    final case class JoinGame(messageId: Option[UUID], gameId: GameId) extends IncomingMessage

    final case class MakeTurn(
      messageId: Option[UUID],
      gameId: GameId,
      position: Position
    ) extends IncomingMessage
  }

  sealed trait OutgoingMessage extends Message

  object OutgoingMessage {

    final case class Pong(messageId: Option[UUID]) extends OutgoingMessage

    final case class ClassicGameCreated(gameId: GameId, messageId: Option[UUID]) extends OutgoingMessage

    final case class JoinedGame(messageId: Option[UUID], gameId: GameId) extends OutgoingMessage

    final case class MadeTurn(
      messageId: Option[UUID],
      gameId: GameId,
      position: Position
    ) extends OutgoingMessage

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
        final case object GameError extends ErrorType

        val values: IndexedSeq[ErrorType] = findValues
      }
    }
  }
}
