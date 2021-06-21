package com.tictactoe.app.server.route

import cats.effect.{Async, Timer}
import com.tictactoe.app.server.middleware.auth.InternalAuthMiddleware
import com.tictactoe.app.server.route.ws.TicTacToeRoutes
import org.http4s.HttpRoutes
import org.http4s.server.Router

object ApplicationRoutes {

  def apply[F[_] : Async : Timer](internalAuthMiddleware: InternalAuthMiddleware[F]): HttpRoutes[F] =
    Router[F](
      "/TicTacToe/ws" -> internalAuthMiddleware.middleware(TicTacToeRoutes()),
    )
}
