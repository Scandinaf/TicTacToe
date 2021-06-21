package com.tictactoe

import cats.Show
import com.tictactoe.model.Message.OutgoingMessage
import org.http4s.{Headers, Request}

package object app {

  object ShowOps {

    implicit val errorShow: Show[OutgoingMessage.Error] =
      error =>
        s"ErrorType - ${error.errorType}; Reason - ${error.reason.value}; MessageId - ${error.messageId}"
  }

  object CompanionOps {

    implicit class RequestCompanion[F[_]](req: Request[F]) {

      val showHeaders: String =
        req.headers
          .redactSensitive(Headers.SensitiveHeaders.contains)
          .toList
          .mkString("Headers(", ", ", ")")

      val show: String =
        s"""
           |-------------------->
           |-------------------->
           |-------------------->
           |${req.httpVersion} ${req.method} ${req.uri} $showHeaders"""
          .stripMargin
    }
  }
}
