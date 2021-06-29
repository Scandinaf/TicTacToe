package com.tictactoe.service.game

import com.tictactoe.model.Game
import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.game.classic.model.Position
import com.tictactoe.service.game.exception.GameServiceException

trait GameService[F[_]] {

  def createGame(sessionId: SessionId): F[Game]

  def joinGame(gameId: GameId, sessionId: SessionId): F[Either[GameServiceException, Unit]]

  def makeTurn(gameId: GameId, sessionId: SessionId, position: Position): F[Either[GameServiceException, Unit]]

  def closeGame(gameId: GameId): F[Unit]
}