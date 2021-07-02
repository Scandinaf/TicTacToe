package com.tictactoe.model

import cats.Show
import com.tictactoe.model.Message.OutgoingMessage
import com.tictactoe.model.Session.SessionId
import fs2.concurrent.Queue

sealed trait Session {

  def id: SessionId
}

object Session {

  final case class SessionId(value: String) extends AnyVal

  object SessionId {

    implicit val show: Show[SessionId] =
      sessionId =>
        s"Session id - ${sessionId.value}"
  }

  final case class WsSession[F[_]](
    id: SessionId,
    user: User,
    context: WsSession.Context[F]
  ) extends Session

  object WsSession {

    final case class Context[F[_]](outgoingQueue: Queue[F, OutgoingMessage])
  }
}
