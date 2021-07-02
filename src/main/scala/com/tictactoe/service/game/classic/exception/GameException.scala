package com.tictactoe.service.game.classic.exception

import com.tictactoe.exception.AppException
import cats.syntax.show._
import com.tictactoe.exception.AppException.{ErrorCode, ParameterKey, PrettyMessage}
import com.tictactoe.model.Position
import com.tictactoe.service.game.classic.model.GameRules

sealed trait GameException extends AppException

object GameException {

  final case class CellAlreadyOccupiedException(position: Position) extends GameException {

    override def prettyMessage: PrettyMessage = PrettyMessage(show"Cell Already Occupied. $position")

    override def parameters: Map[ParameterKey, String] = Map(
      ParameterKey.Column -> position.column.value.toString,
      ParameterKey.Row -> position.row.value.toString
    )

    override def errorCode: ErrorCode = ErrorCode.badRequest
  }

  final case class PositionOutboundException(position: Position, rules: GameRules) extends GameException {

    override def prettyMessage: PrettyMessage =
      PrettyMessage(show"Passed position is outside the playing area. $position. $rules")

    override def parameters: Map[ParameterKey, String] = Map(
      ParameterKey.Column -> position.column.value.toString,
      ParameterKey.ColumnCount -> rules.columnCount.value.toString,
      ParameterKey.Row -> position.row.value.toString,
      ParameterKey.RowCount -> rules.rowCount.value.toString
    )

    override def errorCode: ErrorCode = ErrorCode.badRequest
  }

  final case object GameAlreadyFinishedException extends GameException {

    override def prettyMessage: PrettyMessage = PrettyMessage("The game is already finished")

    override def parameters: Map[ParameterKey, String] = Map.empty

    override def errorCode: ErrorCode = ErrorCode.badRequest
  }
}
