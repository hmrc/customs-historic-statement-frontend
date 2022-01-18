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
import org.mockito.ArgumentMatchers
import play.api.inject.bind
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.Future

class CustomsSessionCacheConnectorSpec extends SpecBase {

  "getAccountNumber" should {

    "return account number for sessionId and linkId" in new Setup {
      val customsSessionCacheUrl = "http://localhost:9840/customs/session-cache/account-link/12345/Ab123"

      when[Future[SessionCacheResponse]](mockHttpClient.GET(ArgumentMatchers.eq(customsSessionCacheUrl), any, any)(any, any, any))
        .thenReturn(Future.successful(SessionCacheResponse("45678")))
      running(app) {
        val result = await(customsSessionCacheConnector.getAccountNumber("12345", "Ab123")(hc))
        result mustBe Some("45678")
      }
    }

  "return false when failed to submit the request" in new Setup {
      val customsSessionCacheUrl = "http://localhost:9840/customs/session-cache/account-link/12345/Ab123"

      when[Future[HttpResponse]](mockHttpClient.GET(ArgumentMatchers.eq(customsSessionCacheUrl), any, any)(any, any, any))
        .thenReturn(Future.failed(new RuntimeException("failed")))
      running(app) {
        val result = await(customsSessionCacheConnector.getAccountNumber("12345", "Ab123")(hc))
        result mustBe None
      }
    }
  }

  trait Setup {
    val mockHttpClient = mock[HttpClient]
    val app = applicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).build()
    val mockAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val customsSessionCacheConnector = app.injector.instanceOf[CustomsSessionCacheConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

}
