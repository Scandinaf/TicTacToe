package com.tictactoe.service.game.exception

import com.tictactoe.model.Game.GameId

final case class AwaitingPlayerException(gameId: GameId) extends GameServiceException {

  val message: String = s"Unable to perform an action because not all players have joined the game. GameId - $gameId."
}
