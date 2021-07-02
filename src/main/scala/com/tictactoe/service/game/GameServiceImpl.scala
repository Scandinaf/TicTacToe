package com.tictactoe.service.game

import cats.data.{EitherT, OptionT}
import cats.effect.Concurrent
import cats.syntax.apply._
import cats.syntax.either._
import cats.syntax.functor._
import cats.{Applicative, ApplicativeThrow}
import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.Game.ClassicGame.GameStatus.{AwaitingConnection, Finished}
import com.tictactoe.model.Game.ClassicGame.{GameStatus, PlayerInfo}
import com.tictactoe.model.Game.{ClassicGame, GameId}
import com.tictactoe.model.Session.SessionId
import com.tictactoe.model.{Game, Position}
import com.tictactoe.service.game.classic.exception.GameException.GameAlreadyFinishedException
import com.tictactoe.service.game.classic.model.GameState.{ActiveGame, FinishedGame}
import com.tictactoe.service.game.exception.GameServiceException
import com.tictactoe.service.game.exception.GameServiceException._
import com.tictactoe.storage.game.GameStorage

class GameServiceImpl[F[_] : Concurrent](gameStorage: GameStorage[F])
  extends GameService[F] {

  override def createGame(game: Game): EitherT[F, GameAlreadyExistsException, Unit] =
    getGame(game.id)
      .toLeftF(gameStorage.put(game))
      .leftMap(_ => GameAlreadyExistsException(game.id))

  override def getGame(gameId: GameId): OptionT[F, Game] =
    gameStorage.get(gameId)

  override def joinGame(gameId: GameId, sessionId: SessionId): EitherT[F, GameServiceException, PlayerCellType] =
    getGame(gameId)
      .toRight(GameNotFoundException(gameId))
      .flatMapF {

        case classicGame: Game.ClassicGame[F] =>
          ApplicativeThrow[F]
            .recover {
              val playerCellType = classicGame.player1.cellType.swap()
              val playerInfo =
                PlayerInfo(
                  sessionId = sessionId,
                  cellType = playerCellType
                )
              classicGame.player2.complete(playerInfo) *>
                gameStorage.put(classicGame.copy(status = GameStatus.Running))
                  .as(playerCellType.asRight[GameServiceException])
            } {
              case _ =>
                UnableConnectToGameException(gameId).asLeft
            }
      }

  override def makeTurn(
    gameId: GameId,
    sessionId: SessionId,
    position: Position
  ): EitherT[F, GameServiceException, Game] =
    getGame(gameId)
      .toRight(GameNotFoundException(gameId))
      .subflatMap {

        case classicGame: ClassicGame[F] =>
          if (classicGame.status == AwaitingConnection)
            AwaitingConnectionException(gameId).asLeft[Game]
          else if (classicGame.status == Finished)
            GameExceptionWrapper(gameId, GameAlreadyFinishedException).asLeft[Game]
          else
            classicGame.asRight[GameServiceException]
      }.flatMapF {

        case classicGame: ClassicGame[F] =>
          def getCurrentPlayerInfo(classicGame: ClassicGame[F]): F[PlayerInfo] =
            if (classicGame.player1.sessionId == sessionId)
              Applicative[F].pure(classicGame.player1)
            else
              classicGame.player2.tryGet.map(_.get)

          getCurrentPlayerInfo(classicGame)
            .map(playerInfo =>
              Either.cond[GameServiceException, Game](
                classicGame.gameEngine.state.turnType == playerInfo.cellType,
                classicGame,
                NotYouTurnException(gameId)
              )
            )
      }.flatMap {

        case classicGame: ClassicGame[F] =>
          classicGame.gameEngine.makeTurn(position)
            .map[Game](gameEngine =>
              gameEngine.state match {

                case _: ActiveGame =>
                  classicGame.copy(gameEngine = gameEngine)

                case _: FinishedGame =>
                  classicGame.copy(status = Finished, gameEngine = gameEngine)
              }
            )
            .semiflatTap(gameStorage.put)
            .leftMap[GameServiceException](GameExceptionWrapper(gameId, _))
      }

  override def closeGame(gameId: Game.GameId): F[Unit] =
    gameStorage.delete(gameId)
}

object GameServiceImpl {

  def apply[F[_] : Concurrent](gameStorage: GameStorage[F]): GameServiceImpl[F] =
    new GameServiceImpl(gameStorage)
}
