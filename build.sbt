ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.14"

lazy val root = (project in file("."))
  .settings(
    name := "Scala_ProjectAuto",
    libraryDependencies ++= Seq(
      "io.kubernetes" % "client-java" % "15.0.1",
      "org.scala-lang.modules" %% "scala-collection-compat" % "2.4.4",
      "com.google.guava" % "guava" % "31.1-jre",
      "com.squareup.okhttp3" % "okhttp" % "4.9.3",
      "com.squareup.okhttp3" % "logging-interceptor" % "4.9.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5",
      "com.typesafe.akka" %% "akka-actor-typed" % "2.6.20",
      "com.typesafe.akka" %% "akka-stream" % "2.6.20",
      "com.typesafe.akka" %% "akka-http" % "10.2.10",
      "com.typesafe.akka" %% "akka-http-spray-json" % "10.2.10",
      "de.heikoseeberger" %% "akka-http-circe" % "1.39.2",
      "io.circe" %% "circe-core" % "0.14.6",
      "io.circe" %% "circe-generic" % "0.15.0-M1",
      "io.circe" %% "circe-parser" % "0.15.0-M1",
      "io.circe" %% "circe-parser" % "0.14.6",
      "com.typesafe.play" %% "play-json" % "2.10.0-RC6",
      "org.scalaj" %% "scalaj-http" % "2.4.2"
    ),
    resolvers ++= Seq(
      Resolver.sonatypeRepo("releases"),
      Resolver.sonatypeRepo("snapshots"),
      Resolver.mavenCentral
    )
  )

