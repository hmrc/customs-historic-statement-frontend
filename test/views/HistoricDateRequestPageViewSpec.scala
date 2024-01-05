/*
 * Copyright 2024 HM Revenue & Customs
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
import forms.HistoricDateRequestPageFormProvider
import models.{HistoricDates, NormalMode, SecurityStatement}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.HistoricDateRequestPageView

class HistoricDateRequestPageViewSpec extends SpecBase {

  "view" should {
    "display correct text" when {
      "title is displayed" in new SetUp {
        view.title() mustBe s"${
          message(
            "cf.historic.document.request.SecurityStatement.title")
        } - ${message("service.name")} - GOV.UK"

      }

      "date is displayed" in new SetUp {
        view.getElementsByTag("h1").text() mustBe
          message("cf.historic.document.request.heading.SecurityStatement")
      }

      "sub header text is displayed" in new SetUp {
        view.text().contains(
          message("cf.historic.document.request.info-text.SecurityStatement")) mustBe true

      }

      "statement start date text and hint text is displayed" in new SetUp {
        view.text().contains(
          message("cf.historic.document.request.from.statements")) mustBe true

        view.getElementById("start-hint").text() mustBe message(
          "cf.historic.document.request.date.pvat.hint"
        )

      }

      "statement end date text and hint text is displayed" in new SetUp {
        view.text().contains(message("cf.historic.document.request.to.statements")) mustBe true

        view.getElementById("end-hint").text() mustBe message(
          "cf.historic.document.request.date.pvat.hint"
        )
      }

      "start date check box month and year is displayed" in new SetUp {
        view.text().contains(message("date.month")) mustBe true

        val startMonth: Element = view.getElementById("start.month")
        Option(startMonth) must not be empty

        view.text().contains(message("date.year")) mustBe true

        val endMonth: Element = view.getElementById("start.year")
        Option(endMonth) must not be empty

      }

      "display a back link" in new SetUp {
        view.getElementsByClass("govuk-back-link").attr("href") mustBe returnUrl
      }

      "display a continue button" in new SetUp {
        view.getElementsByClass("govuk-button").html().contains(message(
          "cf.historic.document.request.continue"
        )) mustBe true
      }
    }
  }

  trait SetUp {
    val app: Application = applicationBuilder().build()
    val returnUrl = "http://localhost:9398/customs/documents/adjustments"

    implicit val message: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()

    val form: Form[HistoricDates] = new HistoricDateRequestPageFormProvider().apply(SecurityStatement)

    val view: Document = Jsoup.parse(app.injector.instanceOf[HistoricDateRequestPageView].apply(
      form,
      NormalMode,
      SecurityStatement,
      returnUrl,
      Some("accountNumber"),
      Some(false)
    ).body)
  }

}
