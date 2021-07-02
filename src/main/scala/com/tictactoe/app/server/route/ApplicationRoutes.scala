package com.tictactoe.app.server.route

import cats.effect.{Concurrent, Timer}
import com.tictactoe.app.server.handler.TicTacToeMessageHandler
import com.tictactoe.app.server.middleware.auth.InternalAuthMiddleware
import com.tictactoe.app.server.route.ws.TicTacToeRoutes
import com.tictactoe.model.AppConfig.ServerConfig.Timeout.IdleTimeout
import com.tictactoe.service.logging.LogOf
import com.tictactoe.service.session.SessionService
import org.http4s.HttpRoutes
import org.http4s.server.Router

object ApplicationRoutes {

  def apply[F[_] : Concurrent : Timer : LogOf](
    internalAuthMiddleware: InternalAuthMiddleware[F],
    incomingMessageHandler: TicTacToeMessageHandler[F],
    sessionService: SessionService[F],
    idleTimeout: IdleTimeout
  ): HttpRoutes[F] =
    Router(
      "/TicTacToe/ws/connect" ->
        internalAuthMiddleware.middleware(
          TicTacToeRoutes(incomingMessageHandler, sessionService, idleTimeout)
        ),
    )
}
