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
import controllers.routes
import models.{C79Certificate, FileRole}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.retrieve.Email
import views.html.ConfirmationPageView

class ConfirmationPageViewSpec extends SpecBase {

  "view" should {

    "display correct text" when {

      "title should display correctly" in new Setup {
        view.title() mustBe s"${messages("cf.accounts.title")} - ${messages("service.name")} - GOV.UK"
      }

      "date should display correctly" in new Setup {
        view.getElementById("email-confirmation-panel-date").text() mustBe messages("03 Oct 2021 to 04 Sept 2022")
      }

      "subheader-text should display correctly" in new Setup {
        view.getElementById("email-confirmation-subheader").text() mustBe messages(
          "cf.historic.document.request.confirmation.subheader-text.next"
        )
      }

      "email confirmation should display correctly" in new Setup {
        view.getElementById("email-confirmation").text() mustBe messages(
          "cf.historic.document.request.confirmation.body-text.request",
          email.value
        )
      }

      "download your PVAT statements text should display correctly" in new Setup {
        view
          .text()
          .contains(messages("cf.historic.document.request.confirmation.body-text2.PostponedVATStatement"))
      }

      "body-text2 should display correctly" in new Setup {
        view.getElementById("body-text2").text() mustBe messages(
          s"cf.historic.document.request.confirmation.body-text2.${fileRole.name}"
        )
      }

      "link should display correct text" in new Setup {
        view.getElementById("link-text").text() mustBe messages(
          s"cf.historic.document.request.confirmation.${fileRole.name}.link-text"
        )
      }
    }

    "display Welsh toggle" in new Setup {
      val languageSelectHtml: Document =
        Jsoup.parse(view.getElementsByClass("hmrc-language-select__list-item").html())

      val welshLinkElement: String = languageSelectHtml.getElementsByTag("a").html()

      languageSelectHtml.toString.contains("English") mustBe true
      welshLinkElement.contains("Cymraeg") mustBe true
    }
  }

  trait Setup {
    implicit val request: FakeRequest[AnyContentAsEmpty.type] =
      fakeRequest(GET, routes.ConfirmationPageController.onPageLoad(C79Certificate).url)

    val email: Email       = Email("some@email.com")
    val fileRole: FileRole = C79Certificate
    val returnLink: String = "http://localhost:9398/customs/documents/import-vat"
    val dates: String      = "03 Oct 2021 to 04 Sept 2022"

    val view: Document =
      Jsoup.parse(
        application.injector.instanceOf[ConfirmationPageView].apply(Some(email), fileRole, returnLink, dates).body
      )
  }
}
