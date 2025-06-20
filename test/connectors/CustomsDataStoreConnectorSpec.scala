/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import base.SpecBase
import models.{
  EmailResponses, EmailUnverifiedResponse, EmailVerifiedResponse, EoriHistory, UndeliverableEmail,
  UndeliverableInformation, UnverifiedEmail
}
import play.api.{Application, Configuration}
import play.api.libs.json.{JsObject, Json}
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.retrieve.Email
import utils.TestData.*
import utils.WireMockSupportProvider
import com.github.tomakehurst.wiremock.client.WireMock.{get, notFound, ok, serverError, urlPathMatching}

import java.time.LocalDate
import com.typesafe.config.ConfigFactory

class CustomsDataStoreConnectorSpec extends SpecBase with WireMockSupportProvider {

  "getEmail" should {
    "return email address from customs data store" in new Setup {
      val emailResponse: EmailResponse = EmailResponse(Some("a@a.com"), Some("time"), None)

      wireMockServer.stubFor(
        get(urlPathMatching(getEmailUrl))
          .willReturn(
            ok(Json.toJson(emailResponse).toString)
          )
      )

      val result: Either[EmailResponses, Email] = await(customsDataStoreConnector.getEmail(hc))

      result mustBe Right(Email("a@a.com"))
      verifyEndPointUrlHit(getEmailUrl)
    }

    "return undeliverable email address from customs data store" in new Setup {

      val emailResponse: EmailResponse = EmailResponse(
        Some("noresponse@email.com"),
        Some("time"),
        Some(UndeliverableInformation("subject-example", "ex-event-id-01", "ex-group-id-01"))
      )

      wireMockServer.stubFor(
        get(urlPathMatching(getEmailUrl))
          .willReturn(
            ok(Json.toJson(emailResponse).toString)
          )
      )

      val result: Either[EmailResponses, Email] = await(customsDataStoreConnector.getEmail(hc))

      result mustBe Left(UndeliverableEmail("noresponse@email.com"))
      verifyEndPointUrlHit(getEmailUrl)
    }

    "return None when call to customs data store fails" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(getEmailUrl))
          .willReturn(notFound())
      )

      val result: Either[EmailResponses, Email] = await(customsDataStoreConnector.getEmail)

      result mustBe Left(UnverifiedEmail)
      verifyEndPointUrlHit(getEmailUrl)
    }
  }

  "getAllEoriHistory" should {
    "parse eoriHistory correctly" in new Setup {
      val jsonObject: JsObject =
        Json.obj("eori" -> "eori1", "validFrom" -> "2018-11-14", "validUntil" -> "2018-12-14T10:15:30+01:00")

      val jsonObject2: JsObject =
        Json.obj("eori" -> "eori1", "validFrom" -> "2018-11-14", "validUntil" -> "2018-12-14T10:15:30")

      val eoriHistory1: EoriHistory =
        EoriHistory("eori1", Some(LocalDate.of(year, eleven, day)), Some(LocalDate.of(year, twelve, day)))

      jsonObject.as[EoriHistory] mustBe eoriHistory1
      jsonObject2.as[EoriHistory] mustBe EoriHistory("eori1", Some(LocalDate.of(year, eleven, day)), None)

      Json.toJson[EoriHistory](eoriHistory1) mustBe Json.obj(
        "eori"       -> "eori1",
        "validFrom"  -> "2018-11-14",
        "validUntil" -> "2018-12-14"
      )
    }

    "return eoriHistory from customs data store" in new Setup {

      val eoriHistory1: EoriHistory = EoriHistory("eori1", Some(LocalDate.now()), Some(LocalDate.now()))

      val eoriHistory2: EoriHistory =
        EoriHistory("eori2", Some(LocalDate.now().minusDays(offset)), Some(LocalDate.now().minusDays(offset)))

      val eoriHistoryResponse: EoriHistoryResponse = EoriHistoryResponse(Seq(eoriHistory1, eoriHistory2))

      wireMockServer.stubFor(
        get(urlPathMatching(eoriHistoryUrl))
          .willReturn(
            ok(Json.toJson(eoriHistoryResponse).toString)
          )
      )

      val result: Seq[EoriHistory] = await(customsDataStoreConnector.getAllEoriHistory("eori1")(hc))

      result.toList mustBe Seq(eoriHistory1, eoriHistory2)
      verifyEndPointUrlHit(eoriHistoryUrl)
    }

    "return empty EoriHistory when failed to get eoriHistory from data store" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(eoriHistoryUrl))
          .willReturn(serverError())
      )

      val result: Seq[EoriHistory] = await(customsDataStoreConnector.getAllEoriHistory("eori1")(hc))

      result.toList mustBe Seq(EoriHistory("eori1", None, None))
      verifyEndPointUrlHit(eoriHistoryUrl)
    }

    "return empty EoriHistory when 404 response code is received while fetching the EORI history from data store" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(eoriHistoryUrl))
          .willReturn(notFound())
      )

      val result: Seq[EoriHistory] = await(customsDataStoreConnector.getAllEoriHistory("eori1")(hc))

      result.toList mustBe Seq(EoriHistory("eori1", None, None))
      verifyEndPointUrlHit(eoriHistoryUrl)
    }
  }

  "retrieveUnverifiedEmail" must {
    "return EmailUnverifiedResponse with unverified email value" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(unverifiedEmailUrl))
          .willReturn(
            ok(Json.toJson(emailUnverifiedRes).toString)
          )
      )

      val result: EmailUnverifiedResponse = await(customsDataStoreConnector.retrieveUnverifiedEmail)

      result mustBe emailUnverifiedRes
      verifyEndPointUrlHit(unverifiedEmailUrl)
    }

    "return EmailUnverifiedResponse with None for unverified email if there is an error while" +
      " fetching response from api" in new Setup {

        wireMockServer.stubFor(
          get(urlPathMatching(unverifiedEmailUrl))
            .willReturn(serverError())
        )

        val result: EmailUnverifiedResponse = await(customsDataStoreConnector.retrieveUnverifiedEmail)

        result.unVerifiedEmail mustBe empty
        verifyEndPointUrlHit(unverifiedEmailUrl)
      }
  }

  "verifiedEmail" must {
    "return verified email when email-display api call is successful" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(verifiedEmailUrl))
          .willReturn(
            ok(Json.toJson(emailVerifiedRes).toString)
          )
      )

      val result: EmailVerifiedResponse = await(customsDataStoreConnector.verifiedEmail)

      result mustBe emailVerifiedRes
      verifyEndPointUrlHit(verifiedEmailUrl)
    }

    "return none for verified email when exception occurs while calling email-display api" in new Setup {

      wireMockServer.stubFor(
        get(urlPathMatching(verifiedEmailUrl))
          .willReturn(serverError())
      )

      val result: EmailVerifiedResponse = await(customsDataStoreConnector.verifiedEmail)

      result.verifiedEmail mustBe empty
      verifyEndPointUrlHit(verifiedEmailUrl)
    }
  }

  override def config: Configuration = Configuration(
    ConfigFactory.parseString(
      s"""
         |microservice {
         |  services {
         |      customs-data-store {
         |      protocol = http
         |      host     = $wireMockHost
         |      port     = $wireMockPort
         |      context = "/customs-data-store"
         |    }
         |  }
         |}
         |""".stripMargin
    )
  )

  trait Setup {
    val getEmailUrl: String        = "/customs-data-store/eori/verified-email"
    val eoriHistoryUrl: String     = "/customs-data-store/eori/eori-history"
    val verifiedEmailUrl: String   = "/customs-data-store/subscriptions/email-display"
    val unverifiedEmailUrl: String = "/customs-data-store/subscriptions/unverified-email-display"

    val app: Application = applicationBuilder()
      .configure(config)
      .build()

    val customsDataStoreConnector: CustomsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]

    val emailUnverifiedRes: EmailUnverifiedResponse = EmailUnverifiedResponse(Some(emailId))
    val emailVerifiedRes: EmailVerifiedResponse     = EmailVerifiedResponse(Some(emailId))
  }
}
