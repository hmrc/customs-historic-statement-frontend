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
import config.FrontendAppConfig
import models.{EoriHistory, UndeliverableEmail, UndeliverableInformation, UnverifiedEmail, EmailVerifiedResponse, EmailUnverifiedResponse}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers.*
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpReads, StringContextOps, UpstreamErrorResponse}
import play.api.http.Status.{NOT_FOUND, INTERNAL_SERVER_ERROR}
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.net.URL
import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CustomsDataStoreConnectorSpec extends SpecBase {

  "getEmail" should {
    "return email address from customs data store" in new Setup {
      val emailResponse = EmailResponse(Some("a@a.com"), Some("time"), None)
      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/GB12345/verified-email"

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailResponse))

      when(mockHttpClient.get(eqTo(url"$customsDataStoreUrl"))(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.getEmail("GB12345")(hc))
        result mustBe Right(Email("a@a.com"))
      }
    }

    "return undeliverable email address from customs data store" in new Setup {

      val emailResponse = EmailResponse(Some("noresponse@email.com"),
        Some("time"), Some(UndeliverableInformation(
          "subject-example", "ex-event-id-01", "ex-group-id-01")))

      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/GB12346/verified-email"

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailResponse))

      when(mockHttpClient.get(eqTo(url"$customsDataStoreUrl"))(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.getEmail("GB12346")(hc))
        result mustBe Left(UndeliverableEmail("noresponse@email.com"))
      }
    }

    "return None when call to customs data store fails" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("NoData", NOT_FOUND, NOT_FOUND)))

      when(mockHttpClient.get(any[URL]())(any())).thenReturn(requestBuilder)

      running(app) {
        val result = customsDataStoreConnector.getEmail(eori)
        await(result) mustBe Left(UnverifiedEmail)
      }
    }
  }

  "getAllEoriHistory" should {
    "parse eoriHistory correctly" in new Setup {
      val jsonObject = Json.obj("eori" -> "eori1",
        "validFrom" -> "2019-11-10", "validUntil" -> "2019-12-10T10:15:30+01:00")

      val jsonObject2 = Json.obj("eori" -> "eori1",
        "validFrom" -> "2019-11-10", "validUntil" -> "2019-12-10T10:15:30")

      val year = 2019
      val day = 10
      val eleven = 11
      val twelve = 12

      val eoriHistory1 = EoriHistory("eori1",
        Some(LocalDate.of(year, eleven, day)), Some(LocalDate.of(year, twelve, day)))

      jsonObject.as[EoriHistory] mustBe eoriHistory1
      jsonObject2.as[EoriHistory] mustBe EoriHistory("eori1", Some(LocalDate.of(year, eleven, day)), None)

      Json.toJson[EoriHistory](eoriHistory1) mustBe Json.obj(
        "eori" -> "eori1", "validFrom" -> "2019-11-10", "validUntil" -> "2019-12-10")
    }

    "return eoriHistory from customs data store" in new Setup {

      val offset = 10

      val eoriHistory1 = EoriHistory("eori1",
        Some(LocalDate.now()),
        Some(LocalDate.now()))

      val eoriHistory2 = EoriHistory("eori2",
        Some(LocalDate.now().minusDays(offset)),
        Some(LocalDate.now().minusDays(offset)))

      val eoriHistoryResponse = EoriHistoryResponse(Seq(eoriHistory1, eoriHistory2))
      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/eori1/eori-history"

      when(requestBuilder.execute(any[HttpReads[EoriHistoryResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(eoriHistoryResponse))

      when(mockHttpClient.get(eqTo(url"$customsDataStoreUrl"))(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.getAllEoriHistory("eori1")(hc))
        result.toList mustBe Seq(eoriHistory1, eoriHistory2)
      }
    }

    "return empty EoriHistory when failed to get eoriHistory from data store" in new Setup {

      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/eori1/eori-history"

      when(requestBuilder.execute(any[HttpReads[EoriHistoryResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("failed to get eori history")))

      when(mockHttpClient.get(eqTo(url"$customsDataStoreUrl"))(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.getAllEoriHistory("eori1")(hc))
        result.toList mustBe Seq(EoriHistory("eori1", None, None))
      }
    }
  }

  "retrieveUnverifiedEmail" must {
    "return EmailUnverifiedResponse with unverified email value" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailUnverifiedRes))

      when(mockHttpClient.get(any[URL]())(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.retrieveUnverifiedEmail)
        result mustBe emailUnverifiedRes
      }
    }

    "return EmailUnverifiedResponse with None for unverified email if there is an error while" +
      " fetching response from api" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("error occurred")))

      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.retrieveUnverifiedEmail)
        result.unVerifiedEmail mustBe empty
      }
    }
  }

  "verifiedEmail" must {
    "return verified email when email-display api call is successful" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailVerifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(emailVerifiedRes))

      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.verifiedEmail)
        result mustBe emailVerifiedRes
      }
    }

    "return none for verified email when exception occurs while calling email-display api" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailVerifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(UpstreamErrorResponse("error occurred", INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR)))

      when(mockHttpClient.get(any())(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsDataStoreConnector.verifiedEmail)
        result.verifiedEmail mustBe empty
      }
    }
  }

  trait Setup {
    val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]
    val eori: String = "GB11111"
    val emailId = "test@test.com"

    val app = applicationBuilder().overrides(
      bind[HttpClientV2].to(mockHttpClient),
      bind[RequestBuilder].toInstance(requestBuilder)
    ).build()

    val mockAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val customsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]

    val emailUnverifiedRes: EmailUnverifiedResponse = EmailUnverifiedResponse(Some(emailId))
    val emailVerifiedRes: EmailVerifiedResponse = EmailVerifiedResponse(Some(emailId))

    implicit val hc: HeaderCarrier = HeaderCarrier()
  }
}
