package com.tictactoe.service.session

import cats.effect.IO
import cats.effect.concurrent.Ref
import cats.syntax.either._
import cats.syntax.option._
import com.tictactoe.model.Message.OutgoingMessage
import com.tictactoe.model.Session
import com.tictactoe.model.Session.{SessionId, WsSession}
import com.tictactoe.model.User.{SimpleUser, UserId}
import com.tictactoe.service.session.exception.SessionServiceException.SessionAlreadyExistsException
import com.tictactoe.storage.session.LocalSessionStorage
import fs2.concurrent.Queue
import org.mockito.scalatest.MockitoSugar
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SessionServiceImplSpec extends AnyFlatSpec with MockitoSugar with Matchers {

  "WsSessionService" should "correctly open session" in new Scope {

    (
      for {
        service <- serviceIO
        result <- service.getSession(session.id).value
        _ = result shouldBe None
        _ <- service.openSession(session).rethrowT
        result <- service.getSession(session.id).value
        _ = result shouldBe session.some
      } yield ()
    ).unsafeRunSync()
  }

  it should "throw SessionAlreadyExistsException" in new Scope {

    (
      for {
        service <- serviceIO
        _ <- service.openSession(session).rethrowT
        result <- service.openSession(session).value
        _ = result shouldBe SessionAlreadyExistsException(session.id).asLeft
      } yield ()
    ).unsafeRunSync()
  }

  it should "correctly get session" in new Scope {

    (
      for {
        service <- serviceIO
        result <- service.getSession(session.id).value
        _ = result shouldBe None
        _ <- service.openSession(session).rethrowT
        result <- service.getSession(session.id).value
        _ = result shouldBe session.some
      } yield ()
    ).unsafeRunSync()
  }

  it should "correctly close session" in new Scope {

    (
      for {
        service <- serviceIO
        _ <- service.openSession(session).rethrowT
        _ <- service.closeSession(session.id)
        result <- service.getSession(session.id).value
        _ = result shouldBe None
      } yield ()
    ).unsafeRunSync()
  }

  trait Scope {

    val session: WsSession[IO] = {

      val outgoingQueue = mock[Queue[IO, OutgoingMessage]]

      WsSession(
        id = SessionId("session_id"),
        user = SimpleUser(id = UserId(1234)),
        context = WsSession.Context[IO](outgoingQueue = outgoingQueue)
      )
    }
    val serviceIO: IO[SessionServiceImpl[IO]] = for {
      localStorage <- Ref.of[IO, Map[SessionId, Session]](Map.empty)
      sessionStorage = LocalSessionStorage[IO](localStorage)
    } yield SessionServiceImpl[IO](sessionStorage)
  }
}
