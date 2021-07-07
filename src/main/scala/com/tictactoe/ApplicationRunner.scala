package com.tictactoe

import cats.effect.concurrent.Ref
import cats.effect.{Blocker, ConcurrentEffect, ContextShift, Resource, Timer}
import cats.{ApplicativeThrow, Parallel}
import com.tictactoe.app.server.ServerModule
import com.tictactoe.app.server.handler.TicTacToeMessageHandlerImpl
import com.tictactoe.app.server.middleware.auth.SimpleAuthMiddleware
import com.tictactoe.app.server.route.ApplicationRoutes
import com.tictactoe.model.Game.GameId
import com.tictactoe.model.Session.SessionId
import com.tictactoe.model.{Game, Session}
import com.tictactoe.service.config.PureConfigReader
import com.tictactoe.service.game.GameServiceImpl
import com.tictactoe.service.gamelog.DBGameLog
import com.tictactoe.service.logging.LogOf
import com.tictactoe.service.notification.NotificationServiceImpl
import com.tictactoe.service.pingpong.SimplePingPongService
import com.tictactoe.service.session.SessionServiceImpl
import com.tictactoe.storage.DBModule
import com.tictactoe.storage.game.LocalGameStorage
import com.tictactoe.storage.gameInfo.PostgreSqlGameInfoStorage
import com.tictactoe.storage.session.LocalSessionStorage
import pureconfig.ConfigSource

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ApplicationRunner {

  def run[F[+_] : Timer : ConcurrentEffect : Parallel : ContextShift](): Resource[F, Unit] = for {
    implicit0(logOf: LogOf[F]) <- Resource.eval(LogOf.slf4j[F])
    logger <- Resource.eval(logOf(Main.getClass))

    configReader = PureConfigReader(ConfigSource.defaultApplication)
    appConfig <- Resource.eval(ApplicativeThrow[F].fromEither(configReader.readAppConfig()))

    _ <- Resource.eval(logger.info("Trying to run db"))
    transactor <- DBModule.of(appConfig.db)
    gameLog = DBGameLog(PostgreSqlGameInfoStorage, transactor)

    _ <- Resource.eval(logger.info("Trying to run web-server"))
    httpServerBlockingContext = Blocker.liftExecutionContext(
      ExecutionContext.fromExecutorService(
        Executors.newCachedThreadPool()
      )
    )
    gameService <- {
      for {
        gameLocalStorage <- Resource.eval(Ref.of[F, Map[GameId, Ref[F, Game]]](Map.empty))
        gameStorage = LocalGameStorage(gameLocalStorage)
      } yield GameServiceImpl(gameStorage)
    }
    sessionService <- {
      for {
        sessionLocalStorage <- Resource.eval(Ref.of[F, Map[SessionId, Session]](Map.empty))
        sessionStorage = LocalSessionStorage(sessionLocalStorage)
      } yield SessionServiceImpl(sessionStorage)
    }
    notificationService = NotificationServiceImpl(sessionService, gameService)
    applicationRoutes = {

      val pingPongService = SimplePingPongService()
      val ticTacToeMessageHandler =
        TicTacToeMessageHandlerImpl(pingPongService, gameService, notificationService, gameLog)

      val internalAuthMiddleware = SimpleAuthMiddleware()

      ApplicationRoutes(
        internalAuthMiddleware,
        ticTacToeMessageHandler,
        sessionService,
        appConfig.server.timeout.idleTimeout
      )
    }
    _ <- ServerModule.of(appConfig.server, applicationRoutes, httpServerBlockingContext)
  } yield ()
}
