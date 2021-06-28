package com.tictactoe.model

import com.tictactoe.model.Message.OutgoingMessage
import com.tictactoe.model.Session.SessionId
import fs2.concurrent.Queue

sealed trait Session[Context] {

  def id: SessionId

  def user: User

  def context: Context
}

object Session {

  final case class SessionId(value: String) extends AnyVal

  final case class WsSession[F[_]](
    id: SessionId,
    user: User,
    context: WsSession.Context[F]
  ) extends Session[WsSession.Context[F]]

  object WsSession {

    final case class Context[F[_]](outgoingQueue: Queue[F, OutgoingMessage])
  }
}
