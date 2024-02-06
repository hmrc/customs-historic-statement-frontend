import play.sbt.routes.RoutesKeys
import scoverage.ScoverageKeys
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

lazy val appName: String = "customs-historic-statement-frontend"
val silencerVersion = "1.17.13"
val testDirectory = "test"

lazy val scalastyleSettings = Seq(scalastyleConfig := baseDirectory.value /  "scalastyle-config.xml",
  (Test / scalastyleConfig) := baseDirectory.value/ testDirectory /  "test-scalastyle-config.xml")

lazy val root = (project in file("."))
  .enablePlugins(PlayScala, SbtAutoBuildPlugin, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(DefaultBuildSettings.scalaSettings: _*)
  .settings(DefaultBuildSettings.defaultSettings(): _*)
  .settings(majorVersion := 0)
  .settings(useSuperShell in ThisBuild := false)
  .settings(
    scalaVersion := "2.13.8",
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

    evictionWarningOptions in update :=
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
    pipelineStages in Assets := Seq(concat,uglify),
    includeFilter in uglify := GlobFilter("customshistoricstatementfrontend-*.js")
  )
  .settings(scalacOptions ++= Seq("-Ypatmat-exhaust-depth", "off"))
  .settings(
    scalacOptions ++= Seq(
      "-Wunused:imports",
      "-Wunused:params",
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
