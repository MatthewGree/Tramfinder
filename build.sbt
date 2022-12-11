val Http4sVersion = "0.23.16"
val CirceVersion = "0.14.3"
val MunitVersion = "0.7.29"
val LogbackVersion = "1.4.4"
val MunitCatsEffectVersion = "1.0.7"

lazy val root = (project in file("."))
  .settings(
    organization := "com.matt",
    name := "tramfinder",
    version := "0.0.1-SNAPSHOT",
    scalaVersion := "2.13.8",
    resolvers ++= Seq(DefaultMavenRepository, JavaNet2Repository, Resolver.typesafeIvyRepo("releases")) ++ Resolver.sonatypeOssRepos("releases"),
    libraryDependencies ++= Seq(
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-ember-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.scalameta" %% "munit" % MunitVersion % Test,
      "org.typelevel" %% "munit-cats-effect-3" % MunitCatsEffectVersion % Test,
      "ch.qos.logback" % "logback-classic" % LogbackVersion % Runtime,
      "org.scalameta" %% "svm-subs" % "20.2.0",
      "com.lucidchart" %% "xtract" % "2.3.0-alpha3",
      "com.lucidchart" %% "xtract-testing" % "2.3.0-alpha3" % "test",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.5"
    ),
    addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.13.2" cross CrossVersion.full),
    addCompilerPlugin("com.olegpy" %% "better-monadic-for" % "0.3.1"),
    testFrameworks += new TestFramework("munit.Framework"),
    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", _*) => MergeStrategy.discard
      case _ => MergeStrategy.first
    },
    Test / scalacOptions := Seq()

  )
