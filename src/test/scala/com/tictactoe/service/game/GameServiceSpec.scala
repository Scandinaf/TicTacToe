package com.tictactoe.service.game

import cats.data.{EitherT, OptionT}
import cats.effect.concurrent.Deferred
import cats.effect.{ContextShift, IO}
import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.{Game, Position}
import com.tictactoe.model.Game.ClassicGame.{GameStatus, PlayerInfo}
import com.tictactoe.model.Game.{ClassicGame, GameId}
import com.tictactoe.model.Position.{Column, Row}
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.game.classic.ClassicTicTacToe
import com.tictactoe.service.game.classic.model.GameState.{ActiveGame, TurnNumber}
import com.tictactoe.service.game.exception.GameServiceException.{
  AwaitingConnectionException,
  GameExceptionWrapper,
  GameNotFoundException,
  NotYouTurnException,
  UnableConnectToGameException
}
import com.tictactoe.storage.game.GameStorage
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.concurrent.ExecutionContext

class GameServiceSpec extends AnyFlatSpec with Matchers with MockitoSugar {

  "GameService" should "get the game correctly" in new Scope {
    val gameId: GameId = GameId("game_id")
    val game: Game = mock[Game]
    when(gameStorage.get(gameId)).thenReturn(OptionT.pure[IO](game))

    service.getGame(gameId).value.unsafeRunSync()
    verify(gameStorage, only).get(*[GameId])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "delete the game correctly" in new Scope {
    val gameId: GameId = GameId("game_id")
    when(gameStorage.delete(gameId)).thenReturn(IO.unit)

    service.closeGame(gameId).unsafeRunSync()
    verify(gameStorage, only).delete(*[GameId])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "throw GameNotFoundException if game doesn't exist during the joinGame operation" in new Scope {
    val gameId: GameId = GameId("fake_id")
    val sessionId: SessionId = SessionId("fake_id")

    when(gameStorage.get(gameId)).thenReturn(OptionT.none)

    assertThrows[GameNotFoundException](
      service.joinGame(gameId, sessionId).rethrowT.unsafeRunSync()
    )

    verify(gameStorage, only).get(*[GameId])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "throw UnableConnectToGameException when there is a problem connecting to the game" in new Scope {
    val gameId: GameId = GameId("game_id")
    val sessionId: SessionId = SessionId("session_id")
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TacCell)
    val gameEngine: ClassicTicTacToe[IO] = mock[ClassicTicTacToe[IO]]

    assertThrows[UnableConnectToGameException](
      (for {
        playerInfoTryableDeferred <- Deferred.tryable[IO, PlayerInfo]
        _ <- playerInfoTryableDeferred.complete(playerInfo)
        classicGame: ClassicGame[IO] =
          ClassicGame[IO](gameId, GameStatus.Running, playerInfo, gameEngine, playerInfoTryableDeferred)
        _ = when(gameStorage.get(gameId)).thenReturn(OptionT.pure[IO](classicGame))
        _ <- service.joinGame(gameId, sessionId).rethrowT
      } yield ()).unsafeRunSync()
    )

    verify(gameStorage, only).get(*[GameId])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "join the game correctly" in new Scope {
    val gameId: GameId = GameId("game_id")
    val sessionId: SessionId = SessionId("session_id")
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TacCell)
    val gameEngine: ClassicTicTacToe[IO] = mock[ClassicTicTacToe[IO]]

    (for {
      playerInfoTryableDeferred <- Deferred.tryable[IO, PlayerInfo]
      classicGame: ClassicGame[IO] =
        ClassicGame[IO](gameId, GameStatus.Running, playerInfo, gameEngine, playerInfoTryableDeferred)
      _ = when(gameStorage.get(gameId)).thenReturn(OptionT.pure[IO](classicGame))
      _ = when(gameStorage.put(classicGame)).thenReturn(IO.unit)
      _ <- service.joinGame(gameId, sessionId).rethrowT
    } yield ()).unsafeRunSync()

    verify(gameStorage, times(1)).get(*[GameId])
    verify(gameStorage, times(1)).put(*[Game])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "throw GameNotFoundException if game doesn't exist during the makeTurn operation" in new Scope {
    val gameId: GameId = GameId("fake_id")
    val sessionId: SessionId = SessionId("fake_id")
    val position: Position = Position(Column(1), Row(1))

    when(gameStorage.get(gameId)).thenReturn(OptionT.none)

    assertThrows[GameNotFoundException](
      service.makeTurn(gameId, sessionId, position).rethrowT.unsafeRunSync()
    )

    verify(gameStorage, only).get(*[GameId])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "throw AwaitingConnectionException if the game is in the wrong state" in new Scope {
    val gameId: GameId = GameId("fake_id")
    val sessionId: SessionId = SessionId("fake_id")
    val position: Position = Position(Column(1), Row(1))
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TacCell)
    val gameEngine: ClassicTicTacToe[IO] = mock[ClassicTicTacToe[IO]]

    assertThrows[AwaitingConnectionException](
      (for {
        playerInfoTryableDeferred <- Deferred.tryable[IO, PlayerInfo]
        classicGame: ClassicGame[IO] =
          ClassicGame[IO](
            gameId,
            GameStatus.AwaitingConnection,
            playerInfo,
            gameEngine,
            playerInfoTryableDeferred
          )
        _ = when(gameStorage.get(gameId)).thenReturn(OptionT.pure[IO](classicGame))
        _ <- service.makeTurn(gameId, sessionId, position).rethrowT
      } yield ()).unsafeRunSync()
    )

    verify(gameStorage, only).get(*[GameId])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "throw GameAlreadyFinishedException if the game is in the wrong state" in new Scope {
    val gameId: GameId = GameId("fake_id")
    val sessionId: SessionId = SessionId("fake_id")
    val position: Position = Position(Column(1), Row(1))
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TacCell)
    val gameEngine: ClassicTicTacToe[IO] = mock[ClassicTicTacToe[IO]]

    assertThrows[GameExceptionWrapper](
      (for {
        playerInfoTryableDeferred <- Deferred.tryable[IO, PlayerInfo]
        classicGame: ClassicGame[IO] =
          ClassicGame[IO](gameId, GameStatus.Finished, playerInfo, gameEngine, playerInfoTryableDeferred)
        _ = when(gameStorage.get(gameId)).thenReturn(OptionT.pure[IO](classicGame))
        _ <- service.makeTurn(gameId, sessionId, position).rethrowT
      } yield ()).unsafeRunSync()
    )

    verify(gameStorage, only).get(*[GameId])
    verifyNoMoreInteractions(gameStorage)
  }

  it should "throw NotYouTurnException if the game is in the wrong state" in new Scope {
    val gameId: GameId = GameId("fake_id")
    val sessionId: SessionId = SessionId("fake_id")
    val position: Position = Position(Column(1), Row(1))
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TacCell)
    val gameEngine: ClassicTicTacToe[IO] = mock[ClassicTicTacToe[IO]]

    assertThrows[NotYouTurnException](
      (for {
        playerInfoTryableDeferred <- Deferred.tryable[IO, PlayerInfo]
        classicGame: ClassicGame[IO] =
          ClassicGame[IO](gameId, GameStatus.Running, playerInfo, gameEngine, playerInfoTryableDeferred)
        _ = when(gameStorage.get(gameId)).thenReturn(OptionT.pure[IO](classicGame))
        _ = when(gameEngine.state).thenReturn(ActiveGame(TurnNumber(1), PlayerCellType.TicCell, List.empty))
        _ <- service.makeTurn(gameId, sessionId, position).rethrowT
      } yield ()).unsafeRunSync()
    )

    verify(gameStorage, only).get(*[GameId])
    verify(gameEngine, only).state
    verifyNoMoreInteractions(gameStorage)
    verifyNoMoreInteractions(gameEngine)
  }

  it should "make turn correctly" in new Scope {
    val gameId: GameId = GameId("fake_id")
    val sessionId: SessionId = SessionId("fake_id")
    val position: Position = Position(Column(1), Row(1))
    val playerInfo: PlayerInfo = PlayerInfo(sessionId, PlayerCellType.TicCell)
    val gameEngine: ClassicTicTacToe[IO] = mock[ClassicTicTacToe[IO]]

    (for {
      playerInfoTryableDeferred <- Deferred.tryable[IO, PlayerInfo]
      classicGame: ClassicGame[IO] =
        ClassicGame[IO](gameId, GameStatus.Running, playerInfo, gameEngine, playerInfoTryableDeferred)
      _ = when(gameStorage.get(gameId)).thenReturn(OptionT.pure[IO](classicGame))
      _ = when(gameEngine.state).thenReturn(ActiveGame(TurnNumber(1), PlayerCellType.TicCell, List.empty))
      _ = when(gameEngine.makeTurn(position)).thenReturn(EitherT.pure(gameEngine))
      _ = when(gameStorage.put(classicGame)).thenReturn(IO.unit)
      _ <- service.makeTurn(gameId, sessionId, position).rethrowT
    } yield ()).unsafeRunSync()

    verify(gameStorage, times(1)).get(*[GameId])
    verify(gameEngine, times(2)).state
    verify(gameEngine, times(1)).makeTurn(*[Position])
    verify(gameStorage, times(1)).put(*[Game])
    verifyNoMoreInteractions(gameStorage)
    verifyNoMoreInteractions(gameEngine)
  }

  trait Scope {

    implicit val contextShift: ContextShift[IO] =
      IO.contextShift(ExecutionContext.Implicits.global)

    val gameStorage: GameStorage[IO] = mock[GameStorage[IO]]
    val service: GameService[IO] = GameServiceImpl(gameStorage)
  }
}
