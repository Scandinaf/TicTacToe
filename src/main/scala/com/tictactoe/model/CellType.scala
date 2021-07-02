package com.tictactoe.model

import enumeratum.{CirceEnum, EnumEntry, _}

import scala.util.Random

sealed trait CellType

object CellType {

  final case object EmptyCell extends CellType

  sealed trait PlayerCellType extends CellType with EnumEntry

  object PlayerCellType extends Enum[PlayerCellType] with CirceEnum[PlayerCellType] {

    implicit class PlayerCellTypeCompanion(playerCellType: PlayerCellType) {

      def swap(): PlayerCellType =
        playerCellType match {
          case TicCell => TacCell
          case TacCell => TicCell
        }
    }

    def random(): PlayerCellType =
      Random.between(1, 3) match {
        case 1 => TicCell
        case _ => TacCell
      }

    final case object TicCell extends PlayerCellType

    final case object TacCell extends PlayerCellType

    override def values: IndexedSeq[PlayerCellType] = findValues
  }
}
