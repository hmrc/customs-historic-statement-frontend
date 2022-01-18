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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import connectors.CustomsDataStoreConnector
import models.C79Certificate
import play.api.inject
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.retrieve.Email
import views.html.ConfirmationPageView

import scala.concurrent.Future

class ConfirmationPageControllerSpec extends SpecBase {

  "ConfirmationPage Controller" must {

    "return OK and the correct view for a GET" in {

      val mockSessionRepository = mock[SessionRepository]
      val mockDataStoreConnector = mock[CustomsDataStoreConnector]

      val app = applicationBuilder(userAnswers = Some(populatedUserAnswers))
        .overrides(
          inject.bind[SessionRepository].toInstance(mockSessionRepository),
          inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
        ).build()

      when(mockDataStoreConnector.getEmail(any)(any)).thenReturn(Future.successful(Some(Email("some@email.com"))))
      when(mockSessionRepository.clear(any)).thenReturn(Future.successful(true))

      val view = app.injector.instanceOf[ConfirmationPageView]
      val appConfig = app.injector.instanceOf[FrontendAppConfig]

      running(app) {
        val request = fakeRequest(GET, routes.ConfirmationPageController.onPageLoad(C79Certificate).url)
        val result = route(app, request).value

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(Some(Email("some@email.com")), C79Certificate, "http://localhost:9398/customs/documents/import-vat")(request, messages(app), appConfig).toString
      }

    }
  }
}
