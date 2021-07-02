package com.tictactoe.model

import cats.Show
import com.tictactoe.exception.AppException.{ErrorCode, ParameterKey, PrettyMessage}
import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Message.OutgoingMessage.GameFinished.WinnerInfo
import com.tictactoe.model.Message.UUID
import com.tictactoe.service.game.classic.model.GameState.WinningCombination

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

    final case class ClassicGameCreated(
      gameId: GameId,
      messageId: Option[UUID],
      cellType: PlayerCellType
    ) extends OutgoingMessage

    final case class PlayerJoinedToGame(
      messageId: Option[UUID],
      gameId: GameId,
      cellType: PlayerCellType
    ) extends OutgoingMessage

    final case class TurnResult(
      messageId: Option[UUID],
      gameId: GameId,
      position: Position
    ) extends OutgoingMessage

    final case class GameFinished(
      messageId: Option[UUID],
      gameId: GameId,
      position: Position,
      winnerInfo: Option[WinnerInfo]
    ) extends OutgoingMessage

    object GameFinished {

      final case class WinnerInfo(cellType: PlayerCellType, winningCombination: WinningCombination)
    }

    final case class Error(
      messageId: Option[UUID],
      prettyMessage: PrettyMessage,
      parameters: Map[ParameterKey, String],
      errorCode: ErrorCode
    ) extends OutgoingMessage

    object Error {

      implicit val show: Show[OutgoingMessage.Error] =
        error =>
          s"Pretty message - ${error.prettyMessage.value}; MessageId - ${error.messageId}"
    }
  }
}
