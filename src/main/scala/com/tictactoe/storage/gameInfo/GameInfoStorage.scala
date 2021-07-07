package com.tictactoe.storage.gameInfo

import com.tictactoe.model.GameInfo

trait GameInfoStorage[F[_]] {

  def post(gameInfo: GameInfo): F[Int]
}
