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

package controllers

import models.{EmailUnverifiedResponse, EmailVerifiedResponse}
import play.api.inject._
import uk.gov.hmrc.http.HttpReads
import base.SpecBase
import org.mockito.ArgumentMatchers
import play.api.test.Helpers._
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import java.net.URL
import scala.concurrent.{ExecutionContext, Future}

class EmailControllerSpec extends SpecBase {

  "EmailController" must {
    "return unverified email response" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(response))

      when(mockHttpClient.get(any[URL]())(any())).thenReturn(requestBuilder)

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
        val result  = route(app, request).value
        status(result) shouldBe OK
      }
    }

    "display verify your email page when exception occurs while connector making the API call" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailUnverifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.failed(new RuntimeException("API call failed")))

      when(mockHttpClient.get(any[URL]())(any())).thenReturn(requestBuilder)

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
        val result  = route(app, request).value
        status(result) shouldBe OK
      }
    }

    "display undeliverable email response" in new Setup {

      when(requestBuilder.execute(any[HttpReads[EmailVerifiedResponse]], any[ExecutionContext]))
        .thenReturn(Future.successful(EmailVerifiedResponse(Some("undeliverableEmail"))))

      when(mockHttpClient.get(any[URL]())(any())).thenReturn(requestBuilder)

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUndeliverable().url)
        val result  = route(app, request).value
        status(result) shouldBe OK
      }
    }
  }

  trait Setup {
    val expectedResult                        = Some("unverifiedEmail")
    implicit val mockHttpClient: HttpClientV2 = mock[HttpClientV2]
    val requestBuilder: RequestBuilder        = mock[RequestBuilder]

    val response = EmailUnverifiedResponse(Some("unverifiedEmail"))

    val app = applicationBuilder()
      .overrides(
        bind[HttpClientV2].toInstance(mockHttpClient),
        bind[RequestBuilder].toInstance(requestBuilder)
      )
      .build()
  }
}
