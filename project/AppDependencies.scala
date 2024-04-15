import play.core.PlayVersion.current
import sbt.*

object AppDependencies {

  private val bootstrapVersion = "8.5.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % "1.8.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "8.5.0",
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % "2.0.0",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "tax-year" % "3.3.0"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
    "org.scalatest" %% "scalatest" % "3.2.16",
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0",
    "org.jsoup" % "jsoup" % "1.16.1",
    "com.typesafe.play" %% "play-test" % current,
    "com.vladsch.flexmark" % "flexmark-all" % "0.64.8",
    "org.scalacheck" %% "scalacheck" % "1.17.0",
    "org.mockito" %% "mockito-scala-scalatest" % "1.17.29"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
