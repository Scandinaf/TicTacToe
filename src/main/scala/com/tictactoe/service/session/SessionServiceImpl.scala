package com.tictactoe.service.session

import cats.Monad
import cats.data.{EitherT, OptionT}
import com.tictactoe.model.Session
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.session.exception.SessionServiceException.SessionAlreadyExistsException
import com.tictactoe.storage.session.SessionStorage

class SessionServiceImpl[F[_] : Monad](sessionStorage: SessionStorage[F])
  extends SessionService[F] {

  override def openSession(session: Session): EitherT[F, SessionAlreadyExistsException, Unit] =
    getSession(session.id)
      .toLeftF(sessionStorage.put(session))
      .leftMap(_ => SessionAlreadyExistsException(session.id))

  override def getSession(id: SessionId): OptionT[F, Session] =
    sessionStorage.get(id)

  override def closeSession(id: SessionId): F[Unit] =
    sessionStorage.delete(id)
}

object SessionServiceImpl {

  def apply[F[_] : Monad](sessionStorage: SessionStorage[F]): SessionServiceImpl[F] =
    new SessionServiceImpl(sessionStorage)
}
