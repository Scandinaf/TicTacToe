package com.tictactoe.model

import com.tictactoe.model.User.UserId

sealed trait User {

  def id: UserId
}

object User {

  final case class UserId(value: Long) extends AnyVal

  final case class SimpleUser(id: UserId) extends User
}
