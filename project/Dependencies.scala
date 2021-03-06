import sbt._

object Dependencies {

  object Cats {

    val libraries: Seq[ModuleID] = {

      val core = "org.typelevel" %% "cats-core" % "2.6.1"
      val effect = "org.typelevel" %% "cats-effect" % "2.5.1"
      val free = "org.typelevel" %% "cats-free" % "2.6.1"

      Seq(core, effect, free)
    }
  }

  object Http4s {

    val libraries: Seq[ModuleID] = {

      val version = "0.21.24"
      val dsl = "org.http4s" %% "http4s-dsl" % version
      val server = "org.http4s" %% "http4s-blaze-server" % version

      Seq(dsl, server)
    }
  }

  object Fs2 {

    val libraries: Seq[ModuleID] = {

      val version = "2.5.6"
      val core = "co.fs2" %% "fs2-core" % version
      val io = "co.fs2" %% "fs2-io" % version
      val reactiveStreams = "co.fs2" %% "fs2-reactive-streams" % version

      Seq(core, io, reactiveStreams)
    }
  }

  object Logging {

    val libraries: Seq[ModuleID] = {

      val slf4j = "org.slf4j" % "slf4j-api" % "2.0.0-alpha1"
      val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.3.0-alpha5"

      Seq(slf4j, logbackClassic)
    }
  }

  object Circe {

    val libraries: Seq[ModuleID] = {

      val version = "0.14.1"
      val core = "io.circe" %% "circe-core" % version
      val generic = "io.circe" %% "circe-generic" % version
      val genericExtras = "io.circe" %% "circe-generic-extras" % version
      val parser = "io.circe" %% "circe-parser" % version

      Seq(core, generic, genericExtras, parser)
    }
  }

  object Enumeratum {

    val libraries: Seq[ModuleID] = {

      val version = "1.7.0"
      val core = "com.beachape" %% "enumeratum" % version
      val circe = "com.beachape" %% "enumeratum-circe" % version

      Seq(core, circe)
    }
  }

  object Doobie {

    val libraries: Seq[ModuleID] = {

      val version = "0.12.1"
      val core = "org.tpolecat" %% "doobie-core" % version
      val postgresql = "org.tpolecat" %% "doobie-postgres" % version
      val hikari = "org.tpolecat" %% "doobie-hikari" % version
      val doobieTest = "org.tpolecat" %% "doobie-scalatest" % version % "test"

      Seq(core, postgresql, hikari, doobieTest)
    }
  }

  object ScalaTest {

    val libraries: Seq[ModuleID] = {

      val scalaTest = "org.scalatest" %% "scalatest" % "3.3.0-SNAP3"
      val mockito = "org.mockito" %% "mockito-scala-scalatest" % "1.16.37"

      Seq(scalaTest, mockito)
        .map(_ % Test)
    }
  }

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.16.0"
  val scalaUuid = "io.jvm.uuid" %% "scala-uuid" % "0.3.1"
}