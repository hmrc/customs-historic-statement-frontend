import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.itSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "customs-historic-statement-frontend"

val bootstrapVersion = "7.22.0"
val silencerVersion = "1.17.13"
val scala2_13_8 = "2.13.8"
val testDirectory = "test"

Global / lintUnusedKeysOnLoad := false
ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala2_13_8

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value /  "scalastyle-config.xml",
  (Test / scalastyleConfig) := baseDirectory.value/ testDirectory /  "test-scalastyle-config.xml")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % Test))

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(DefaultBuildSettings.scalaSettings *)
  .settings(DefaultBuildSettings.defaultSettings() *)
  .settings(ThisBuild / useSuperShell := false)
  .settings(
    name := appName,
    RoutesKeys.routesImport += "models._",
    TwirlKeys.templateImports ++= Seq(
      "play.twirl.api.HtmlFormat",
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "views.ViewUtils._",
      "models.Mode",
      "controllers.routes._"
    ),
    PlayKeys.playDefaultPort := 9396,
    ScoverageKeys.coverageExcludedFiles := "<empty>;Reverse.*;.*handlers.*;" +
      ".*BuildInfo.*;.*javascript.*;.*FrontendAuditConnector.*;.*Routes.*;.*GuiceInjector;.*ControllerConfiguration;" +
      ".*Formatters; .*LocalDateFormatter; .*package; .*UserAnswers",

    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageMinimumBranchTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true,
    scalacOptions ++= Seq("-feature"),
    libraryDependencies ++= AppDependencies(),
    retrieveManaged := true,

    update / evictionWarningOptions :=
      EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    resolvers += Resolver.jcenterRepo,

    Concat.groups := Seq(
      "javascripts/customshistoricstatementfrontend-app.js" ->
        group(Seq("javascripts/show-hide-content.js",
          "javascripts/jquery.min.js",
          "javascripts/customshistoricstatementfrontend.js"))
    ),
    uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    Assets / pipelineStages := Seq(concat,uglify),
    uglify / includeFilter := GlobFilter("customshistoricstatementfrontend-*.js")
  )
  .settings(scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "off"))
  .settings(
    scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates",
      "-P:silencer:globalFilters=possible missing interpolator: detected interpolated identifier `\\$date`",
      "-P:silencer:pathFilters=target/.*",
      s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"),

    Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:patvars",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),

    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    )
  )
  .settings(scalastyleSettings)
  .configs(IntegrationTest)
