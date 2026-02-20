import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % "2.12.0",
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "12.31.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.4.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"       %% "tax-year"                              % "5.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup"          % "jsoup"                  % "1.17.2",
    "org.scalacheck"    %% "scalacheck"             % "1.18.0",
    "org.scalatestplus" %% "mockito-4-11"           % "3.2.18.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
