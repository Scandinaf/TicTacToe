package com.tictactoe.storage.session

import cats.Functor
import cats.effect.concurrent.Ref
import cats.syntax.functor._
import com.tictactoe.model.Session
import com.tictactoe.model.Session.{SessionId, WsSession}

class WsSessionStorage[F[_] : Functor](
  localStorage: Ref[F, Map[SessionId, WsSession[F]]]
) extends SessionStorage[F, WsSession[F]] {

  override def put(session: WsSession[F]): F[Unit] =
    localStorage.update(_ + (session.id -> session))

  override def get(sessionId: Session.SessionId): F[Option[WsSession[F]]] =
    for {
      map <- localStorage.get
    } yield map.get(sessionId)

  override def delete(sessionId: Session.SessionId): F[Unit] =
    localStorage.update(_ - sessionId)
}

object WsSessionStorage {

  def apply[F[_] : Functor](localStorage: Ref[F, Map[SessionId, WsSession[F]]]): WsSessionStorage[F] =
    new WsSessionStorage(localStorage)
}
