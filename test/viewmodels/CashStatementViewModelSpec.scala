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

package viewmodels

import base.SpecBase
import models.FileFormat.{Csv, Pdf}
import models.*
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import utils.Utils.h2Component

import java.time.LocalDate

class CashStatementViewModelSpec extends SpecBase {

  "getRequestedStatementsGroupedByYear" should {

    "return grouped statements by year when exist" in new Setup {
      val cashStatements: Seq[CashStatementForEori] = Seq(
        CashStatementForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq.empty,
          requestedStatements = Seq(CashStatementByMonth(startDateJuly))))

      val viewModel: CashStatementViewModel =
        CashStatementViewModel(statementsForAllEoris = cashStatements)

      val result: Seq[GroupedStatementsByEori] = CashStatementViewModel.getRequestedStatementsGroupedByYear(viewModel)

      result.size mustBe 1
      result.head.statementsByYear.size mustBe 1
      result.head.statementsByYear.head._1 mustBe periodStartYear
      result.head.statementsByYear.head._2.head.formattedMonthYear mustBe s"$monthJuly $periodStartYear"
    }

    "return an empty sequence when no requested statements exist" in new Setup {
      val cashStatements: Seq[CashStatementForEori] = Seq(
        CashStatementForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq(CashStatementByMonth(startDateJuly)),
          requestedStatements = Seq.empty))

      val viewModel: CashStatementViewModel =
        CashStatementViewModel(statementsForAllEoris = cashStatements)

      val result: Seq[GroupedStatementsByEori] = CashStatementViewModel.getRequestedStatementsGroupedByYear(viewModel)

      result mustBe empty
    }
  }

  "hasRequestedStatements" should {

    "return true if there are requested statements" in new Setup {
      val cashStatements: Seq[CashStatementForEori] = Seq(
        CashStatementForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq.empty,
          requestedStatements = Seq(CashStatementByMonth(startDateJuly))
        )
      )

      val viewModel: CashStatementViewModel =
        CashStatementViewModel(statementsForAllEoris = cashStatements)

      val result: Boolean = viewModel.statementsForAllEoris.exists(_.requestedStatements.nonEmpty)

      result mustBe true
    }

    "return false if there are no requested statements" in new Setup {
      val cashStatements: Seq[CashStatementForEori] = Seq(
        CashStatementForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq.empty,
          requestedStatements = Seq.empty))

      val viewModel: CashStatementViewModel =
        CashStatementViewModel(statementsForAllEoris = cashStatements)

      val result: Boolean = viewModel.statementsForAllEoris.exists(_.requestedStatements.nonEmpty)

      result mustBe false
    }
  }

  "generateStatementsByYear" should {

    "generate HTML correctly for statements" in new Setup {
      val statements: Seq[CashStatementByMonth] = Seq(CashStatementByMonth(startDateJuly))
      val groupedStatements: GroupedStatementsByEori = GroupedStatementsByEori(
        eoriIndex = 0,
        eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
        statementsByYear = Map(periodStartYear -> statements))

      val result: Html = CashStatementViewModel.generateStatementsByYear(groupedStatements)

      val expectedYearHeading: HtmlFormat.Appendable = h2Component(
        msg = periodStartYear.toString,
        classes = "govuk-heading-s govuk-!-margin-bottom-0 govuk-!-margin-top-7")

      result.body must include(expectedYearHeading.body)
    }

    "generate HTML correctly for statement rows" in new Setup {
      val statements: Seq[CashStatementByMonth] = Seq(
        CashStatementByMonth(startDateJuly, Seq(csvFile)),
        CashStatementByMonth(startDateJuly.plusMonths(1), Seq(pdfFile)))

      val groupedStatements: GroupedStatementsByEori = GroupedStatementsByEori(
        eoriIndex = 0,
        eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
        statementsByYear = Map(periodStartYear -> statements))

      val result: Html = CashStatementViewModel.generateStatementsByYear(groupedStatements)

      result.body must include(monthJuly)
      result.body must include(monthAugust)
      result.body must include("file.csv")
      result.body must include("file.pdf")
      result.body must include("Download PDF")
      result.body must include("Download CSV")
    }

    "generate HTML correctly for multiple years in the grouped statements" in new Setup {
      val statements2023: Seq[CashStatementByMonth] = Seq(CashStatementByMonth(startDateJuly))
      val statements2024: Seq[CashStatementByMonth] = Seq(CashStatementByMonth(startDateJuly.plusYears(1)))
      val groupedStatements: GroupedStatementsByEori = GroupedStatementsByEori(
        eoriIndex = 0,
        eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
        statementsByYear = Map(
          periodStartYear -> statements2023,
          periodStartYear + 1 -> statements2024
        )
      )

      val result: Html = CashStatementViewModel.generateStatementsByYear(groupedStatements)

      result.body must include(periodStartYear.toString)
      result.body must include((periodStartYear + 1).toString)

      result.body must include(monthJuly)
    }
  }

  trait Setup {

    val app: Application = applicationBuilder().build()
    implicit val msg: Messages = messages(app)

    val eoriNumber = "EORI456"
    val startDateJuly: LocalDate = LocalDate.parse("2023-07-10")
    val endDateJuly: LocalDate = LocalDate.parse("2023-07-20")
    val startDateJune: LocalDate = LocalDate.parse("2023-06-01")
    val endDateJune: LocalDate = LocalDate.parse("2023-06-30")

    val accountNumber: Option[String] = Some("123456789")
    val monthJuly = "July"
    val monthAugust = "August"

    val periodStartYear = 2023
    val periodStartMonth = 7
    val periodStartDay = 1

    val periodEndYear = 2023
    val periodEndMonth = 8
    val periodEndDay = 31

    val expectedFileSizeCsv = 1048576L
    val expectedFileSizePdf = 2097152L

    val csvFileMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = periodStartYear,
      periodStartMonth = periodStartMonth,
      periodStartDay = periodStartDay,
      periodEndYear = periodEndYear,
      periodEndMonth = periodEndMonth - 1,
      periodEndDay = periodEndDay,
      fileFormat = Csv,
      fileRole = CashStatement,
      cashAccountNumber = accountNumber,
      statementRequestId = None)

    val pdfFileMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = periodStartYear,
      periodStartMonth = periodStartMonth + 1,
      periodStartDay = periodStartDay,
      periodEndYear = periodEndYear,
      periodEndMonth = periodEndMonth,
      periodEndDay = periodEndDay,
      fileFormat = Pdf,
      fileRole = CashStatement,
      cashAccountNumber = accountNumber,
      statementRequestId = None)

    val csvFile: CashStatementFile = CashStatementFile(
      filename = "file.csv",
      downloadURL = "file.csv",
      size = expectedFileSizeCsv,
      metadata = csvFileMetadata)

    val pdfFile: CashStatementFile = CashStatementFile(
      filename = "file.pdf",
      downloadURL = "file.pdf",
      size = expectedFileSizePdf,
      metadata = pdfFileMetadata)
  }
}
