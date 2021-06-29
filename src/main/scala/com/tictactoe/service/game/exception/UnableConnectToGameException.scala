package com.tictactoe.service.game.exception

import com.tictactoe.model.Game.GameId

final case class UnableConnectToGameException(gameId: GameId) extends GameServiceException {

  val message: String = s"Unable connect to game! GameId - $gameId."
}
