package com.tictactoe.service.gamelog

import cats.effect.BracketThrow
import cats.syntax.functor._
import com.tictactoe.model.GameInfo
import com.tictactoe.storage.gameInfo.GameInfoStorage
import doobie.implicits._
import doobie.{ConnectionIO, Transactor}

class DBGameLog[F[_] : BracketThrow](gameInfoStorage: GameInfoStorage[ConnectionIO], transactor: Transactor[F])
  extends GameLog[F] {

  override def logGameInfo(gameInfo: GameInfo): F[Unit] =
    gameInfoStorage.post(gameInfo)
      .transact(transactor).as(())
}

object DBGameLog {

  def apply[F[_] : BracketThrow](
    gameInfoStorage: GameInfoStorage[ConnectionIO],
    transactor: Transactor[F]
  ): DBGameLog[F] =
    new DBGameLog(gameInfoStorage, transactor)
}
