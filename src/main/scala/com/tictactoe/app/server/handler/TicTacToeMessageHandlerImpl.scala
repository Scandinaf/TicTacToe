package com.tictactoe.app.server.handler

import cats.effect.Concurrent
import cats.effect.concurrent.Deferred
import cats.syntax.flatMap._
import cats.syntax.functor._
import com.tictactoe.model.CellType.PlayerCellType
import com.tictactoe.model.Game.ClassicGame.{GameStatus, PlayerInfo}
import com.tictactoe.model.Game.{ClassicGame, GameId}
import com.tictactoe.model.Message
import com.tictactoe.model.Message.IncomingMessage.{CreateClassicGame, JoinGame, MakeTurn, Ping}
import com.tictactoe.model.Message.OutgoingMessage
import com.tictactoe.model.Message.OutgoingMessage.GameFinished.WinnerInfo
import com.tictactoe.model.Message.OutgoingMessage.{
  ClassicGameCreated,
  GameFinished,
  PlayerJoinedToGame,
  TurnResult
}
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.game.GameService
import com.tictactoe.service.game.classic.ClassicTicTacToe
import com.tictactoe.service.game.classic.model.GameState
import com.tictactoe.service.notification.NotificationService
import com.tictactoe.service.pingpong.PingPongService
import io.jvm.uuid._

class TicTacToeMessageHandlerImpl[F[+_] : Concurrent](
  pingPongService: PingPongService[F],
  classicGameService: GameService[F],
  notificationService: NotificationService[F]
) extends TicTacToeMessageHandler[F] {

  override def handle(
    incomingMessage: Message.IncomingMessage
  )(implicit
    sessionId: SessionId
  ): F[OutgoingMessage] =
    incomingMessage match {

      case ping: Ping =>
        pingPongService.pong(ping)

      case CreateClassicGame(messageId) =>
        for {
          player2SessionIdDef <- Deferred.tryable[F, PlayerInfo]
          player1Info = PlayerInfo(sessionId, PlayerCellType.random())
          game = ClassicGame[F](
            id = GameId(UUID.random.string),
            status = GameStatus.AwaitingConnection,
            player1 = player1Info,
            gameEngine = ClassicTicTacToe[F](),
            player2 = player2SessionIdDef
          )
          _ <- classicGameService.createGame(game).rethrowT
          outgoingMessage = ClassicGameCreated(game.id, messageId, player1Info.cellType)
          _ <- notificationService.notify(game.id, outgoingMessage)
        } yield outgoingMessage

      case JoinGame(messageId, gameId) =>
        for {
          playerCellType <- classicGameService.joinGame(gameId, sessionId).rethrowT
          outgoingMessage = PlayerJoinedToGame(messageId, gameId, playerCellType)
          _ <- notificationService.notify(gameId, outgoingMessage)
        } yield outgoingMessage

      case MakeTurn(messageId, gameId, position) =>
        for {
          game <- classicGameService.makeTurn(gameId, sessionId, position).rethrowT
          outgoingMessage = game match {

            case ClassicGame(_, _, _, gameEngine, _) =>
              gameEngine.state match {

                case GameState.ActiveGame(_, _, _) =>
                  TurnResult(messageId, gameId, position)

                case GameState.FinishedGame(_, turnType, _, winningCombination) =>
                  val maybeWinnerInfo = winningCombination
                    .map(winningCombination => WinnerInfo(turnType, winningCombination))

                  GameFinished(messageId, gameId, position, maybeWinnerInfo)
              }
          }
          _ <- notificationService.notify(gameId, outgoingMessage)
        } yield outgoingMessage
    }
}

object TicTacToeMessageHandlerImpl {

  def apply[F[+_] : Concurrent](
    pingPongService: PingPongService[F],
    classicGameService: GameService[F],
    notificationService: NotificationService[F]
  ): TicTacToeMessageHandlerImpl[F] =
    new TicTacToeMessageHandlerImpl(pingPongService, classicGameService, notificationService)
}
