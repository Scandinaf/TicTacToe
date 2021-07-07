package com.tictactoe.service.notification

import cats.{Monad, Parallel}
import cats.data.OptionT
import cats.syntax.functor._
import cats.syntax.parallel._
import com.tictactoe.model.Game.{ClassicGame, GameId}
import com.tictactoe.model.Message
import com.tictactoe.model.Session.{SessionId, WsSession}
import com.tictactoe.service.game.GameService
import com.tictactoe.service.session.SessionService

class NotificationServiceImpl[F[_] : Monad : Parallel](
  sessionService: SessionService[F],
  gameService: GameService[F]
) extends NotificationService[F] {

  override def notify(
    gameId: GameId,
    message: Message.OutgoingMessage
  ): F[Unit] =
    for {
      _ <- gameService
        .getGame(gameId)
        .flatMap {

          case classicGame: ClassicGame[F] =>
            def sendNotification(sessionId: SessionId): OptionT[F, Boolean] =
              sessionService
                .getSession(sessionId)
                .semiflatMap {

                  case wsSession: WsSession[F] =>
                    wsSession.context.outgoingQueue.offer1(message)
                }

            List(
              sendNotification(classicGame.player1.sessionId),
              OptionT(classicGame.player2.tryGet)
                .map(_.sessionId)
                .flatMap(sendNotification)
            ).parSequence
        }.value
    } yield ()
}

object NotificationServiceImpl {

  def apply[F[_] : Monad : Parallel](
    sessionService: SessionService[F],
    gameService: GameService[F]
  ): NotificationServiceImpl[F] =
    new NotificationServiceImpl(sessionService, gameService)
}
