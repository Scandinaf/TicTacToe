package com.tictactoe

import org.http4s.{Headers, Request}

package object app {

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
