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
import models.{
  C79Certificate, CDSCashAccount, DateMessages, DutyDefermentStatement, FileRole, HistoricDates, NormalMode,
  PostponedVATStatement, SecurityStatement
}
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}
import play.api.data.Form
import views.html.HistoricDateRequestPageView

import java.time.LocalDate

class HistoricDateRequestPageViewSpec extends SetUpWithSpecBase {

  "view" should {

    fileRoles.foreach { fileRole =>
      s"display correct text for $fileRole" when {
        val viewDoc = view(fileRole)

        "title is displayed" in {
          viewDoc
            .title() mustBe s"${messages(s"cf.historic.document.request.$fileRole.title")} - ${messages("service.name")} - GOV.UK"
        }

        "date is displayed" in {
          viewDoc.getElementsByTag("h1").text() mustBe
            messages(s"cf.historic.document.request.heading.$fileRole")
        }

        "sub header text is displayed" in {
          viewDoc.text().contains(messages(s"cf.historic.document.request.info-text.$fileRole")) mustBe true
        }

        "earliest requestable date inset text is displayed below the heading" in {
          viewDoc.getElementById("earliest-requestable-year").text() mustBe earliestDateMessage

          viewDoc.select("h1 + #earliest-requestable-year").size() mustBe 1
        }

        "statement start date text and hint text is displayed" in {
          viewDoc.text().contains(startDateText) mustBe true
          viewDoc.getElementById("start-hint").text() mustBe startDateHint(fileRole)
        }

        "statement end date text and hint text is displayed" in {
          viewDoc.text().contains(endDateText) mustBe true
          viewDoc.getElementById("end-hint").text() mustBe endDateHint(fileRole)
        }

        "start date check box month and year is displayed" in {
          viewDoc.text().contains(messages("date.month")) mustBe true

          val startMonth: Element = viewDoc.getElementById("start.month")
          Option(startMonth) must not be empty

          viewDoc.text().contains(messages("date.year")) mustBe true

          val endMonth: Element = viewDoc.getElementById("start.year")
          Option(endMonth) must not be empty
        }

        "display a back link" in {
          viewDoc.getElementsByClass("govuk-back-link").attr("href") mustBe returnUrl
        }

        "display a continue button" in {
          viewDoc
            .getElementsByClass("govuk-button")
            .html()
            .contains(messages("cf.historic.document.request.continue")) mustBe true
        }
      }
    }
  }

}

trait SetUpWithSpecBase extends SpecBase {
  val returnUrl       = "http://localhost:9398/customs/documents/adjustments"
  val hintExampleDate = LocalDate.of(2025, 9, 1)

  lazy val earliestDateMessage: String =
    messages("cf.historic.document.request.form.error.date-too-far-in-past", "April", "2020")

  private def form(fileRole: FileRole): Form[HistoricDates] = new HistoricDateRequestPageFormProvider().apply(fileRole)

  val fileRoles: Seq[FileRole] = Seq(SecurityStatement, C79Certificate, PostponedVATStatement, DutyDefermentStatement)

  protected def view(fileRole: FileRole): Document =
    Jsoup.parse(
      application.injector
        .instanceOf[HistoricDateRequestPageView]
        .apply(
          form(fileRole),
          NormalMode,
          fileRole,
          returnUrl,
          DateMessages(fileRole),
          Some("accountNumber"),
          Some(false),
          earliestDateMessage,
          hintExampleDate
        )
        .body
    )

  val startDateText: String = messages("cf.historic.document.request.from")

  protected def startDateHint(fileRole: FileRole): String =
    fileRole match {
      case C79Certificate         => messages("cf.historic.document.request.date.C79Certificate.hint", "9", "2025")
      case PostponedVATStatement  =>
        messages("cf.historic.document.request.date.PostponedVATStatement.hint", "9", "2025")
      case DutyDefermentStatement =>
        messages("cf.historic.document.request.date.DutyDefermentStatement.hint", "9", "2025")
      case SecurityStatement      => messages("cf.historic.document.request.date.SecurityStatement.hint", "9", "2025")
      case CDSCashAccount         => messages("cf.historic.document.request.date.CashStatement.hint")
    }

  val endDateText: String                               = messages("cf.historic.document.request.to")
  protected def endDateHint(fileRole: FileRole): String =
    fileRole match {
      case C79Certificate => messages("cf.historic.document.request.endDate.C79Certificate.hint", "9", "2025")
      case _              => messages("cf.historic.document.request.endDate.hint", "9", "2025")
    }
}
