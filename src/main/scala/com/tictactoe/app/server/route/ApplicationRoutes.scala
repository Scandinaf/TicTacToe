package com.tictactoe.app.server.route

import cats.effect.Concurrent
import com.tictactoe.app.server.handler.TicTacToeMessageHandler
import com.tictactoe.app.server.middleware.auth.InternalAuthMiddleware
import com.tictactoe.app.server.route.ws.TicTacToeRoutes
import com.tictactoe.service.logging.LogOf
import org.http4s.HttpRoutes
import org.http4s.server.Router

object ApplicationRoutes {

  def apply[F[_] : Concurrent : LogOf](
    internalAuthMiddleware: InternalAuthMiddleware[F],
    incomingMessageHandler: TicTacToeMessageHandler[F]
  ): HttpRoutes[F] =
    Router(
      "/TicTacToe/ws/connect" -> internalAuthMiddleware.middleware(TicTacToeRoutes(incomingMessageHandler)),
    )
}
