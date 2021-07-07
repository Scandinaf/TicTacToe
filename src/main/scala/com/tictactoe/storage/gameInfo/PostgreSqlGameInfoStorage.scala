package com.tictactoe.storage.gameInfo

import com.tictactoe.model.GameInfo
import doobie._
import doobie.implicits._
import io.circe.syntax.EncoderOps
import com.tictactoe.app.server.JsonOps._

object PostgreSqlGameInfoStorage extends GameInfoStorage[ConnectionIO] {

  override def post(gameInfo: GameInfo): ConnectionIO[Int] =
    sql"""INSERT INTO GameInfo (game_id, incoming_message, outgoing_message)
          VALUES (${gameInfo.gameId.value}, ${gameInfo.incomingMessage.asJson.noSpaces}, ${gameInfo.outgoingMessage.asJson.noSpaces})"""
      .update
      .run
}
