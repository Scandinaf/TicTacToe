package com.tictactoe.app.server.middleware.auth

import cats.data.Kleisli
import cats.effect.Async
import cats.syntax.either._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.{Applicative, ApplicativeThrow}
import com.tictactoe.app.CompanionOps._
import com.tictactoe.app.server.middleware.auth.SimpleAuthMiddleware.HeaderName
import com.tictactoe.model.User
import com.tictactoe.model.User.{SimpleUser, UserId}
import com.tictactoe.service.logging.LogOf
import org.http4s.dsl.io._
import org.http4s.server.AuthMiddleware
import org.http4s.syntax.string._
import org.http4s.util.CaseInsensitiveString
import org.http4s.{ContextRoutes, Request, Response, Status}

class SimpleAuthMiddleware[F[_] : Async : LogOf] extends InternalAuthMiddleware[F] {

  override def middleware: AuthMiddleware[F, User] = {

    type ErrorMsg = String
    val authUser: Kleisli[F, Request[F], Either[ErrorMsg, User]] =
      Kleisli { request =>
        for {
          logger <- implicitly[LogOf[F]].apply(this.getClass)
          simpleUser <-
            ApplicativeThrow[F].onError(
              Async[F].delay {
                for {
                  userIdHeader <- request.headers
                    .get(HeaderName.userId)
                    .toRight(s"Couldn't find an '${HeaderName.userId}' header")
                  userId <-
                    Either.catchOnly[NumberFormatException](userIdHeader.value.toLong)
                      .leftMap(_ =>
                        s"The passed header value '${HeaderName.userId}' has invalid type, a numeric value is expected."
                      )
                } yield SimpleUser(id = UserId(userId))
              }
            ) {
              case throwable =>
                logger.error(
                  s"""${request.show}
                     |The authorization attempt ended with an exceptional situation.""".stripMargin,
                  throwable
                )
            }
        } yield simpleUser
      }

    val onFailure = ContextRoutes.of[ErrorMsg, F] {
      case _ as errorMsg =>
        Applicative[F].pure(
          Response[F](Status.BadRequest)
            .withEntity(errorMsg)
        )
    }

    AuthMiddleware(authUser, onFailure)
  }
}

object SimpleAuthMiddleware {

  object HeaderName {

    val userId: CaseInsensitiveString = "UserId".ci
  }

  def apply[F[_] : Async : LogOf](): SimpleAuthMiddleware[F] =
    new SimpleAuthMiddleware
}
