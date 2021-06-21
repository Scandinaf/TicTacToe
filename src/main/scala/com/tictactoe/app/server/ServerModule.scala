package com.tictactoe.app.server

import cats.effect.{Blocker, ConcurrentEffect, Resource, Timer}
import com.tictactoe.model.AppConfig.ServerConfig
import org.http4s.HttpRoutes
import org.http4s.server.Server
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.syntax.kleisli._

object ServerModule {

  def of[F[_] : ConcurrentEffect : Timer](
    config: ServerConfig,
    routes: HttpRoutes[F],
    blocker: Blocker
  ): Resource[F, Server[F]] =
    BlazeServerBuilder(blocker.blockingContext)
      .bindHttp(port = config.port.value, host = config.host.value)
      .withIdleTimeout(config.timeout.idleTimeout.value)
      .withHttpApp(routes.orNotFound)
      .resource
}
