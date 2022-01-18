/*
 * Copyright 2022 HM Revenue & Customs
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
import models.EoriHistory
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import java.time.LocalDate
import scala.concurrent.Future

class CustomsDataStoreConnectorSpec extends SpecBase {

  "getEmail" should {
    "return email address from customs data store" in new Setup {
      val emailResponse = EmailResponse(Some("a@a.com"), Some("time"))
      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/GB12345/verified-email"
      when[Future[EmailResponse]](mockHttpClient.GET(eqTo(customsDataStoreUrl), any, any)(any, any, any)).thenReturn(Future.successful(emailResponse))
      running(app) {
        val result = await(customsDataStoreConnector.getEmail("GB12345")(hc))
        result mustBe Some(Email("a@a.com"))
      }
    }

    "return None when call to customs data store fails" in new Setup {
      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/GB12345/verified-email"
      when[Future[EmailResponse]](mockHttpClient.GET(eqTo(customsDataStoreUrl), any, any)(any, any, any))
        .thenReturn(Future.failed(new RuntimeException("failed")))
      running(app) {
        val result = await(customsDataStoreConnector.getEmail("GB12345")(hc))
        result mustBe None
      }
    }
  }

  "getAllEoriHistory" should {
    "parse eoriHistory correctly" in new Setup {
      val jsonObject = Json.obj("eori" -> "eori1", "validFrom" -> "2019-11-10", "validUntil" -> "2019-12-10T10:15:30+01:00")
      val jsonObject2 = Json.obj("eori" -> "eori1", "validFrom" -> "2019-11-10", "validUntil" -> "2019-12-10T10:15:30")
      val eoriHistory1 = EoriHistory("eori1", Some(LocalDate.of(2019, 11, 10)), Some(LocalDate.of(2019, 12, 10)))

      jsonObject.as[EoriHistory] mustBe eoriHistory1
      jsonObject2.as[EoriHistory] mustBe EoriHistory("eori1", Some(LocalDate.of(2019, 11, 10)),None)
      Json.toJson[EoriHistory](eoriHistory1) mustBe Json.obj("eori" -> "eori1", "validFrom" -> "2019-11-10", "validUntil" -> "2019-12-10")
    }

    "return eoriHistory from customs data store" in new Setup {
      val eoriHistory1 = EoriHistory("eori1", Some(LocalDate.now()), Some(LocalDate.now()))
      val eoriHistory2 = EoriHistory("eori2", Some(LocalDate.now().minusDays(10)), Some(LocalDate.now().minusDays(10)))

      val eoriHistoryResponse = EoriHistoryResponse(Seq(eoriHistory1, eoriHistory2))
      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/eori1/eori-history"
      when[Future[EoriHistoryResponse]](mockHttpClient.GET(eqTo(customsDataStoreUrl), any, any)(any, any, any))
        .thenReturn(Future.successful(eoriHistoryResponse))
      running(app) {
        val result = await(customsDataStoreConnector.getAllEoriHistory("eori1")(hc))
        result.toList mustBe Seq(eoriHistory1, eoriHistory2)
      }
    }

    "return empty EoriHistory when failed to get eoriHistory from data store" in new Setup {
      val customsDataStoreUrl = "http://localhost:9893/customs-data-store/eori/eori1/eori-history"
      when[Future[EoriHistoryResponse]](mockHttpClient.GET(eqTo(customsDataStoreUrl), any, any)(any, any, any))
        .thenReturn(Future.failed(new RuntimeException("failed to get eori history")))
      running(app) {
        val result = await(customsDataStoreConnector.getAllEoriHistory("eori1")(hc))
        result.toList mustBe Seq(EoriHistory("eori1", None, None))
      }
    }
  }

  trait Setup {
    val mockHttpClient = mock[HttpClient]
    val app = applicationBuilder().overrides(
      bind[HttpClient].to(mockHttpClient)
    ).build()
    val mockAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val customsDataStoreConnector = app.injector.instanceOf[CustomsDataStoreConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

}
