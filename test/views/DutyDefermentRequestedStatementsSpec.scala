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
import play.api.routing.Router.empty.routes
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

    //    val backLinkUrl = routes.
    val someEori = "12345678"
    private val someDan = "12345"
    private val someRequestId: Option[String] = Some("Ab1234")
    val pdfFileName = "2018_03_01-08.pdf"
    val pdfUrl = "url.pdf"
    val pdfSize = 1024L
    val periodStartYear = 2018
    val periodStartMonth = 3
    val periodStartDay = 1
    val periodEndYear = 2018
    val periodEndMonth = 3
    val periodEndDay = 8
    val dutyPaymentType = "BACS"

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

    private val dutyDeferementFile_2: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 2, 1, 2018, 2, 8, Pdf, DutyDefermentStatement, Supplementary, Some(true), Some("BACS"), someDan, someRequestId))

    private val eoriHistory = EoriHistory(someEori, Some(LocalDate.of(2019, 11, 10)),
      Some(LocalDate.of(2019, 12, 10)))

    private val dutyDefermentStatementsForEori: DutyDefermentStatementsForEori =
      DutyDefermentStatementsForEori.apply(eoriHistory, Seq(dutyDeferementFile), Seq(dutyDeferementFile_2))

    private val dutyDefermentModel = DutyDefermentAccountViewModel("accountNumber", Seq(dutyDefermentStatementsForEori), false)

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
