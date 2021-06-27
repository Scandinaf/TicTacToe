package com.tictactoe.service.session.exception

import com.tictactoe.model.Session.SessionId

final case class SessionAlreadyOpenedException(id: SessionId) extends SessionServiceException
