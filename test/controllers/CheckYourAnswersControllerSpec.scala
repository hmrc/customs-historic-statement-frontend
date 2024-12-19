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
import connectors.CustomsFinancialsApiConnector
import models.{C79Certificate, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.AccountNumber
import play.api.{Application, inject}
import play.api.test.Helpers._
import repositories.SessionRepository

import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return Ok" in new Setup {
      val app: Application = createApp()

      running(app) {
        val request = fakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(C79Certificate).url)
        val result  = route(app, request).value

        status(result) mustBe OK
      }
    }

    "redirect to SessionExpired page if user returns from confirmation page using browser back button" in new Setup {
      val app: Application = createApp(emptyUserAnswers)

      when(mockSessionRepo.clear(any)).thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.CheckYourAnswersController.onPageLoad(C79Certificate).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
      }
    }
  }

  "onSubmit" should {
    "redirect to the confirmation controller on a successful submission" in new Setup {
      when(mockCustomsFinancialsApiConnector.postHistoricDocumentRequest(any)(any))
        .thenReturn(Future.successful(true))

      val app: Application = createApp()

      running(app) {
        val request = fakeRequest(POST, routes.CheckYourAnswersController.onSubmit(C79Certificate).url)
        val result  = route(app, request).value

        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.ConfirmationPageController.onPageLoad(C79Certificate).url
      }
    }

    "redirect to Technical difficulties if the user answers aren't populated" in {
      val app = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(AccountNumber, "123").success.value)).build()

      running(app) {
        val request = fakeRequest(POST, routes.CheckYourAnswersController.onSubmit(C79Certificate).url)
        val result  = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.TechnicalDifficultiesController.onPageLoad().url
      }
    }

    "redirect to Technical difficulties if the call to the api fails" in new Setup {
      when(mockCustomsFinancialsApiConnector.postHistoricDocumentRequest(any)(any))
        .thenReturn(Future.successful(false))

      val app: Application = createApp()

      running(app) {
        val request = fakeRequest(POST, routes.CheckYourAnswersController.onSubmit(C79Certificate).url)
        val result  = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.TechnicalDifficultiesController.onPageLoad().url
      }
    }
  }

  trait Setup {
    val mockCustomsFinancialsApiConnector: CustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockSessionRepo: SessionRepository                               = mock[SessionRepository]

    def createApp(userAnswers: UserAnswers = populatedUserAnswers): Application =
      applicationBuilder(userAnswers = Some(userAnswers))
        .overrides(
          inject.bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
          inject.bind[SessionRepository].toInstance(mockSessionRepo)
        )
        .build()
  }
}
