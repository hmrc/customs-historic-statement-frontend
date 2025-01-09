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

import controllers.routes
import models.{C79Certificate, DutyDefermentStatement, FileRole, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import viewmodels.CheckYourAnswersHelper
import views.html.CheckYourAnswersView

class CheckYourAnswersViewSpec extends ViewTestHelper {
  "view" should {
    "display correct title and contents" when {
      "the file role is C79 Certificate" in new Setup {
        implicit val viewDoc: Document = view()

        titleShouldBeCorrect(viewDoc, "cf.historic.document.request.review.C79Certificate.title")
        pageShouldContainBackLinkUrl(viewDoc, returnUrl)
        shouldContainSelectedDuration
        shouldContainChangeLink
        shouldContainConfirmAndSendButton
      }

      "the file role is Duty Deferment" in new Setup {
        implicit val viewDoc: Document = view(DutyDefermentStatement)
        titleShouldBeCorrect(viewDoc, "cf.historic.document.request.review.DutyDefermentStatement.title")

        shouldContainSecondaryHeading(
          view = viewDoc,
          accountNumberText =
            s"${messages("cf.account.detail.requested.deferment-account-secondary-heading")} accountNumber"
        )
      }

      "the file role is Duty Deferment and NI Indicator is enabled" in new Setup {
        implicit val viewDoc: Document = view(DutyDefermentStatement, Some(true))
        titleShouldBeCorrect(viewDoc, "cf.historic.document.request.review.DutyDefermentStatement.title")

        shouldContainSecondaryHeading(
          view = viewDoc,
          accountNumberText = messages("cf.account.detail.requested.deferment-account-secondary-heading.NiAccount")
        )
      }
    }
  }

  private def shouldContainSecondaryHeading(implicit view: Document, accountNumberText: String): Assertion =
    view.getElementById("eori-heading").text().contains(accountNumberText) mustBe true

  private def shouldContainSelectedDuration(implicit view: Document): Assertion =
    view.getElementsByClass("govuk-summary-list__value").text().contains("October 2019 to October 2019") mustBe true

  private def shouldContainChangeLink(implicit view: Document): Assertion = {
    val visuallyHiddenLinks = view.getElementsByClass("govuk-summary-list__actions")

    visuallyHiddenLinks.html().contains(messages("site.change")) mustBe true

    view
      .getElementsByClass("govuk-summary-list__actions")
      .html()
      .contains("/customs/historic-statement/import-vat/change-request-date") mustBe true
  }

  private def shouldContainConfirmAndSendButton(implicit view: Document): Assertion =
    view.getElementsByClass("govuk-button").html().contains(messages("site.continue")) mustBe true

  trait Setup {
    val cyaHelper: CheckYourAnswersHelper = new CheckYourAnswersHelper(populatedUserAnswers)
    val fileRole: FileRole                = C79Certificate

    val returnUrl: String = routes.HistoricDateRequestPageController.onPageLoad(NormalMode, fileRole).url

    def view(fileRole: FileRole = C79Certificate, niIndicator: Option[Boolean] = Some(false)): Document =
      Jsoup.parse(
        application.injector
          .instanceOf[CheckYourAnswersView]
          .apply(
            helper = cyaHelper,
            fileRole = fileRole,
            maybeDan = Some("accountNumber"),
            niIndicator = niIndicator
          )
          .body
      )
  }
}
