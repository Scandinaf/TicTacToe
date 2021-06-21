import sbt._

object Dependencies {

  object Cats {

    val libraries = {

      val core = "org.typelevel" %% "cats-core" % "2.6.1"
      val effect = "org.typelevel" %% "cats-effect" % "2.5.1"

      Seq(core, effect)
    }
  }

  object Http4s {

    val libraries = {

      val version = "0.21.24"
      val dsl = "org.http4s" %% "http4s-dsl" % version
      val server = "org.http4s" %% "http4s-blaze-server" % version

      Seq(dsl, server)
    }
  }

  object Fs2 {

    val libraries = {

      val version = "2.5.6"
      val core = "co.fs2" %% "fs2-core" % version
      val io = "co.fs2" %% "fs2-io" % version
      val reactiveStreams = "co.fs2" %% "fs2-reactive-streams" % version

      Seq(core, io, reactiveStreams)
    }
  }

  object Logging {

    val libraries = {

      val slf4j = "org.slf4j" % "slf4j-api" % "2.0.0-alpha1"
      val logbackClassic = "ch.qos.logback" % "logback-classic" % "1.3.0-alpha5"

      Seq(slf4j, logbackClassic)
    }
  }

  object Circe {

    val libraries = {

      val version = "0.14.1"
      val core = "io.circe" %% "circe-core" % version
      val generic = "io.circe" %% "circe-generic" % version
      val genericExtras = "io.circe" %% "circe-generic-extras" % version
      val parser = "io.circe" %% "circe-parser" % version

      Seq(core, generic, genericExtras, parser)
    }
  }

  object Enumeratum {

    val libraries = {

      val version = "1.7.0"
      val core = "com.beachape" %% "enumeratum" % version
      val circe = "com.beachape" %% "enumeratum-circe" % version

      Seq(core, circe)
    }
  }

  val pureConfig = "com.github.pureconfig" %% "pureconfig" % "0.16.0"
  val scalaTest = "org.scalatest" %% "scalatest" % "3.3.0-SNAP3" % Test
  val mockito = "org.mockito" %% "mockito-scala-scalatest" % "1.16.37" % Test
}