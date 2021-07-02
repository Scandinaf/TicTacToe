package com.tictactoe.service.session

import cats.data.{EitherT, OptionT}
import com.tictactoe.model.Session
import com.tictactoe.model.Session.SessionId
import com.tictactoe.service.session.exception.SessionServiceException.SessionAlreadyExistsException

trait SessionService[F[_]] {

  def openSession(session: Session): EitherT[F, SessionAlreadyExistsException, Unit]

  def getSession(id: SessionId): OptionT[F, Session]

  def closeSession(id: SessionId): F[Unit]
}
