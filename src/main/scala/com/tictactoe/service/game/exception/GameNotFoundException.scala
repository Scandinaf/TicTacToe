package com.tictactoe.service.game.exception

import com.tictactoe.model.Game.GameId

final case class GameNotFoundException(gameId: GameId) extends GameServiceException {

  val message: String = s"Game not found! GameId - $gameId."
}
