package com.tictactoe.exception

import com.tictactoe.exception.AppException.{ErrorCode, ParameterKey, PrettyMessage}
import enumeratum._

trait AppException extends RuntimeException {

  def prettyMessage: PrettyMessage
  def parameters: Map[ParameterKey, String]
  def errorCode: ErrorCode
}

object AppException {

  final case class PrettyMessage(value: String) extends AnyVal
  final case class ErrorCode(value: Int) extends AnyVal

  object ErrorCode {

    val badRequest = ErrorCode(400)
    val internalError = ErrorCode(500)
  }

  sealed trait ParameterKey extends EnumEntry
  object ParameterKey extends Enum[ParameterKey] with CirceEnum[ParameterKey] {

    case object SessionId extends ParameterKey
    case object Column extends ParameterKey
    case object ColumnCount extends ParameterKey
    case object Row extends ParameterKey
    case object RowCount extends ParameterKey
    case object GameId extends ParameterKey

    override def values: IndexedSeq[ParameterKey] = findValues
  }
}
