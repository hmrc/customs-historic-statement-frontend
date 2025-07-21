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

import base.SpecBase
import connectors.CustomsDataStoreConnector
import models.C79Certificate
import play.api.{Application, inject}
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.retrieve.Email
import views.html.ConfirmationPageView

import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

class ConfirmationPageControllerSpec extends SpecBase {

  "onPageLoad" must {

    "return OK and the correct view including a return link to the MIDVA dashboard" in new Setup {

      when(mockDataStoreConnector.getEmail(any)).thenReturn(Future.successful(Right(Email("some@email.com"))))
      when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.ConfirmationPageController.onPageLoad(C79Certificate).url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            Some(Email("some@email.com")),
            C79Certificate,
            appConfig.financialsHomepage,
            "March 2018 to March 2018"
          )(request, messages, appConfig).toString

        contentAsString(result) must include(appConfig.financialsHomepage)
      }
    }

    "return None when an error occurs for email retrieval" in new Setup {

      when(mockDataStoreConnector.getEmail(any)).thenReturn(Future.successful(Left("An error occurred")))
      when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.ConfirmationPageController.onPageLoad(C79Certificate).url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            None,
            C79Certificate,
            appConfig.financialsHomepage,
            "March 2018 to March 2018"
          )(request, messages, appConfig).toString

        contentAsString(result) must include(appConfig.financialsHomepage)
      }
    }

    "return None when email retrieval fails with an exception" in new Setup {

      when(mockDataStoreConnector.getEmail(any)).thenReturn(Future.failed(new RuntimeException("Fail")))
      when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.ConfirmationPageController.onPageLoad(C79Certificate).url)
        val result  = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(
            None,
            C79Certificate,
            appConfig.financialsHomepage,
            "March 2018 to March 2018"
          )(request, messages, appConfig).toString

        contentAsString(result) must include(appConfig.financialsHomepage)
      }
    }
  }

  trait Setup {
    val mockSessionRepository: SessionRepository          = mock[SessionRepository]
    val mockDataStoreConnector: CustomsDataStoreConnector = mock[CustomsDataStoreConnector]

    val app: Application = applicationBuilder(userAnswers = Some(populatedUserAnswers))
      .overrides(
        inject.bind[SessionRepository].toInstance(mockSessionRepository),
        inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
      )
      .build()

    val view: ConfirmationPageView = app.injector.instanceOf[ConfirmationPageView]
  }
}
