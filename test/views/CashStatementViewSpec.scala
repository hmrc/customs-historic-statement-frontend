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
import models.{CashStatement, CashStatementFile, CashStatementFileMetadata, EoriHistory}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import viewmodels.{CashStatementByMonth, CashStatementForEori, CashStatementViewModel}
import views.html.CashStatementView

import java.time.LocalDate

class CashStatementViewSpec extends ViewTestHelper {

  "view" should {
    
    "display correct title and contents" in new Setup {
      titleShouldBeCorrect(view, "cf.cash-statement-requested-account-title")
      pageShouldContainBackLinkUrl(view, config.returnLink(CashStatement))
      headingShouldBeCorrect
      requestedParagraphTextShouldBeCorrect
      requestedListParagraphTextShouldBeCorrect
    }
  }

  private def headingShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("requested-cash-statement-heading")
      .html()
      .contains(msg("cf.cash-statement-requested-heading")) mustBe true
  }

  private def requestedParagraphTextShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("requested-cash-statement-paragraph")
      .html()
      .contains(msg("cf.cash-statement-requested-paragraph")) mustBe true
  }

  private def requestedListParagraphTextShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("requested-cash-statement-list-paragraph")
      .html()
      .contains(msg("cf.cash-statement-requested-list-paragraph")) mustBe true
  }

  trait Setup {

    val someEori = "12345678"
    val localDateYear = 2020
    val localDateMonth = 10
    val localDateDay = 1
    val filename: String = "statement_file_01"
    val downloadURL: String = "download_url_01"
    val size = 120L
    val periodStartYear: Int = 2019
    val periodStartMonth: Int = 7
    val periodStartDay: Int = 10

    val eoriHistory: EoriHistory = EoriHistory(someEori,
      Some(LocalDate.of(localDateYear, localDateMonth, localDateDay)),
      Some(LocalDate.of(localDateYear, localDateMonth, localDateDay)))

    val cashStatementFile: CashStatementFile = CashStatementFile(
      filename,
      downloadURL,
      size,
      CashStatementFileMetadata(
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        Csv,
        CashStatement,
        Some("requestId")
      ), someEori)

    val cashStatementsByMonth: CashStatementByMonth = CashStatementByMonth(
      LocalDate.of(localDateYear, localDateMonth, localDateDay),
      Seq(cashStatementFile))

    val cashStatementsForEori: CashStatementForEori = CashStatementForEori(
      eoriHistory,
      Seq(cashStatementsByMonth),
      Seq.empty)

    val accountNumber = "12345"

    val cashStatementViewModel: CashStatementViewModel = CashStatementViewModel(Seq(cashStatementsForEori))

    implicit val view: Document = Jsoup.parse(app.injector.instanceOf[CashStatementView].apply(
      cashStatementViewModel,
      config.returnLink(CashStatement)).body)
  }
}
