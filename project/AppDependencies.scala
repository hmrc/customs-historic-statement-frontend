import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "9.1.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "2.2.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "10.5.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % "3.0.0",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "tax-year" % "5.0.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.jsoup" % "jsoup" % "1.17.2",
      "org.scalacheck" %% "scalacheck" % "1.18.0",
    "org.scalatestplus" %% "mockito-4-11" % "3.2.18.0"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
