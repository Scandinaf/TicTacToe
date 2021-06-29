package com.tictactoe.service.game.classic

import cats.ApplicativeThrow
import cats.effect.Concurrent
import cats.effect.concurrent.Deferred
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.tictactoe.model.Game.ClassicGame.GameStatus
import com.tictactoe.model.Game.ClassicGame.GameStatus.AwaitingPlayer
import com.tictactoe.model.Game.{ClassicGame, GameId}
import com.tictactoe.model.Session.SessionId
import com.tictactoe.model.{Game, Session}
import com.tictactoe.service.game.GameService
import com.tictactoe.service.game.classic.model.Position
import com.tictactoe.service.game.exception.{
  AwaitingPlayerException,
  GameNotFoundException,
  GameServiceException,
  UnableConnectToGameException
}
import com.tictactoe.storage.game.ClassicGameStorage
import io.jvm.uuid._

class ClassicGameService[F[_] : Concurrent](classicGameStorage: ClassicGameStorage[F])
  extends GameService[F] {

  override def createGame(sessionId: Session.SessionId): F[Game] =
    for {
      player1SessionIdDef <- Deferred[F, SessionId]
      player2SessionIdDef <- Deferred[F, SessionId]
      _ <- player1SessionIdDef.complete(sessionId)
      game = ClassicGame[F](
        id = GameId(UUID.random.string),
        status = GameStatus.AwaitingPlayer,
        player1 = player1SessionIdDef,
        gameEngine = ClassicTicTacToe[F](),
        player2 = player2SessionIdDef
      )
      _ <- classicGameStorage.put(game)
    } yield game

  override def joinGame(gameId: GameId, sessionId: SessionId): F[Either[GameServiceException, Unit]] =
    for {
      maybeGame <- classicGameStorage.get(gameId)
      result <- maybeGame.toRight(GameNotFoundException(gameId))
        .traverse(game => {
          ApplicativeThrow[F]
            .recover(
              for {
                _ <- game.player2.complete(sessionId)
                _ <- classicGameStorage.put(game.copy(status = GameStatus.Running))
              } yield ().asRight[GameServiceException]
            ) {
              case _ =>
                UnableConnectToGameException(gameId).asLeft
            }
        })
    } yield result.flatten

  override def makeTurn(
    gameId: GameId,
    sessionId: SessionId,
    position: Position
  ): F[Either[GameServiceException, Unit]] =
    for {
      maybeGame <- classicGameStorage.get(gameId)
      result <- maybeGame.toRight(GameNotFoundException(gameId))
        .flatMap(game =>
          if (game.status == AwaitingPlayer)
            AwaitingPlayerException(gameId).asLeft
          else game.asRight
        ).traverse(game => {

          for {
            newGame <- game.gameEngine.makeTurn(position)
          } yield newGame.map(f => game.copy(gameEngine = f)).leftMap(_ => GameNotFoundException(gameId))
        })
      result <- result.flatten.traverse(game => {
        classicGameStorage.put(game)
      })
    } yield result

  override def closeGame(gameId: Game.GameId): F[Unit] =
    classicGameStorage.delete(gameId)
}
