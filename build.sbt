ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.15"
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.15" % "test"
libraryDependencies += "org.mockito" %% "mockito-scala" % "1.17.12" % Test

libraryDependencies += "com.outr" %% "hasher" % "1.2.2"
libraryDependencies += "org.typelevel" %% "cats-core" % "2.0.0"



lazy val root = (project in file("."))
  .settings(
    name := "ddd-example"
  )
