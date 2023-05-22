import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % "1.2.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc" % "7.7.0-play-28",
    "uk.gov.hmrc" %% "play-conditional-form-mapping" % "1.13.0-play-28",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "7.15.0",
    "uk.gov.hmrc" %% "tax-year" % "3.2.0"
  )

  val test = Seq(
    "uk.gov.hmrc" %% "bootstrap-test-play-28" % "7.15.0" ,
    "org.scalatest" %% "scalatest" % "3.2.9" ,
    "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" ,
    "org.jsoup" % "jsoup" % "1.10.2" ,
    "com.typesafe.play" %% "play-test" % current,
    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10",
    "org.scalacheck" %% "scalacheck" % "1.14.1",
    "org.mockito" %% "mockito-scala-scalatest" % "1.16.37"
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test

}
