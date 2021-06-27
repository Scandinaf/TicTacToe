package com.tictactoe.storage.session

import com.tictactoe.model.Session
import com.tictactoe.model.Session.SessionId

trait SessionStorage[F[_], T <: Session[_]] {

  def put(session: T): F[Unit]

  def get(sessionId: SessionId): F[Option[T]]

  def delete(sessionId: SessionId): F[Unit]
}
