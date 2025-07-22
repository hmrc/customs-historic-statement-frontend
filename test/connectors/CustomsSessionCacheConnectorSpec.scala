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
import org.mockito.ArgumentMatchers
import play.api.inject.bind
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HttpReads, HttpResponse, StringContextOps}
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import scala.concurrent.{ExecutionContext, Future}

class CustomsSessionCacheConnectorSpec extends SpecBase {

  "getAccountNumber" should {

    "return account number for sessionId and linkId" in new Setup {

      val customsSessionCacheUrl = "http://localhost:9840/customs/session-cache/account-link/12345/Ab123"

      when(requestBuilder.execute(any[HttpReads[SessionCacheResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(SessionCacheResponse("45678")))

      when(mockHttpClient.get(ArgumentMatchers.eq(url"$customsSessionCacheUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(customsSessionCacheConnector.getAccountNumber("12345", "Ab123")(hc))
        result mustBe Some("45678")
      }
    }

    "return account link for sessionId and linkId" in new Setup {

      val customsSessionCacheUrl = "http://localhost:9840/customs/session-cache/account-link/12345/Ab123"

      when(requestBuilder.execute(any[HttpReads[AccountLink]], any[ExecutionContext]))
        .thenReturn(Future.successful(AccountLink("GB123", "1234567", "a67dhdfkd8sf", "Open", Some(0), true)))

      when(mockHttpClient.get(ArgumentMatchers.eq(url"$customsSessionCacheUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(customsSessionCacheConnector.getAccountLink("12345", "Ab123")(hc))
        result mustBe Some(AccountLink("GB123", "1234567", "a67dhdfkd8sf", "Open", Some(0), true))
      }
    }

    "return false when failed to submit the request" in new Setup {

      val customsSessionCacheUrl = "http://localhost:9840/customs/session-cache/account-link/12345/Ab123"

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("failed")))

      when(mockHttpClient.get(ArgumentMatchers.eq(url"$customsSessionCacheUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(customsSessionCacheConnector.getAccountNumber("12345", "Ab123")(hc))
        result mustBe None
      }
    }

    "return None when getAccountLink request fails" in new Setup {

      val customsSessionCacheUrl = "http://localhost:9840/customs/session-cache/account-link/12345/Ab123"

      when(requestBuilder.execute(any[HttpReads[AccountLink]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("failed")))

      when(mockHttpClient.get(ArgumentMatchers.eq(url"$customsSessionCacheUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(customsSessionCacheConnector.getAccountLink("12345", "Ab123")(hc))
        result mustBe None
      }
    }
  }

  "SessionCacheResponse" should {
    "serialize and deserialize correctly" in new Setup {
      Json.toJson(sessionCacheResponse) mustBe sessionCacheJson
      sessionCacheJson.as[SessionCacheResponse] mustBe sessionCacheResponse
    }
  }

  "AccountLink" should {
    "serialize and deserialize correctly with accountStatusId" in new Setup {
      Json.toJson(accountLinkWithId) mustBe accountLinkWithIdJson
      accountLinkWithIdJson.as[AccountLink] mustBe accountLinkWithId
    }
  }

  "AccountLink" should {
    "serialize and deserialize correctly without accountStatusId" in new Setup {
      Json.toJson(accountLinkWithoutIdJson) mustBe accountLinkWithoutIdJson
      accountLinkWithoutIdJson.as[AccountLink] mustBe accountLinkWithoutId
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

    val customsSessionCacheConnector: CustomsSessionCacheConnector =
      app.injector.instanceOf[CustomsSessionCacheConnector]
    val sessionCacheResponse: SessionCacheResponse                 = SessionCacheResponse("123456")
    val sessionCacheJson: JsValue                                  = Json.parse("""{"accountNumber":"123456"}""")

    val accountLinkWithId: AccountLink = AccountLink(
      eori = "GB12345",
      accountNumber = "AC123",
      linkId = "Link123",
      accountStatus = "Active",
      accountStatusId = Some(1),
      isNiAccount = true
    )

    val accountLinkWithIdJson: JsValue = Json.parse(
      """{
        |      "eori":"GB12345",
        |      "accountNumber":"AC123",
        |      "linkId":"Link123",
        |      "accountStatus":"Active",
        |      "accountStatusId":1,
        |      "isNiAccount":true
        |}""".stripMargin
    )

    val accountLinkWithoutId: AccountLink = AccountLink(
      eori = "GB12345",
      accountNumber = "AC123",
      linkId = "Link123",
      accountStatus = "Inactive",
      accountStatusId = None,
      isNiAccount = false
    )

    val accountLinkWithoutIdJson: JsValue = Json.parse(
      """{
        |      "eori":"GB12345",
        |      "accountNumber":"AC123",
        |      "linkId":"Link123",
        |      "accountStatus":"Inactive",
        |      "isNiAccount":false
        |}""".stripMargin
    )

  }
}
