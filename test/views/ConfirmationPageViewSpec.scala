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

package views

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import models.{C79Certificate, FileRole}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.Email
import views.html.ConfirmationPageView

class ConfirmationPageViewSpec extends SpecBase {

  "view" should {
    "display correct text" when {
      "title should display correctly" in new Setup {
        running(app) {
          view.title() mustBe s"${messages(app)(
            "cf.accounts.title")} - ${messages(app)("service.name")} - GOV.UK"
        }
      }

      "date should display correctly" in new Setup {
        running(app) {
          view.getElementById(
            "email-confirmation-panel-date").text() mustBe messages(app)(
            "03 Oct 2021 to 04 Sept 2022")
        }
      }

      "subheader-text should display correctly" in new Setup {
        running(app) {
          view.getElementById(
            "email-confirmation-subheader").text() mustBe messages(app)(
            "cf.historic.document.request.confirmation.subheader-text.next")
        }
      }

      "email confirmation should display correctly" in new Setup {
        running(app) {
            view.getElementById(
              "email-confirmation").text() mustBe messages(app)(
              "cf.historic.document.request.confirmation.body-text.request", email.value)
          }
      }
      
      "body-text2 should display correctly" in new Setup {
        running(app) {
          view.getElementById(
            "body-text2").text() mustBe messages(app)(
            s"cf.historic.document.request.confirmation.body-text2.${fileRole.name}")
        }
      }
    }
  }

  trait Setup {
    val app: Application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()

    implicit val msges: Messages = messages(app)
    implicit val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad(C79Certificate).url)

    val email: Email = Email("some@email.com")
    val fileRole: FileRole = C79Certificate
    val returnLink: String = "http://localhost:9398/customs/documents/import-vat"
    val dates: String = "03 Oct 2021 to 04 Sept 2022"

    val view: Document = Jsoup.parse(
      app.injector.instanceOf[ConfirmationPageView].apply(
        Some(email),
        fileRole,
        returnLink,
        dates).body)
  }
}
