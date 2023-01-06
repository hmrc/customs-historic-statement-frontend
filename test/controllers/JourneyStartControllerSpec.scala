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
import connectors.CustomsSessionCacheConnector
import models.{C79Certificate, DutyDefermentStatement, NormalMode, SecurityStatement}
import play.api.test.Helpers._
import play.api.{Application, inject}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderNames.xSessionId

import scala.concurrent.Future

class JourneyStartControllerSpec extends SpecBase {

  "dutyDeferment" should {
    "redirect to the HistoricDateRequestPageController when a valid request has been sent" in new Setup {
      when(mockSessionCacheConnector.getAccountNumber(any, any)(any))
        .thenReturn(Future.successful(Some("accountNumber")))
      when(mockSessionRepository.set(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.JourneyStartController.dutyDeferment("linkId").url).withHeaders(xSessionId -> "something")
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.HistoricDateRequestPageController.onPageLoad(NormalMode, DutyDefermentStatement).url
      }
    }

    "redirect to the session expired controller if no session id present" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.JourneyStartController.dutyDeferment("linkId").url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad.url
      }
    }

    "redirect to the session expired controller if no account number found associated to the linkId" in new Setup {
      when(mockSessionCacheConnector.getAccountNumber(any, any)(any))
        .thenReturn(Future.successful(None))

      running(app) {
        val request = fakeRequest(GET, routes.JourneyStartController.dutyDeferment("linkId").url).withHeaders(xSessionId -> "something")
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad.url
      }
    }
  }

  "nonDutyDeferment" should {
    "redirect to the HistoricDateRequestPageController when a valid request C79Certificate has been sent" in new Setup {
      when(mockSessionRepository.set(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.JourneyStartController.nonDutyDeferment(C79Certificate).url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.HistoricDateRequestPageController.onPageLoad(NormalMode, C79Certificate).url
      }
    }

    "redirect to the HistoricDateRequestPageController when a valid request SecurityStatement has been sent" in new Setup {
      when(mockSessionRepository.set(any))
        .thenReturn(Future.successful(true))

      running(app) {
        val request = fakeRequest(GET, routes.JourneyStartController.nonDutyDeferment(SecurityStatement).url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.HistoricDateRequestPageController.onPageLoad(NormalMode, SecurityStatement).url
      }
    }

    "redirect to the BAD_REQUEST when a invalid file role has been sent" in new Setup {
      running(app) {
        val request = fakeRequest(GET, "/customs/historic-statement/start-journey/invalid")
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "redirect to the technical difficulties page when an invalid request has been sent" in new Setup {
      running(app) {
        val request = fakeRequest(GET, routes.JourneyStartController.nonDutyDeferment(DutyDefermentStatement).url)
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.TechnicalDifficultiesController.onPageLoad.url
      }
    }
  }

  trait Setup {

    val mockSessionCacheConnector: CustomsSessionCacheConnector = mock[CustomsSessionCacheConnector]
    val mockSessionRepository: SessionRepository = mock[SessionRepository]

    val app: Application = applicationBuilder().overrides(
      inject.bind[CustomsSessionCacheConnector].toInstance(mockSessionCacheConnector),
      inject.bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()
  }


}
