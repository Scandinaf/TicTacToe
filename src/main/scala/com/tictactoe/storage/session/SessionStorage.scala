package com.tictactoe.storage.session

import cats.data.OptionT
import com.tictactoe.model.Session
import com.tictactoe.model.Session.SessionId

trait SessionStorage[F[_]] {

  def put(session: Session): F[Unit]

  def get(sessionId: SessionId): OptionT[F, Session]

  def delete(sessionId: SessionId): F[Unit]
}
