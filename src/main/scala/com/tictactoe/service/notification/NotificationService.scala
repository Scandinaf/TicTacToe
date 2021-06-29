package com.tictactoe.service.notification

import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Message.OutgoingMessage
import com.tictactoe.model.Session.SessionId

trait NotificationService[F[_]] {

  def notify(
              initiator: SessionId,
              gameId: GameId,
              message: OutgoingMessage
            ): F[Unit]
}
