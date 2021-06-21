package com.tictactoe.app.server.route.ws

import cats.effect.{Async, Timer}
import com.tictactoe.model.User
import fs2.{Pipe, Stream}
import org.http4s.AuthedRoutes
import org.http4s.Method.GET
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.Text

import scala.concurrent.duration.DurationInt

private[route] object TicTacToeRoutes {

  def apply[F[_] : Async : Timer](): AuthedRoutes[User, F] = AuthedRoutes.of[User, F] {

    case GET -> Root as _ =>
      val toClient: Stream[F, WebSocketFrame] =
        Stream.awakeEvery[F](1.seconds)
          .map(d => {
            println("SEND!!!")
            Text(s"Ping! $d")
          })

      val fromClient: Pipe[F, WebSocketFrame, Unit] = _.evalMap {
        case Text(t, _) => Async[F].delay(println(t))
        case f          => Async[F].delay(println(s"Unknown type: $f"))
      }

      WebSocketBuilder[F].build(toClient, fromClient)
  }
}
