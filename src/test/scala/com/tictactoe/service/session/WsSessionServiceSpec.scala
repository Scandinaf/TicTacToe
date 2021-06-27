package com.tictactoe.service.session

import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.syntax.either._
import com.tictactoe.model.Session.{SessionId, WsSession}
import com.tictactoe.model.User.{SimpleUser, UserId}
import com.tictactoe.storage.session.WsSessionStorage
import fs2.concurrent.Queue
import org.http4s.websocket.WebSocketFrame
import org.mockito.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import cats.syntax.option._
import com.tictactoe.service.session.exception.SessionAlreadyOpenedException

class WsSessionServiceSpec extends AnyFlatSpec with MockitoSugar with Matchers {

  "WsSessionService" should "correctly open session" in new Scope {

    (
      for {
        service <- serviceIO
        result <- service.getSession(session.id)
        _ = result shouldBe None
        _ <- service.openSession(session)
        result <- service.getSession(session.id)
        _ = result shouldBe session.some
      } yield ()
    ).unsafeRunSync()
  }

  it should "throw SessionAlreadyOpenedException" in new Scope {

    (
      for {
        service <- serviceIO
        _ <- service.openSession(session)
        result <- service.openSession(session)
        _ = result shouldBe SessionAlreadyOpenedException(session.id).asLeft
      } yield ()
    ).unsafeRunSync()
  }

  it should "correctly get session" in new Scope {

    (
      for {
        service <- serviceIO
        result <- service.getSession(session.id)
        _ = result shouldBe None
        _ <- service.openSession(session)
        result <- service.getSession(session.id)
        _ = result shouldBe session.some
      } yield ()
    ).unsafeRunSync()
  }

  it should "correctly close session" in new Scope {

    (
      for {
        service <- serviceIO
        _ <- service.openSession(session)
        _ <- service.closeSession(session.id)
        result <- service.getSession(session.id)
        _ = result shouldBe None
      } yield ()
    ).unsafeRunSync()
  }

  trait Scope {

    val session: WsSession[IO] = {

      val outgoingQueue = mock[Queue[IO, WebSocketFrame]]

      WsSession(
        id = SessionId("session_id"),
        user = SimpleUser(id = UserId(1234)),
        context = WsSession.Context[IO](outgoingQueue = outgoingQueue)
      )
    }
    val serviceIO: IO[WsSessionService[IO]] = for {
      localStorage <- Ref.of[IO, Map[SessionId, WsSession[IO]]](Map.empty)
      sessionStorage = WsSessionStorage[IO](localStorage)
    } yield WsSessionService[IO](sessionStorage)
  }
}
