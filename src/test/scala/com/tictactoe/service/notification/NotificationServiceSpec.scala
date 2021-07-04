package com.tictactoe.service.notification

import cats.Id
import cats.data.OptionT
import cats.effect.concurrent.TryableDeferred
import cats.syntax.option._
import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.Game.ClassicGame.{GameStatus, PlayerInfo}
import com.tictactoe.model.Game.{ClassicGame, GameId}
import com.tictactoe.model.Message.OutgoingMessage.Pong
import com.tictactoe.model.Message.{OutgoingMessage, UUID}
import com.tictactoe.model.Session.{SessionId, WsSession}
import com.tictactoe.model.User.{SimpleUser, UserId}
import com.tictactoe.model.{Game, Session}
import com.tictactoe.service.game.GameService
import com.tictactoe.service.game.classic.ClassicTicTacToe
import com.tictactoe.service.session.SessionService
import fs2.concurrent.Queue
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class NotificationServiceSpec extends AnyFlatSpec with Matchers with MockitoSugar {

  "NotificationService" should "ignore messages for an unknown gameId" in new Scope {
    val gameId: GameId = GameId("1234-1234-1234")
    val outgoingMessage: Pong = Pong(messageId = UUID("1234-1234-1234-1234").some)

    when(gameService.getGame(gameId)).thenReturn(OptionT.none[Id, Game])
    service.notify(gameId, outgoingMessage)

    verify(gameService, only).getGame(*[GameId])
    verifyZeroInteractions(sessionService)
  }

  it should "ignore messages for an unknown sessionId" in new Scope {
    val gameId: GameId = GameId("1234-1234-1234")
    val outgoingMessage: Pong = Pong(messageId = UUID("1234-1234-1234-1234").some)
    val sessionId: SessionId = SessionId("fake_id")
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TicCell)
    val playerInfoDeferred: TryableDeferred[Id, PlayerInfo] = mock[TryableDeferred[Id, PlayerInfo]]
    val engine: ClassicTicTacToe[Id] = mock[ClassicTicTacToe[Id]]
    val game: ClassicGame[Id] =
      ClassicGame[Id](gameId, GameStatus.Running, playerInfo, engine, playerInfoDeferred)

    when(gameService.getGame(gameId)).thenReturn(OptionT.pure[Id](game))
    when(playerInfoDeferred.tryGet).thenReturn(playerInfo.some)
    when(sessionService.getSession(sessionId)).thenReturn(OptionT.none[Id, Session])

    service.notify(gameId, outgoingMessage)

    verify(gameService, only).getGame(*[GameId])
    verify(sessionService, times(2)).getSession(*[SessionId])
  }

  it should "correctly notify all players" in new Scope {
    val gameId: GameId = GameId("1234-1234-1234")
    val outgoingMessage: Pong = Pong(messageId = UUID("1234-1234-1234-1234").some)
    val sessionId: SessionId = SessionId("real_id")
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TicCell)
    val playerInfoDeferred: TryableDeferred[Id, PlayerInfo] = mock[TryableDeferred[Id, PlayerInfo]]
    val engine: ClassicTicTacToe[Id] = mock[ClassicTicTacToe[Id]]
    val game: ClassicGame[Id] =
      ClassicGame[Id](gameId, GameStatus.Running, playerInfo, engine, playerInfoDeferred)
    val outgoingQueue: Queue[Id, OutgoingMessage] = mock[Queue[Id, OutgoingMessage]]
    val session: WsSession[Id] =
      WsSession(sessionId, SimpleUser(UserId(1234)), WsSession.Context(outgoingQueue))

    when(gameService.getGame(gameId)).thenReturn(OptionT.pure[Id](game))
    when(playerInfoDeferred.tryGet).thenReturn(playerInfo.some)
    when(sessionService.getSession(sessionId)).thenReturn(OptionT.pure[Id](session))
    when(outgoingQueue.offer1(outgoingMessage)).thenReturn(true)

    service.notify(gameId, outgoingMessage)

    verify(gameService, only).getGame(*[GameId])
    verify(sessionService, times(2)).getSession(*[SessionId])
    verify(outgoingQueue, times(2)).offer1(*[OutgoingMessage])
  }

  trait Scope {

    val sessionService: SessionService[Id] = mock[SessionService[Id]]
    val gameService: GameService[Id] = mock[GameService[Id]]
    val service: NotificationService[Id] = NotificationServiceImpl(sessionService, gameService)
  }
}
