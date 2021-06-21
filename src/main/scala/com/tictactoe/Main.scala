package com.tictactoe

import cats.effect.{ExitCode, IO, IOApp}

object Main extends IOApp {

  override def run(args: List[String]): IO[ExitCode] =
    ApplicationRunner.run[IO]()
      .use(_ => IO.never)
      .as(ExitCode.Success)
}
