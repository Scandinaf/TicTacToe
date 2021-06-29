package com.tictactoe.app.server.handler

import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, Monad}
import com.tictactoe.model.Message
import com.tictactoe.model.Message.IncomingMessage.{CreateClassicGame, JoinGame, MakeTurn, Ping}
import com.tictactoe.model.Message.OutgoingMessage
import com.tictactoe.model.Message.OutgoingMessage.Error.{ErrorType, Reason}
import com.tictactoe.model.Message.OutgoingMessage.{ClassicGameCreated, Error, JoinedGame, MadeTurn}
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.game.GameService
import com.tictactoe.service.notification.NotificationService
import com.tictactoe.service.pingpong.PingPongService

class TicTacToeMessageHandlerImpl[F[+_] : Monad](
  pingPongService: PingPongService[F],
  classicGameService: GameService[F],
  notificationService: NotificationService[F]
) extends TicTacToeMessageHandler[F] {

  override def handle(
    incomingMessage: Message.IncomingMessage
  )(implicit
    sessionId: SessionId
  ): F[OutgoingMessage] =
    incomingMessage match {

      case ping: Ping =>
        pingPongService.pong(ping)

      case CreateClassicGame(messageId) =>
        for {
          game <- classicGameService.createGame(sessionId)
        } yield ClassicGameCreated(game.id, messageId)

      case JoinGame(messageId, gameId) =>
        for {
          joinGameResult <- classicGameService.joinGame(gameId, sessionId)
          result <- joinGameResult match {

            case Left(gameServiceException) =>
              Applicative[F].pure(
                Error(
                  errorType = ErrorType.GameError,
                  reason = Reason(gameServiceException.message),
                  messageId
                )
              )

            case Right(_) =>
              for {
                _ <- notificationService.notify(sessionId, gameId, JoinedGame(None, gameId))
              } yield JoinedGame(messageId, gameId)
          }
        } yield result

      case MakeTurn(messageId, gameId, position) =>
        classicGameService.makeTurn(gameId, sessionId, position).flatMap {

          case Left(gameServiceException) =>
            Applicative[F].pure(
              Error(
                errorType = ErrorType.GameError,
                reason = Reason(gameServiceException.message),
                messageId
              )
            )

          case Right(_) =>
            for {
              _ <- notificationService.notify(sessionId, gameId, MadeTurn(None, gameId, position))
            } yield MadeTurn(messageId, gameId, position)
        }
    }
}

object TicTacToeMessageHandlerImpl {

  def apply[F[+_] : Monad](
    pingPongService: PingPongService[F],
    classicGameService: GameService[F],
    notificationService: NotificationService[F]
  ): TicTacToeMessageHandlerImpl[F] =
    new TicTacToeMessageHandlerImpl(pingPongService, classicGameService, notificationService)
}
