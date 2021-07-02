package com.tictactoe.service.session.exception

import com.tictactoe.exception.AppException
import cats.syntax.show._
import com.tictactoe.exception.AppException.{ErrorCode, ParameterKey, PrettyMessage}
import com.tictactoe.model.Session.SessionId

sealed trait SessionServiceException extends AppException

object SessionServiceException {

  final case class SessionAlreadyExistsException(id: SessionId) extends SessionServiceException {

    override val prettyMessage: PrettyMessage =
      PrettyMessage(show"A session with this identifier already exists. $id")

    override def parameters: Map[ParameterKey, String] = Map(ParameterKey.SessionId -> id.value)

    override def errorCode: ErrorCode = ErrorCode.internalError
  }
}
