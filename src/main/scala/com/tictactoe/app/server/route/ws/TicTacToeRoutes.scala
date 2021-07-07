package com.tictactoe.app.server.route.ws

import cats.effect.concurrent.Ref
import cats.effect.{Concurrent, Timer}
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.show._
import cats.{Applicative, ApplicativeThrow}
import com.tictactoe.app.server.JsonOps._
import com.tictactoe.app.server.handler.TicTacToeMessageHandler
import com.tictactoe.exception.AppException
import com.tictactoe.exception.AppException.{ErrorCode, PrettyMessage}
import com.tictactoe.model.AppConfig.ServerConfig.Timeout.IdleTimeout
import com.tictactoe.model.Message.OutgoingMessage.Error
import com.tictactoe.model.Message.{IncomingMessage, OutgoingMessage}
import com.tictactoe.model.Session.{SessionId, WsSession}
import com.tictactoe.model.User
import com.tictactoe.service.logging.{Log, LogOf}
import com.tictactoe.service.session.SessionService
import fs2.concurrent.Queue
import fs2.{Pipe, Stream}
import io.circe.parser.decode
import io.circe.syntax.EncoderOps
import io.jvm.uuid._
import org.http4s.AuthedRoutes
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.server.websocket.WebSocketBuilder
import org.http4s.websocket.WebSocketFrame
import org.http4s.websocket.WebSocketFrame.{Close, Text}

private[route] object TicTacToeRoutes {

  def apply[F[_] : Concurrent : Timer : LogOf](
    handler: TicTacToeMessageHandler[F],
    sessionService: SessionService[F],
    idleTimeout: IdleTimeout
  ): AuthedRoutes[User, F] =
    AuthedRoutes.of[User, F] {

      case GET -> Root as user =>
        def buildOutgoingStream(
          outgoingWebSocketFrameQueue: Queue[F, WebSocketFrame],
          outgoingMessageQueue: Queue[F, OutgoingMessage],
          messageCounter: Ref[F, Int]
        ): Stream[F, WebSocketFrame] =
          outgoingWebSocketFrameQueue
            .dequeue
            .merge(
              outgoingMessageQueue.dequeue
                .map(outgoingMessage => Text(outgoingMessage.asJson.noSpaces))
            ).concurrently(
              Stream.awakeEvery[F](idleTimeout.value)
                .evalTap(_ =>
                  for {
                    messageCount <- messageCounter.getAndSet(0)
                    _ <- outgoingWebSocketFrameQueue.offer1(Close()).whenA(messageCount <= 0)
                  } yield ()
                )
            )

        def buildIncomingPipe(
          outgoingMessageQueue: Queue[F, OutgoingMessage],
          messageCounter: Ref[F, Int]
        )(implicit
          logger: Log[F],
          sessionId: SessionId
        ): Pipe[F, WebSocketFrame, Unit] =
          _.evalTap(_ => messageCounter.update(_ + 1))
            .evalMap {

              case Text(json, _) =>
                decode[IncomingMessage](json)
                  .fold(
                    circeError => {
                      val error = Error(
                        messageId = None,
                        prettyMessage =
                          PrettyMessage(s"Failed to parse transferred data. Transmitted Json - $json."),
                        parameters = Map.empty,
                        errorCode = ErrorCode.badRequest
                      )

                      logger.error(show"$error", circeError) *>
                        outgoingMessageQueue.offer1(error).as(())
                    },
                    incomingMessage =>
                      ApplicativeThrow[F]
                        .handleErrorWith(handler.handle(incomingMessage).as(())) {
                          case appException: AppException =>
                            val error = Error(
                              messageId = incomingMessage.messageId,
                              prettyMessage = appException.prettyMessage,
                              parameters = appException.parameters,
                              errorCode = appException.errorCode
                            )

                            logger.error(show"$error", appException) *>
                              outgoingMessageQueue.offer1(error).as(())

                          case throwable: Throwable =>
                            val error = Error(
                              messageId = incomingMessage.messageId,
                              prettyMessage =
                                PrettyMessage("During the processing of the message there were problems"),
                              parameters = Map.empty,
                              errorCode = ErrorCode.internalError
                            )

                            logger.error(show"$error", throwable) *>
                              outgoingMessageQueue.offer1(error).as(())
                        }
                  )

              case _: Close =>
                for {
                  _ <- logger.warn("A message was received that the connection was closed")
                  _ <- sessionService.closeSession(sessionId)
                } yield ()

              case unknownFrame =>
                val error = Error(
                  messageId = None,
                  prettyMessage =
                    PrettyMessage(s"The data transmitted doesn't match the accepted format. Unknown frame - $unknownFrame"),
                  parameters = Map.empty,
                  errorCode = ErrorCode.badRequest
                )

                for {
                  _ <- logger.warn(show"$error")
                  _ <- outgoingMessageQueue.offer1(error)
                } yield ()
            }

        for {
          implicit0(logger: Log[F]) <- implicitly[LogOf[F]].apply(this.getClass)
          implicit0(sessionId: SessionId) <- Applicative[F].pure(SessionId(UUID.random.string))

          messageCounter <- Ref.of[F, Int](0)

          outgoingWebSocketFrameQueue <- Queue.unbounded[F, WebSocketFrame]
          outgoingMessageQueue <- Queue.unbounded[F, OutgoingMessage]

          session = WsSession(
            id = sessionId,
            user = user,
            context = WsSession.Context(outgoingMessageQueue)
          )
          _ <- sessionService.openSession(session).rethrowT

          outgoingStream =
            buildOutgoingStream(outgoingWebSocketFrameQueue, outgoingMessageQueue, messageCounter)
          incomingPipe = buildIncomingPipe(outgoingMessageQueue, messageCounter)

          webSocketConnection <-
            WebSocketBuilder[F].build(outgoingStream, incomingPipe, filterPingPongs = false)
        } yield webSocketConnection
    }
}
