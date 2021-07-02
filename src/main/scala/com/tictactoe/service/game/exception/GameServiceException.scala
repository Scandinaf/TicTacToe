package com.tictactoe.service.game.exception

import cats.syntax.show._
import com.tictactoe.exception.AppException
import com.tictactoe.exception.AppException.{ErrorCode, ParameterKey, PrettyMessage}
import com.tictactoe.model.Game.GameId
import com.tictactoe.service.game.classic.exception.GameException

sealed trait GameServiceException extends AppException

object GameServiceException {

  final case class AwaitingConnectionException(gameId: GameId) extends GameServiceException {

    override def prettyMessage: PrettyMessage =
      PrettyMessage(show"Unable to perform an action because not all players have joined the game. $gameId")

    override def parameters: Map[ParameterKey, String] = Map(ParameterKey.GameId -> gameId.value)

    override def errorCode: ErrorCode = ErrorCode.badRequest
  }

  final case class GameAlreadyExistsException(gameId: GameId) extends GameServiceException {

    override def prettyMessage: PrettyMessage =
      PrettyMessage(show"A game with this identifier already exists. $gameId")

    override def parameters: Map[ParameterKey, String] = Map(ParameterKey.GameId -> gameId.value)

    override def errorCode: ErrorCode = ErrorCode.internalError
  }

  final case class GameNotFoundException(gameId: GameId) extends GameServiceException {

    override def prettyMessage: PrettyMessage = PrettyMessage(show"Game not found! $gameId")

    override def parameters: Map[ParameterKey, String] = Map(ParameterKey.GameId -> gameId.value)

    override def errorCode: ErrorCode = ErrorCode.badRequest
  }

  final case class UnableConnectToGameException(gameId: GameId) extends GameServiceException {

    override def prettyMessage: PrettyMessage = PrettyMessage(show"Unable connect to game! $gameId")

    override def parameters: Map[ParameterKey, String] = Map(ParameterKey.GameId -> gameId.value)

    override def errorCode: ErrorCode = ErrorCode.badRequest
  }

  final case class GameExceptionWrapper(gameId: GameId, gameException: GameException)
    extends GameServiceException {

    override def prettyMessage: PrettyMessage =
      PrettyMessage(value = show"${gameException.prettyMessage.value}. $gameId")

    override def parameters: Map[ParameterKey, String] =
      gameException.parameters + (ParameterKey.GameId -> gameId.value)

    override def errorCode: ErrorCode = gameException.errorCode
  }

  final case class NotYouTurnException(gameId: GameId) extends GameServiceException {

    override def prettyMessage: PrettyMessage =
      PrettyMessage(show"At this moment the turn of another player! $gameId")

    override def parameters: Map[ParameterKey, String] = Map(ParameterKey.GameId -> gameId.value)

    override def errorCode: ErrorCode = ErrorCode.badRequest
  }
}
