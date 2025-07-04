name := """xcala.play"""

organization := "com.xcala"

version := "1.12.17"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.16"

resolvers ++=
  Seq(
    "Sonatype Nexus Repository Manager".at("https://nexus.darkube.app/repository/ajor-maven/")
  )

publishTo :=
  Some(
    "Sonatype Nexus Repository Manager".at("https://nexus.darkube.app/repository/ajor-maven/")
  )

credentials +=
  Credentials(
    "Sonatype Nexus Repository Manager",
    "nexus.darkube.app",
    "ci",
    System.getenv.get("NEXUS_KEY")
  )

libraryDependencies ++=
  Seq(
    guice,
    ws,
    caffeine,
    filters,
    jodaForms,
    "com.typesafe.akka"            %% "akka-stream-kafka"          % "4.0.2",
    "org.reactivemongo"            %% "reactivemongo"              % "1.0.10",
    "com.nappin"                   %% "play-recaptcha"             % "2.6",
    "com.typesafe.akka"            %% "akka-actor-typed"           % "2.8.6",
    "com.typesafe.akka"            %% "akka-testkit"               % "2.8.6" % "test",
    "com.typesafe.akka"            %% "akka-serialization-jackson" % "2.8.6",
    "com.typesafe.akka"            %% "akka-stream"                % "2.8.6",
    "com.typesafe.akka"            %% "akka-slf4j"                 % "2.8.6",
    "com.bahmanm"                  %% "persianutils"               % "5.0",
    "io.lemonlabs"                 %% "scala-uri"                  % "4.0.3",
    "com.typesafe.play"            %% "play-slick"                 % "5.2.0",
    "com.github.tototoshi"         %% "slick-joda-mapper"          % "2.9.1",
    "com.github.tminglei"          %% "slick-pg"                   % "0.21.1",
    "com.lightbend.akka"           %% "akka-stream-alpakka-slick"  % "6.0.2",
    "com.lightbend.akka"           %% "akka-stream-alpakka-s3"     % "6.0.2",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.19.0",
    "com.typesafe.play"            %% "play-json"                  % "2.10.6",
    "com.typesafe.play"            %% "play-json-joda"             % "2.10.6",
    "com.sksamuel.scrimage"        %% "scrimage-scala"             % "4.3.1",
    "com.sksamuel.scrimage"         % "scrimage-webp"              % "4.3.1",
    "org.apache.logging.log4j"      % "log4j-to-slf4j"             % "2.24.3",
    "org.apache.tika"               % "tika-core"                  % "3.1.0",
    "io.sentry"                     % "sentry-logback"             % "8.12.0",
    "io.minio"                      % "minio"                      % "8.5.17",
    "commons-io"                    % "commons-io"                 % "2.19.0",
    "org.postgresql"                % "postgresql"                 % "42.7.5",
    "software.amazon.awssdk"        % "s3"                         % "2.31.44",
    "com.ibm.icu"                   % "icu4j"                      % "77.1",
    specs2                          % Test
  )

ThisBuild / scapegoatVersion := "3.1.9"

scapegoatIgnoredFiles :=
  Seq(
    ".*/ReverseRoutes.scala",
    ".*/JavaScriptReverseRoutes.scala",
    ".*/Routes.scala"
  )

scapegoatDisabledInspections :=
  Seq(
    "DuplicateImport",
    "CatchThrowable",
    "UnusedMethodParameter",
    "OptionGet",
    "BooleanParameter",
    "VariableShadowing",
    "UnsafeTraversableMethods",
    "CatchException",
    "EitherGet",
    "ComparingFloatingPointTypes",
    "PartialFunctionInsteadOfMatch",
    "AsInstanceOf",
    "ClassNames"
  )

routesImport ++=
  Seq(
    "reactivemongo.api.bson.BSONObjectID",
    "xcala.play.extensions.Bindables._",
    "play.api.i18n.Messsages"
  )

TwirlKeys.templateImports ++=
  Seq(
    "reactivemongo.api.bson.{BSONObjectID, BSONDocument}",
    "_root_.xcala.play.models._",
    "reactivemongo.api.gridfs.ReadFile",
    "_root_.xcala.play.extensions.PersianUtils._",
    "_root_.xcala.play.extensions.MultilangHelper._",
    "java.util.UUID"
  )

scalacOptions ++=
  Seq(
    "-feature",
    "-Wunused",
    "-Wdead-code",
    "-Xlint",
    "-Wconf:cat=unused-imports&site=.*views.html.*:s", // Silence import warnings in Play html files
    "-Wconf:cat=unused-imports&site=<empty>:s",        // Silence import warnings on Play `routes` files
    "-Wconf:cat=unused-imports&site=router:s"          // Silence import warnings on Play `routes` files
  )

publishConfiguration      := publishConfiguration.value.withOverwrite(true)
publishLocalConfiguration := publishLocalConfiguration.value.withOverwrite(true)

Test / javaOptions ++= Seq("--add-opens=java.base/java.lang=ALL-UNNAMED")

// Semanticdb is only for better IDE experience. therefore we disable it in non-development environments
if (sys.props.getOrElse("ci", "") != "true") {
  new Def.SettingList(
    Seq(
      addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.13.6" cross CrossVersion.full),
      scalacOptions ++=
        List(
          "-Yrangepos",
          "-P:semanticdb:synthetics:on"
        )
    )
  )

} else {
  new Def.SettingList(Nil)
}
