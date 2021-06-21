package com.tictactoe.model

import com.tictactoe.model.Message.UUID

sealed trait Message {

  def messageId: UUID
}

object Message {

  final case class UUID(value: String) extends AnyVal

  sealed trait IncomingMessage extends Message

  sealed trait OutgoingMessage extends Message

  final case class Ping(messageId: UUID) extends IncomingMessage with OutgoingMessage

  final case class Pong(messageId: UUID) extends OutgoingMessage with IncomingMessage
}
