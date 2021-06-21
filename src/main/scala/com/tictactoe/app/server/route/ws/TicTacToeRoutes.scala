package com.tictactoe.app.server.route.ws

import cats.ApplicativeThrow
import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import com.tictactoe.app.json._
import com.tictactoe.app.ShowOps._
import com.tictactoe.app.server.handler.TicTacToeMessageHandler
import com.tictactoe.model.AppConfig.ServerConfig.Timeout.IdleTimeout
import com.tictactoe.model.Message.OutgoingMessage.Error
import com.tictactoe.model.Message.OutgoingMessage.Error.{ErrorType, Reason}
import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage}
import com.tictactoe.model.User
import com.tictactoe.service.logging.{Log, LogOf}
import fs2.concurrent.Queue
import fs2.{Pipe, Stream}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import org.http4s.AuthedRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

private[route] object TicTacToeRoutes {

  def apply[F[_] : Concurrent : Timer : LogOf](
    handler: TicTacToeMessageHandler[F],
    idleTimeout: IdleTimeout
  ): AuthedRoutes[User, F] =
    AuthedRoutes.of[User, F] {

      case GET -> Root as _ =>
        def buildOutgoingStream(
          outgoingQueue: Queue[F, WebSocketFrame],
          messageCounter: Ref[F, Int]
        ): Stream[F, WebSocketFrame] =
          outgoingQueue
            .dequeue
            .concurrently(
              Stream.awakeEvery[F](idleTimeout.value)
                .evalTap(_ =>
                  for {
                    messageCount <- messageCounter.getAndSet(0)
                    _ <- outgoingQueue.offer1(Close()).whenA(messageCount <= 0)
                  } yield ()
                )
            )

        def buildIncomingPipe(
          outgoingQueue: Queue[F, WebSocketFrame],
          messageCounter: Ref[F, Int]
        )(implicit
          logger: Log[F]
        ): Pipe[F, WebSocketFrame, Unit] = {

          def toTextFrame(outgoingMessage: OutgoingMessage): Text =
            Text(outgoingMessage.asJson.noSpaces)

          _.evalTap(_ => messageCounter.update(_ + 1))
            .evalMap {

              case Text(json, _) =>
                for {
                  outgoingMessage <-
                    decode[IncomingMessage](json)
                      .fold(
                        circeError => {
                          val error = Error(
                            errorType = ErrorType.TransmittedDataError,
                            reason = Reason(s"Failed to parse transferred data. Transmitted Json - $json"),
                            messageId = None
                          )

                          logger
                            .error(show"$error", circeError)
                            .as(error)
                        },
                        incomingMessage =>
                          ApplicativeThrow[F]
                            .recoverWith(handler.handle(incomingMessage)) {

                              case throwable: Throwable =>
                                val error = Error(
                                  errorType = ErrorType.InternalError,
                                  reason =
                                    Reason(s"During the processing of the message there were problems"),
                                  messageId = incomingMessage.messageId
                                )

                                logger
                                  .error(show"$error", throwable)
                                  .as(error)
                            }
                      )
                  _ <- outgoingQueue.offer1(toTextFrame(outgoingMessage))
                } yield ()

              case _: Close =>
                logger.warn("A message was received that the connection was closed")

              case unknownFrame =>
                val error = Error(
                  errorType = ErrorType.TransmittedDataError,
                  reason =
                    Reason(s"The data transmitted doesn't match the accepted format. Unknown frame - $unknownFrame"),
                  messageId = None
                )

                for {
                  _ <- logger.warn(show"$error")
                  _ <- outgoingQueue.offer1(toTextFrame(error))
                } yield ()
            }
        }

        for {
          implicit0(logger: Log[F]) <- implicitly[LogOf[F]].apply(this.getClass)

          messageCounter <- Ref.of[F, Int](0)

          outgoingQueue <- Queue.unbounded[F, WebSocketFrame]
          outgoingStream = buildOutgoingStream(outgoingQueue, messageCounter)
          incomingPipe = buildIncomingPipe(outgoingQueue, messageCounter)

          webSocketConnection <-
            WebSocketBuilder[F].build(outgoingStream, incomingPipe, filterPingPongs = false)
        } yield webSocketConnection
    }
}
