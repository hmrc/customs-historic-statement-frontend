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
import models.{C79Certificate, HistoricDocumentRequest}
import org.mockito.ArgumentMatchers
import play.api.http.Status
import play.api.inject.bind
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HttpReads, HttpResponse, StringContextOps}
import utils.Utils.emptyString
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class CustomsFinancialsApiConnectorSpec extends SpecBase {

  "postHistoricDocumentRequest" should {

    "submit request successfully" in new Setup {
      val customsFinancialsApiUrl = "http://localhost:9878/customs-financials-api/historic-document-request"

      val historicDocumentRequest =
        HistoricDocumentRequest(C79Certificate, LocalDate.now(), LocalDate.now().plusMonths(1), Some("1234"))

      when(requestBuilder.withBody(ArgumentMatchers.eq(historicDocumentRequest))(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse.apply(Status.NO_CONTENT, emptyString)))

      when(mockHttpClient.post(ArgumentMatchers.eq(url"$customsFinancialsApiUrl"))(any())).thenReturn(requestBuilder)

      running(app) {
        val result = await(customsFinancialsApiConnector.postHistoricDocumentRequest(historicDocumentRequest)(hc))
        result mustBe true
      }
    }

    "return false when failed to submit the request" in new Setup {
      val customsFinancialsApiUrl = "http://localhost:9878/customs-financials-api/historic-document-request"

      val historicDocumentRequest =
        HistoricDocumentRequest(C79Certificate, LocalDate.now(), LocalDate.now().plusMonths(1), Some("1234"))

      when(requestBuilder.withBody(ArgumentMatchers.eq(historicDocumentRequest))(any(), any(), any()))
        .thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("failed")))

      when(mockHttpClient.post(ArgumentMatchers.eq(url"$customsFinancialsApiUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(customsFinancialsApiConnector.postHistoricDocumentRequest(historicDocumentRequest)(hc))
        result mustBe false
      }
    }
  }

  "deleteNotification" should {
    "return true when deletion is successful" in new Setup {
      val customsFinancialsApiUrl =
        "http://localhost:9878/customs-financials-api/eori/eori1/requested-notifications/C79Certificate"

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(HttpResponse.apply(Status.OK, emptyString)))

      when(mockHttpClient.delete(ArgumentMatchers.eq(url"$customsFinancialsApiUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(customsFinancialsApiConnector.deleteNotification("eori1", C79Certificate)(hc))
        result mustBe true
      }
    }

    "return false when failed to submit the request" in new Setup {
      val customsFinancialsApiUrl =
        "http://localhost:9878/customs-financials-api/eori/eori1/requested-notifications/C79Certificate"

      when(requestBuilder.withBody(any())(any(), any(), any())).thenReturn(requestBuilder)

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("failed")))

      when(mockHttpClient.delete(ArgumentMatchers.eq(url"$customsFinancialsApiUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(customsFinancialsApiConnector.deleteNotification("eori1", C79Certificate)(hc))
        result mustBe false
      }
    }
  }

  trait Setup {
    val mockHttpClient: HttpClientV2   = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val app = applicationBuilder()
      .overrides(
        bind[HttpClientV2].toInstance(mockHttpClient),
        bind[RequestBuilder].toInstance(requestBuilder)
      )
      .build()

    val mockAppConfig                 = app.injector.instanceOf[FrontendAppConfig]
    val customsFinancialsApiConnector = app.injector.instanceOf[CustomsFinancialsApiConnector]
  }
}
