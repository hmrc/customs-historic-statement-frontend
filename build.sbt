import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings.itSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "customs-historic-statement-frontend"

val bootstrapVersion = "9.5.0"
val silencerVersion = "1.7.16"
val scala3_3_4 = "3.3.4"
val testDirectory = "test"

val turnoffJSUglifyWarningsTask = SettingKey[Seq[String]]("sbt-uglify turn off console output")
turnoffJSUglifyWarningsTask := Seq("warnings=false")

Global / lintUnusedKeysOnLoad := false
ThisBuild / majorVersion := 0
ThisBuild / scalaVersion := scala3_3_4

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value / "scalastyle-config.xml",
  (Test / scalastyleConfig) := baseDirectory.value / testDirectory / "test-scalastyle-config.xml")

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test")
  .settings(itSettings())
  .settings(libraryDependencies ++= Seq("uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion % Test))

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
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
    ScoverageKeys.coverageFailOnMinimum := false,
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
    Assets / pipelineStages := Seq(concat, uglify),
    uglify / includeFilter := GlobFilter("customshistoricstatementfrontend-*.js")
  )
  .settings(uglifyCompressOptions := turnoffJSUglifyWarningsTask.value)
  .settings(
    scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")),
    scalacOptions ++= Seq("-Wconf:src=routes/.*:s", "-Wconf:msg=Flag.*repeatedly:s"),

      Test / scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
      "-Wunused:implicits",
      "-Wunused:explicits",
      "-Wunused:privates"),

    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.for3Use2_13With("", ".12")),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.for3Use2_13With("", ".12")
    )
  )
  .settings(scalastyleSettings)

addCommandAlias("runAllChecks", ";clean;compile;coverage;test;it/test;scalastyle;Test/scalastyle;coverageReport")
