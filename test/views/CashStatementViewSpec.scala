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

import models.FileFormat.Csv
import models.{CDSCashAccount, CashStatementFile, CashStatementFileMetadata, EoriHistory}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import viewmodels.{CashStatementForEori, CashStatementMonthToMonth, CashStatementViewModel}
import views.html.CashStatementView
import utils.TestData.{date, downloadUrl, eori, fileName, periodStartDay, periodStartMonth, periodStartYear}

class CashStatementViewSpec extends ViewTestHelper {

  "view" should {

    "display correct title and contents" in new Setup {
      titleShouldBeCorrect(view, "cf.cash-statement-requested-heading")
      pageShouldContainBackLinkUrl(view, appConfig.returnLink(CDSCashAccount))
      headingShouldBeCorrect
      requestedParagraphTextShouldBeCorrect
      requestedListParagraphTextShouldBeCorrect
      helpAndSupportGuidanceShouldBePresent
    }
  }

  private def helpAndSupportGuidanceShouldBePresent(implicit view: Document): Assertion =
    view
      .getElementById("search-transactions-support-message-heading")
      .html()
      .contains(messages("site.support.heading")) mustBe true

  private def headingShouldBeCorrect(implicit view: Document): Assertion =
    view
      .getElementById("requested-cash-statement-heading")
      .html()
      .contains(messages("cf.cash-statement-requested-heading")) mustBe true

  private def requestedParagraphTextShouldBeCorrect(implicit view: Document): Assertion =
    view
      .getElementById("requested-cash-statement-paragraph")
      .html()
      .contains(messages("cf.cash-statement-requested-paragraph")) mustBe true

  private def requestedListParagraphTextShouldBeCorrect(implicit view: Document): Assertion =
    view
      .getElementById("requested-cash-statement-list-paragraph")
      .html()
      .contains(messages("cf.cash-statement-requested-list-paragraph")) mustBe true

  trait Setup {
    val size = 120L

    val eoriHistory: EoriHistory = EoriHistory(eori, Some(date), Some(date))

    val cashStatementFile: CashStatementFile = CashStatementFile(
      fileName,
      downloadUrl,
      size,
      CashStatementFileMetadata(
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        Csv,
        CDSCashAccount,
        Some("requestId")
      ),
      eori
    )

    val cashStatementsByMonth: CashStatementMonthToMonth =
      CashStatementMonthToMonth(date, date, Seq(cashStatementFile))()

    val cashStatementsForEori: CashStatementForEori =
      CashStatementForEori(eoriHistory, Seq(cashStatementsByMonth), Seq.empty)

    val cashStatementViewModel: CashStatementViewModel = CashStatementViewModel(Seq(cashStatementsForEori))

    implicit val view: Document = Jsoup.parse(
      application.injector
        .instanceOf[CashStatementView]
        .apply(cashStatementViewModel, appConfig.returnLink(CDSCashAccount))
        .body
    )
  }
}
