import play.sbt.routes.RoutesKeys
import uk.gov.hmrc.DefaultBuildSettings

ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := "3.3.5"

lazy val microservice = Project("ngr-rald-frontend", file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    // https://www.scala-lang.org/2021/01/12/configuring-and-suppressing-warnings.html
    // suppress warnings in generated routes files
    scalacOptions += "-Wconf:src=routes/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    pipelineStages := Seq(gzip),
    Compile / scalacOptions -= "utf8",
    PlayKeys.playDefaultPort := 1505,
  )
  .settings(
      RoutesKeys.routesImport ++= Seq(
          "uk.gov.hmrc.ngrraldfrontend.models._",
          "uk.gov.hmrc.play.bootstrap.binders.RedirectUrl"
      ),
      TwirlKeys.templateImports ++= Seq(
          "uk.gov.hmrc.ngrraldfrontend.models.Mode"
      ),
  )
  .settings(CodeCoverageSettings.settings*)
  .disablePlugins(JUnitXmlReportPlugin)

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings())
  .settings(libraryDependencies ++= AppDependencies.it)
