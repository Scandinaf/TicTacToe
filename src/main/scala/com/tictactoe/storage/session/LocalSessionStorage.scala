package com.tictactoe.storage.session

import cats.Functor
import cats.data.OptionT
import cats.effect.concurrent.Ref
import com.tictactoe.model.Session
import com.tictactoe.model.Session.SessionId

class LocalSessionStorage[F[_] : Functor](localStorage: Ref[F, Map[SessionId, Session]])
  extends SessionStorage[F] {

  override def put(session: Session): F[Unit] =
    localStorage.update(_ + (session.id -> session))

  override def get(sessionId: Session.SessionId): OptionT[F, Session] =
    OptionT
      .liftF(localStorage.get)
      .subflatMap(_.get(sessionId))

  override def delete(sessionId: Session.SessionId): F[Unit] =
    localStorage.update(_ - sessionId)
}

object LocalSessionStorage {

  def apply[F[_] : Functor](localStorage: Ref[F, Map[SessionId, Session]]): LocalSessionStorage[F] =
    new LocalSessionStorage(localStorage)
}
