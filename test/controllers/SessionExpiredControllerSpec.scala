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
import config.FrontendAppConfig
import play.api.test.Helpers._
import views.html.SessionExpiredView

class SessionExpiredControllerSpec extends SpecBase {

  "SessionExpired Controller" must {

    "return OK and the correct view for a GET" in {

      val app       = applicationBuilder(userAnswers = None).build()
      val request   = fakeRequest(GET, routes.SessionExpiredController.onPageLoad().url)
      val result    = route(app, request).value
      val view      = app.injector.instanceOf[SessionExpiredView]
      val appConfig = app.injector.instanceOf[FrontendAppConfig]

      status(result) mustEqual OK

      contentAsString(result) mustEqual
        view()(request, messages(app), appConfig).toString

      app.stop()
    }
  }
}
