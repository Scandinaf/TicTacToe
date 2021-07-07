package com.tictactoe.storage

import cats.effect.{Async, Blocker, ContextShift, Resource}
import com.tictactoe.model.AppConfig.DBConfig
import doobie.ExecutionContexts
import doobie.hikari.HikariTransactor
import doobie.implicits._

import java.util.concurrent.Executors
import scala.concurrent.ExecutionContext

object DBModule {

  def of[F[_] : Async : ContextShift](config: DBConfig): Resource[F, HikariTransactor[F]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[F](16)
      be = Blocker.liftExecutionContext(
        ExecutionContext.fromExecutorService(
          Executors.newCachedThreadPool()
        )
      )
      xa <- HikariTransactor.newHikariTransactor[F](
        "org.postgresql.Driver",
        s"jdbc:postgresql://${config.host.value}:${config.port.value}/Application",
        config.user.value,
        config.password.value,
        ce,
        be
      )
      _ <- Resource.eval[F, Int](sql"select 42".query[Int].unique.transact(xa))
    } yield xa
}
