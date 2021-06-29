package com.tictactoe.service.notification

import cats.Monad
import com.tictactoe.model.Game.{ClassicGame, GameId}
import com.tictactoe.model.{Message, Session}
import com.tictactoe.model.Session.WsSession
import com.tictactoe.storage.game.GameStorage
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.tictactoe.storage.session.SessionStorage

class NotificationServiceImpl[F[_] : Monad](
                                             sessionStorage: SessionStorage[F, WsSession[F]],
                                             gameStorage: GameStorage[F, ClassicGame[F]]
                                           ) extends NotificationService[F] {

  override def notify(
                       initiator: Session.SessionId,
                       gameId: GameId,
                       message: Message.OutgoingMessage
                     ): F[Unit] = {
    for {
      maybeGame <- gameStorage.get(gameId)
      maybeSessionId <- maybeGame.traverse(game => {
        for {
          player1SessionId <- game.player1.get
          player2SessionId <- game.player2.get
        } yield Seq(player1SessionId, player2SessionId).find(_ != initiator)
      })
      maybeSession <- maybeSessionId.flatten
        .traverse(sessionId => {
          for {
            maybeSession <- sessionStorage.get(sessionId)
          } yield maybeSession
        })
      _ <- maybeSession.flatten
        .traverse(session => session.context.outgoingQueue.offer1(message))
    } yield ()
  }
}
