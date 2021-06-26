package com.tictactoe.service.game.classic.exception

import com.tictactoe.service.game.classic.model.GameState.FinishedGame

final case class GameAlreadyFinishedException(state: FinishedGame) extends GameException
