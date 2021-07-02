package com.tictactoe.service.notification

import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Message.OutgoingMessage

trait NotificationService[F[_]] {

  def notify(
    gameId: GameId,
    message: OutgoingMessage
  ): F[Unit]
}
