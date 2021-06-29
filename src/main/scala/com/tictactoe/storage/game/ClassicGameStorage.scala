package com.tictactoe.storage.game

import cats.effect.Sync
import cats.effect.concurrent.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import com.tictactoe.model.Game.{ClassicGame, GameId}

class ClassicGameStorage[F[_] : Sync](
                                       localStorage: Ref[F, Map[GameId, Ref[F, ClassicGame[F]]]]
                                     ) extends GameStorage[F, ClassicGame[F]] {

  override def put(game: ClassicGame[F]): F[Unit] =
    for {
      map <- localStorage.get
      _ <- map.get(game.id) match {
        case Some(gameRef) => gameRef.set(game)
        case None =>
          for {
            gameRef <- Ref.of[F, ClassicGame[F]](game)
            _ <- localStorage.update(_ + (game.id -> gameRef))
          } yield ()
      }
    } yield ()

  override def get(gameId: GameId): F[Option[ClassicGame[F]]] =
    for {
      map <- localStorage.get
      maybeGame <- map.get(gameId).traverse(_.get)
    } yield maybeGame

  override def delete(gameId: GameId): F[Unit] =
    localStorage.update(_ - gameId)
}
