package com.tictactoe.service.session

import com.tictactoe.model.Session
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.session.exception.SessionAlreadyOpenedException

trait SessionService[F[_], T <: Session[_]] {

  def openSession(session: T): F[Either[SessionAlreadyOpenedException, Unit]]

  def getSession(id: SessionId): F[Option[T]]

  def closeSession(id: SessionId): F[Unit]
}
