import Dependencies._

lazy val commonSettings = Seq(
  version := "0.1",
  scalaVersion := "2.13.6"
)

lazy val root = project
  .in(file("."))
  .settings(commonSettings)
  .settings(addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"))
  .settings(addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.0" cross CrossVersion.full))
  .settings(
    name := "TicTacToe",
    libraryDependencies ++=
      Cats.libraries ++
        Http4s.libraries ++
        Fs2.libraries ++
        Circe.libraries ++
        Enumeratum.libraries ++
        Doobie.libraries ++
        Logging.libraries ++
        ScalaTest.libraries ++
        Seq(
          pureConfig,
          scalaUuid
        )
  )