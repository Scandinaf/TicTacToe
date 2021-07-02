package com.tictactoe.service.game

import cats.data.{EitherT, OptionT}
import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Session.SessionId
import com.tictactoe.model.{Game, Position}
import com.tictactoe.service.game.exception.GameServiceException
import com.tictactoe.service.game.exception.GameServiceException.GameAlreadyExistsException

trait GameService[F[_]] {

  def createGame(game: Game): EitherT[F, GameAlreadyExistsException, Unit]

  def getGame(gameId: GameId): OptionT[F, Game]

  def joinGame(gameId: GameId, sessionId: SessionId): EitherT[F, GameServiceException, PlayerCellType]

  def makeTurn(
    gameId: GameId,
    sessionId: SessionId,
    position: Position
  ): EitherT[F, GameServiceException, Game]

  def closeGame(gameId: GameId): F[Unit]
}
