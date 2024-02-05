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
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

class LogoutControllerSpec extends SpecBase {

  "logout" should {
    "redirect the user to logout with a continue url of the feedback survey" in new Setup {
      val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest(GET, routes.LogoutController.logout.url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          "http://localhost:9553/bas-gateway/sign-out-without-state?continue=https%3A%2F%2F" +
            "www.development.tax.service.gov.uk%2Ffeedback%2FCDS-FIN"
      }
    }
  }

  "logoutNoSurvey" should {
    "redirect the user to logout with no feedback survey continueUrl" in new Setup {
      val request = fakeRequest(GET, routes.LogoutController.logoutNoSurvey.url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe
          "http://localhost:9553/bas-gateway/sign-out-without-state?continue=http%3A%2F%2F" +
            "localhost%3A9876%2Fcustoms%2Fpayment-records"
      }
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
  }
}
