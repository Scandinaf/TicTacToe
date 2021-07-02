package com.tictactoe.storage.game

import cats.data.OptionT
import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.tictactoe.model.Game
import com.tictactoe.model.Game.GameId

class LocalGameStorage[F[_] : Sync](localStorage: Ref[F, Map[GameId, Ref[F, Game]]])
  extends GameStorage[F] {

  override def put(game: Game): F[Unit] =
    for {
      projection <- localStorage.get
      _ <- projection.get(game.id)
        .map(_.set(game))
        .getOrElse {
          for {
            gameRef <- Ref.of[F, Game](game)
            _ <- localStorage.update(_ + (game.id -> gameRef))
          } yield ()
        }
    } yield ()

  override def get(gameId: GameId): OptionT[F, Game] =
    OptionT
      .liftF(localStorage.get)
      .subflatMap(_.get(gameId))
      .semiflatMap(_.get)

  override def delete(gameId: GameId): F[Unit] =
    localStorage.update(_ - gameId)
}

object LocalGameStorage {

  def apply[F[_] : Sync](localStorage: Ref[F, Map[GameId, Ref[F, Game]]]): LocalGameStorage[F] =
    new LocalGameStorage(localStorage)
}
