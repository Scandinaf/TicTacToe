package com.tictactoe.model

import cats.Show
import cats.effect.concurrent.TryableDeferred
import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.Game.ClassicGame.{GameStatus, PlayerInfo}
import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.game.classic.ClassicTicTacToe
import enumeratum._

sealed trait Game {

  def id: GameId
}

object Game {

  final case class GameId(value: String) extends AnyVal

  object GameId {

    implicit val show: Show[GameId] =
      gameId =>
        s"GameId - ${gameId.value}"
  }

  final case class ClassicGame[F[_]](
    id: GameId,
    status: GameStatus,
    player1: PlayerInfo,
    gameEngine: ClassicTicTacToe[F],
    player2: TryableDeferred[F, PlayerInfo]
  ) extends Game

  object ClassicGame {

    final case class PlayerInfo(sessionId: SessionId, cellType: PlayerCellType)

    sealed trait GameStatus extends EnumEntry

    object GameStatus extends Enum[GameStatus] {

      final case object AwaitingConnection extends GameStatus

      final case object Running extends GameStatus

      final case object Finished extends GameStatus

      override def values: IndexedSeq[GameStatus] = findValues
    }
  }
}
