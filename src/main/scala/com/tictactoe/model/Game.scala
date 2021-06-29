package com.tictactoe.model

import cats.effect.concurrent.Deferred
import com.tictactoe.model.Game.ClassicGame.GameStatus
import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.game.classic.ClassicTicTacToe
import enumeratum._

sealed trait Game {

  def id: GameId
}

object Game {

  final case class GameId(value: String) extends AnyVal

  final case class ClassicGame[F[_]](
                                      id: GameId,
                                      status: GameStatus,
                                      player1: Deferred[F, SessionId],
                                      gameEngine: ClassicTicTacToe[F],
                                      player2: Deferred[F, SessionId]
                                    ) extends Game

  object ClassicGame {

    sealed trait GameStatus extends EnumEntry
    object GameStatus extends Enum[GameStatus] with CirceEnum[GameStatus] {

      final case object AwaitingPlayer extends GameStatus
      final case object Running extends GameStatus

      override def values: IndexedSeq[GameStatus] = findValues
    }
  }
}