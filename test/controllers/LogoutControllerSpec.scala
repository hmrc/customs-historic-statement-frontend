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
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import repositories.SessionRepository
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import scala.concurrent.Future

class LogoutControllerSpec extends SpecBase {

  "logout" should {

    "redirect the user to logout with a continue url of the feedback survey" when {

      "sessionRepository clears the user session successfully" in new Setup {
        val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(GET, routes.LogoutController.logout().url)

        when(sessionRepoMock.clear(any)).thenReturn(Future.successful(true))

        running(app) {
          val result = route(app, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            "http://localhost:9553/bas-gateway/sign-out-without-state?" +
              "continue=http%3A%2F%2Flocalhost%3A9514%2Ffeedback%2FCDS-FIN"
        }
      }

      "error occurs in clearing user session" in new Setup {
        val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(GET, routes.LogoutController.logout().url)

        when(sessionRepoMock.clear(any)).thenReturn(Future.failed(new RuntimeException("error occurred")))

        running(app) {
          val result = route(app, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            "http://localhost:9553/bas-gateway/sign-out-without-state?" +
              "continue=http%3A%2F%2Flocalhost%3A9514%2Ffeedback%2FCDS-FIN"
        }
      }

    }
  }

  "logoutNoSurvey" should {

    "redirect the user to logout with no feedback survey continueUrl" when {

      "sessionRepository clears the user session successfully" in new Setup {
        val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(GET, routes.LogoutController.logoutNoSurvey().url)

        when(sessionRepoMock.clear(any)).thenReturn(Future.successful(true))

        running(app) {
          val result = route(app, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            "http://localhost:9553/bas-gateway/sign-out-without-state?" +
              "continue=http%3A%2F%2Flocalhost%3A9876%2Fcustoms%2Fpayment-records"
        }
      }

      "error occurs in clearing user session" in new Setup {
        val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(GET, routes.LogoutController.logoutNoSurvey().url)

        when(sessionRepoMock.clear(any)).thenReturn(Future.failed(new RuntimeException("error occurred")))

        running(app) {
          val result = route(app, request).value

          status(result) mustBe SEE_OTHER

          redirectLocation(result).value mustBe
            "http://localhost:9553/bas-gateway/sign-out-without-state?" +
              "continue=http%3A%2F%2Flocalhost%3A9876%2Fcustoms%2Fpayment-records"
        }
      }
    }

  }

  trait Setup {
    val sessionRepoMock: SessionRepository = mock[SessionRepository]

    val app: Application = applicationBuilder().overrides(
      inject.bind[SessionRepository].toInstance(sessionRepoMock)
    ).build()
  }
}
