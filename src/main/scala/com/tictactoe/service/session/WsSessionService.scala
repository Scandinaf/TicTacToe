package com.tictactoe.service.session

import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, Monad}
import com.tictactoe.model.Session.{SessionId, WsSession}
import com.tictactoe.service.session.exception.SessionAlreadyOpenedException
import com.tictactoe.storage.session.SessionStorage

class WsSessionService[F[_] : Monad](sessionStorage: SessionStorage[F, WsSession[F]])
  extends SessionService[F, WsSession[F]] {

  override def openSession(session: WsSession[F]): F[Either[SessionAlreadyOpenedException, Unit]] =
    for {
      maybeSession <- sessionStorage.get(session.id)
      result <- maybeSession match {

        case Some(_) =>
          Applicative[F].pure(
            SessionAlreadyOpenedException(session.id)
              .asLeft
          )

        case None =>
          sessionStorage.put(session)
            .map(_.asRight)
      }
    } yield result

  override def getSession(id: SessionId): F[Option[WsSession[F]]] =
    sessionStorage.get(id)

  override def closeSession(id: SessionId): F[Unit] =
    sessionStorage.delete(id)
}

object WsSessionService {

  def apply[F[_] : Monad](sessionStorage: SessionStorage[F, WsSession[F]]): WsSessionService[F] =
    new WsSessionService(sessionStorage)
}
