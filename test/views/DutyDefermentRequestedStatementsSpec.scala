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

import models.DDStatementType.{Supplementary, Weekly}
import models.FileFormat.Pdf
import models.{DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata, DutyDefermentStatementsForEori, EoriHistory}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import viewmodels.DutyDefermentAccountViewModel
import views.html.DutyDefermentRequestedStatements

import java.time.LocalDate

class DutyDefermentRequestedStatementsSpec extends ViewTestHelper {
  "view" should {
    "display correct title and contents" in new Setup {
      titleShouldBeCorrect(view, "cf.account.detail.requested.title")
      pageShouldContainBackLinkUrl(view, config.returnLink("dutyDeferment"))
      shouldContainAccountNumber
      headingShouldBeCorrect
      subHeadingShouldBeCorrect
      eoriNumberShouldBeCorrect
    }
  }

  trait Setup {

    private val someEori = "12345678"
    private val someDan = "12345"
    private val someRequestId: Option[String] = Some("Ab1234")
    private val pdfFileName = "2018_03_01-08.pdf"
    private val pdfUrl = "url.pdf"
    private val pdfSize = 1024L
    private val periodStartYear = 2018
    private val periodStartMonth = 3
    private val periodStartMonth2 = 2
    private val periodStartDay = 1
    private val periodEndYear = 2018
    private val periodEndMonth2 = 3
    private val periodEndMonth = 2
    private val periodEndDay = 8
    private val dutyPaymentType = "BACS"

    private val dutyDeferementFile: DutyDefermentStatementFile =
      DutyDefermentStatementFile(pdfFileName,
        pdfUrl,
        pdfSize,
        DutyDefermentStatementFileMetadata(periodStartYear,
          periodStartMonth,
          periodStartDay,
          periodEndYear,
          periodEndMonth,
          periodEndDay,
          Pdf,
          DutyDefermentStatement,
          Weekly,
          Some(true),
          Some(dutyPaymentType),
          someDan,
          someRequestId))

    private val dutyDeferementFile_2: DutyDefermentStatementFile = DutyDefermentStatementFile(pdfFileName,
      pdfUrl,
      pdfSize,
      DutyDefermentStatementFileMetadata(periodStartYear,
        periodStartMonth2,
        periodStartDay,
        periodEndYear,
        periodEndMonth2,
        periodEndDay,
        Pdf,
        DutyDefermentStatement,
        Supplementary,
        Some(true),
        Some(dutyPaymentType),
        someDan,
        someRequestId
      )
    )

    private val localDateYear = 2019
    private val localDateMonth = 11
    private val localDateDay = 10

    private val eoriHistory = EoriHistory(someEori,
      Some(LocalDate.of(localDateYear, localDateMonth, localDateDay)),
      Some(LocalDate.of(localDateYear, localDateMonth, localDateDay)))

    private val dutyDefermentStatementsForEori: DutyDefermentStatementsForEori =
      DutyDefermentStatementsForEori.apply(eoriHistory, Seq(dutyDeferementFile), Seq(dutyDeferementFile_2))

    private val dutyDefermentModel = DutyDefermentAccountViewModel(
      "accountNumber",
      Seq(dutyDefermentStatementsForEori),
      isNiAccount = false)

    implicit val view: Document = Jsoup.parse(app.injector.instanceOf[DutyDefermentRequestedStatements].
      apply(dutyDefermentModel, config.returnLink("dutyDeferment")).body)
  }

  private def shouldContainAccountNumber(implicit view: Document): Assertion = {
    view.getElementById("eori-heading").html().contains(
      msg("cf.account.detail.requested.deferment-account-secondary-heading")
    ) mustBe true
  }

  private def headingShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("requested-statements-heading").html().contains(
      msg("cf.account.detail.requested.deferment-account-heading")
    ) mustBe true
  }

  private def subHeadingShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("requested-statements-available-text").html().contains(
      msg("cf.account.detail.requested.deferment-account-statements-available.text")
    ) mustBe true
  }

  private def eoriNumberShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("historic-eori-0").html().contains(
      msg("cf.account.details.previous-eori", "12345678")
    ) mustBe true
  }
}
