package com.tictactoe.app.server.middleware.auth

import com.tictactoe.model.User
import org.http4s.server.AuthMiddleware

trait InternalAuthMiddleware[F[_]] {

  def middleware: AuthMiddleware[F, User]
}
