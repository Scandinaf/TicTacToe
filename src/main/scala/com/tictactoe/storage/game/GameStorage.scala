package com.tictactoe.storage.game

import com.tictactoe.model.Game
import com.tictactoe.model.Game.GameId

trait GameStorage[F[_], T <: Game] {

  def put(game: T): F[Unit]

  def get(gameId: GameId): F[Option[T]]

  def delete(gameId: GameId): F[Unit]
}
