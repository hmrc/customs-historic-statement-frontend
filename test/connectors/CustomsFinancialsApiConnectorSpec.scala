/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{C79Certificate, HistoricDocumentRequest}
import org.mockito.ArgumentMatchers
import play.api.http.Status
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import java.time.LocalDate
import scala.concurrent.Future

class CustomsFinancialsApiConnectorSpec extends SpecBase {

  "postHistoricDocumentRequest" should {

    "submit request successfully" in new Setup {
      val customsFinancialsApiUrl = "http://localhost:9878/customs-financials-api/historic-document-request"
      val historicDocumentRequest = HistoricDocumentRequest(C79Certificate, LocalDate.now(), LocalDate.now().plusMonths(1), Some("1234"))

      when[Future[HttpResponse]](mockHttpClient.POST(ArgumentMatchers.eq(customsFinancialsApiUrl), ArgumentMatchers.eq(historicDocumentRequest), any)(any, any, any, any))
        .thenReturn(Future.successful(HttpResponse.apply(Status.NO_CONTENT, "")))
      running(app) {
        val result = await(customsFinancialsApiConnector.postHistoricDocumentRequest(historicDocumentRequest)(hc))
        result mustBe true
      }
    }

  "return false when failed to submit the request" in new Setup {
      val customsFinancialsApiUrl = "http://localhost:9878/customs-financials-api/historic-document-request"
      val historicDocumentRequest = HistoricDocumentRequest(C79Certificate, LocalDate.now(), LocalDate.now().plusMonths(1), Some("1234"))

      when[Future[HttpResponse]](mockHttpClient.POST(ArgumentMatchers.eq(customsFinancialsApiUrl), ArgumentMatchers.eq(historicDocumentRequest), any)(any, any, any, any))
        .thenReturn(Future.failed(new RuntimeException("failed")))
      running(app) {
        val result = await(customsFinancialsApiConnector.postHistoricDocumentRequest(historicDocumentRequest)(hc))
        result mustBe false
      }
    }
  }

  "deleteNotification" should {

    "return true when deletion is successful" in new Setup {
      val customsFinancialsApiUrl = "http://localhost:9878/customs-financials-api/eori/eori1/requested-notifications/C79Certificate"
      when[Future[HttpResponse]](mockHttpClient.DELETE(ArgumentMatchers.eq(customsFinancialsApiUrl), any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse.apply(Status.OK, "")))
      running(app) {
        val result = await(customsFinancialsApiConnector.deleteNotification("eori1", C79Certificate)(hc))
        result mustBe true
      }
    }

    "return false when failed to submit the request" in new Setup {
      val customsFinancialsApiUrl = "http://localhost:9878/customs-financials-api/eori/eori1/requested-notifications/C79Certificate"
      when[Future[HttpResponse]](mockHttpClient.DELETE(ArgumentMatchers.eq(customsFinancialsApiUrl), any)(any, any, any))
        .thenReturn(Future.failed(new RuntimeException("failed")))
      running(app) {
        val result = await(customsFinancialsApiConnector.deleteNotification("eori1", C79Certificate)(hc))
        result mustBe false
      }
    }
  }
  trait Setup {
    val mockHttpClient = mock[HttpClient]
    val app = applicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).build()
    val mockAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val customsFinancialsApiConnector = app.injector.instanceOf[CustomsFinancialsApiConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

}
