package com.tictactoe

import cats.ApplicativeThrow
import cats.effect.{Blocker, ConcurrentEffect, Resource, Timer}
import com.tictactoe.app.server.ServerModule
import com.tictactoe.app.server.handler.TicTacToeMessageHandlerImpl
import com.tictactoe.app.server.middleware.auth.SimpleAuthMiddleware
import com.tictactoe.app.server.route.ApplicationRoutes
import com.tictactoe.service.config.PureConfigReader
import com.tictactoe.service.logging.LogOf
import com.tictactoe.service.pingpong.SimplePingPongService
import pureconfig.ConfigSource

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object ApplicationRunner {

  def run[F[+_] : Timer : ConcurrentEffect](): Resource[F, Unit] = for {
    implicit0(logOf: LogOf[F]) <- Resource.eval(LogOf.slf4j[F])
    logger <- Resource.eval(logOf(Main.getClass))

    configReader = PureConfigReader(ConfigSource.defaultApplication)
    appConfig <- Resource.eval(ApplicativeThrow[F].fromEither(configReader.readAppConfig()))

    _ <- Resource.eval(logger.info("Trying to run web-server"))
    httpServerBlockingContext = Blocker.liftExecutionContext(
      ExecutionContext.fromExecutorService(
        Executors.newCachedThreadPool()
      )
    )
    applicationRoutes = {

      val pingPongService = SimplePingPongService()
      val ticTacToeMessageHandler = TicTacToeMessageHandlerImpl(pingPongService)

      val internalAuthMiddleware = SimpleAuthMiddleware()

      ApplicationRoutes(
        internalAuthMiddleware,
        ticTacToeMessageHandler,
        appConfig.server.timeout.idleTimeout
      )
    }
    _ <- ServerModule.of(appConfig.server, applicationRoutes, httpServerBlockingContext)
  } yield ()
}
