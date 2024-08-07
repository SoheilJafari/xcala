name := """xcala.play"""

organization := "com.xcala"

version := "1.6.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.13.14"

resolvers ++=
  Seq(
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
    "org.reactivemongo"            %% "reactivemongo"              % "1.0.10",
    "com.nappin"                   %% "play-recaptcha"             % "2.6",
    "com.typesafe.akka"            %% "akka-actor-typed"           % "2.8.5",
    "com.typesafe.akka"            %% "akka-testkit"               % "2.8.5" % "test",
    "com.typesafe.akka"            %% "akka-serialization-jackson" % "2.8.5",
    "com.typesafe.akka"            %% "akka-stream"                % "2.8.5",
    "com.typesafe.akka"            %% "akka-slf4j"                 % "2.8.5",
    "com.bahmanm"                  %% "persianutils"               % "5.0",
    "io.lemonlabs"                 %% "scala-uri"                  % "4.0.3",
    "org.apache.tika"               % "tika-core"                  % "2.9.2",
    "ch.qos.logback"                % "logback-classic"            % "1.5.6",
    "io.sentry"                     % "sentry-logback"             % "7.9.0",
    "io.minio"                      % "minio"                      % "8.5.10",
    "commons-io"                    % "commons-io"                 % "2.15.1",
    "com.sksamuel.scrimage"        %% "scrimage-scala"             % "4.1.3",
    "com.sksamuel.scrimage"         % "scrimage-webp"              % "4.1.3",
    "com.fasterxml.jackson.module" %% "jackson-module-scala"       % "2.17.1",
    "org.postgresql"                % "postgresql"                 % "42.7.3",
    "com.typesafe.play"            %% "play-slick"                 % "5.2.0",
    "com.github.tototoshi"         %% "slick-joda-mapper"          % "2.9.1",
    "com.github.tminglei"          %% "slick-pg"                   % "0.21.1",
    "com.lightbend.akka"           %% "akka-stream-alpakka-slick"  % "6.0.2",
    "com.ibm.icu"                   % "icu4j"                      % "75.1",
    "com.typesafe.play"            %% "play-json"                  % "2.10.4",
    "com.typesafe.play"            %% "play-json-joda"             % "2.10.4",
    specs2                          % Test
  )

ThisBuild / scapegoatVersion := "2.1.6"

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

Assets / LessKeys.less / includeFilter := "*.less"
Assets / LessKeys.less / excludeFilter := "_*.less"

pipelineStages := Seq(gzip)

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
      addCompilerPlugin("org.scalameta" % "semanticdb-scalac" % "4.9.5" cross CrossVersion.full),
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
