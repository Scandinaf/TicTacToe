package com.tictactoe

import cats.effect.IO
import com.tictactoe.service.logging.LogOf

trait TestImplicits {

  implicit val logOf: LogOf[IO] =
    LogOf.slf4j[IO].unsafeRunSync()
}
