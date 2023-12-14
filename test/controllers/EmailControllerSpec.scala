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

import connectors.CustomsFinancialsApiConnector
import models.{EmailUnverifiedResponse, EmailVerifiedResponse}
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.inject._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}
import base.SpecBase
import org.mockito.ArgumentMatchers
import play.api.test.Helpers._

import scala.concurrent.Future

class EmailControllerSpec extends SpecBase {

  "EmailController" must {

    "return unverified email" in new Setup {

      when[Future[EmailUnverifiedResponse]](
        mockHttpClient.GET(ArgumentMatchers.endsWith("/subscriptions/unverified-email-display"), any, any)
        (any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val connector = app.injector.instanceOf[CustomsFinancialsApiConnector]

        val result: Future[Option[String]] = connector.isEmailUnverified(hc)
        await(result) mustBe expectedResult
      }
    }

    "return unverified email response" in new Setup {

      when[Future[EmailUnverifiedResponse]](
        mockHttpClient.GET(ArgumentMatchers.endsWith("/subscriptions/unverified-email-display"), any, any)
        (any, any, any))
        .thenReturn(Future.successful(response))

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUnverified().url)
        val result = route(app, request).value
        status(result) shouldBe OK
      }
    }

    "return undeliverable email response" in {

      val mockHttpClient = mock[HttpClient]

      when[Future[EmailVerifiedResponse]](
        mockHttpClient.GET(ArgumentMatchers.endsWith("/subscriptions/email-display"), any, any)
        (any, any, any))
        .thenReturn(Future.successful(EmailVerifiedResponse(Some("undeliverableEmail"))))

      val app = applicationBuilder().overrides(
        bind[HttpClient].toInstance(mockHttpClient)
      ).build()

      running(app) {
        val request = fakeRequest(GET, routes.EmailController.showUndeliverable().url)
        val result = route(app, request).value
        status(result) shouldBe OK
      }
    }
  }

  trait Setup {
    val expectedResult = Some("unverifiedEmail")
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val mockHttpClient = mock[HttpClient]

    val response = EmailUnverifiedResponse(Some("unverifiedEmail"))

    val app = applicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttpClient)
    ).build()
  }

}
