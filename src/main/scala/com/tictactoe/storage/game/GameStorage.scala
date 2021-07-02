package com.tictactoe.storage.game

import cats.data.OptionT
import com.tictactoe.model.Game
import com.tictactoe.model.Game.GameId

trait GameStorage[F[_]] {

  def put(game: Game): F[Unit]

  def get(gameId: GameId): OptionT[F, Game]

  def delete(gameId: GameId): F[Unit]
}
