package com.tictactoe.service.gamelog

import com.tictactoe.model.GameInfo

trait GameLog[F[_]] {

  def logGameInfo(gameInfo: GameInfo): F[Unit]
}
