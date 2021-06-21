package com.tictactoe.app.server.middleware.auth

import cats.effect.IO
import com.tictactoe.TestImplicits
import com.tictactoe.model.User
import com.tictactoe.app.server.RoutesBaseScope
import com.tictactoe.app.server.middleware.auth.SimpleAuthMiddleware.HeaderName
import org.http4s._
import org.http4s.dsl.impl.Root
import org.http4s.dsl.io._
import org.http4s.implicits._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class SimpleAuthMiddlewareSpec extends AnyFlatSpec with Matchers {

  "SimpleAuthMiddleware" should "retrieve the userId data correctly" in new Scope {

    val response = sendRequest(
      uri = uri"/welcome",
      method = Method.GET,
      routes = routes,
      headers = Headers(List(Header(HeaderName.userId.value, "256")))
    ).unsafeRunSync()

    response.status shouldBe Ok
    response.bodyText.compile
      .string
      .unsafeRunSync() shouldBe "Welcome, UserId(256)"
  }

  it should "return badRequest if header is missing" in new Scope {

    val response = sendRequest(
      uri = uri"/welcome",
      method = Method.GET,
      routes = routes
    ).unsafeRunSync()

    response.status shouldBe BadRequest
    response.bodyText.compile
      .string
      .unsafeRunSync() shouldBe s"Couldn't find an '${HeaderName.userId}' header"
  }

  it should "return badRequest if header has the wrong type" in new Scope {

    val response = sendRequest(
      uri = uri"/welcome",
      method = Method.GET,
      routes = routes,
      headers = Headers(List(Header(HeaderName.userId.value, "fake_text")))
    ).unsafeRunSync()

    response.status shouldBe BadRequest
    response.bodyText.compile
      .string
      .unsafeRunSync() shouldBe s"The passed header value '${HeaderName.userId}' has invalid type, a numeric value is expected."
  }

  trait Scope extends RoutesBaseScope with TestImplicits {

    val routes: HttpRoutes[IO] = {

      val middleware = SimpleAuthMiddleware[IO]().middleware
      val authedRoutes: AuthedRoutes[User, IO] = {
        AuthedRoutes.of {
          case GET -> Root / "welcome" as user => Ok(s"Welcome, ${user.id}")
        }
      }

      middleware(authedRoutes)
    }
  }
}
