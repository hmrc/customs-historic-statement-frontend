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
import config.FrontendAppConfig
import models.FileFormat.{Csv, Pdf}
import models.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import scala.jdk.CollectionConverters.ListHasAsScala
import org.mockito.Mockito.when
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
          requestedStatements = Seq(CashStatementMonthToMonth(startDate, endDate))
        )
      )

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
          currentStatements = Seq(CashStatementMonthToMonth(startDate, endDate)),
          requestedStatements = Seq.empty
        )
      )

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
          requestedStatements = Seq(CashStatementMonthToMonth(startDate, endDate))
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
          requestedStatements = Seq.empty
        )
      )

      val viewModel: CashStatementViewModel =
        CashStatementViewModel(statementsForAllEoris = cashStatements)

      val result: Boolean = viewModel.statementsForAllEoris.exists(_.requestedStatements.nonEmpty)

      result mustBe false
    }
  }

  "generateStatementsByYear" should {

    "generate HTML correctly for statements" in new Setup {
      val statements: Seq[CashStatementMonthToMonth] = Seq(CashStatementMonthToMonth(startDate, endDate))
      val groupedStatements: GroupedStatementsByEori = GroupedStatementsByEori(
        eoriIndex = 0,
        eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
        statementsByYear = Map(periodStartYear -> statements)
      )

      val result: Html = CashStatementViewModel.generateStatementsByYear(groupedStatements)

      val expectedYearHeading: HtmlFormat.Appendable = h2Component(
        msg = periodStartYear.toString,
        classes = "govuk-heading-s govuk-!-margin-bottom-0 govuk-!-margin-top-7"
      )

      result.body must include(expectedYearHeading.body)
    }

    "generate HTML correctly for statement rows" in new Setup {
      val statements: Seq[CashStatementMonthToMonth] = Seq(
        CashStatementMonthToMonth(startDate, endDate.minusMonths(1), Seq(csvFile)),
        CashStatementMonthToMonth(startDate, endDate, Seq(csvFile2)),
        CashStatementMonthToMonth(startDate.plusMonths(1), endDate, Seq(pdfFile, csvFile4, pdfFile2)),
        CashStatementMonthToMonth(startDate.plusMonths(1), endDate.plusMonths(2), Seq(csvFile3))
      )

      val groupedStatements: GroupedStatementsByEori = GroupedStatementsByEori(
        eoriIndex = 0,
        eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
        statementsByYear = Map(periodStartYear -> statements)
      )

      val result: Html       = CashStatementViewModel.generateStatementsByYear(groupedStatements)
      val document: Document = Jsoup.parse(result.body)

      private val rowsByMonth               = document.select("div[id^=requested-statements-list-0-row-]")
      private val julyStartMonthRow1        = rowsByMonth.asScala.head
      private val julyStartMonthRow2        = rowsByMonth.asScala(1)
      private val augustStartMonthRow1      = rowsByMonth.asScala(2)
      private val augustStartMonthRow2      = rowsByMonth.asScala(3)
      private val augustStartMonthRow1Links = augustStartMonthRow1.select("a")
      private val csvLinks                  = document.select("a[href$=.csv]")
      private val pdfLinks                  = document.select("a[href$=.pdf]")

      rowsByMonth.size mustBe 4
      julyStartMonthRow1.select("dt").first.ownText mustBe "July to July"
      julyStartMonthRow2.select("dt").first.ownText mustBe "July to August"
      augustStartMonthRow1.select("dt").first.ownText mustBe "August to August"
      augustStartMonthRow2.select("dt").first.ownText mustBe "August to October"
      augustStartMonthRow1Links.eachText.asScala.head mustBe "PDF (2.0MB) Download PDF of August to August (2.0MB)"
      augustStartMonthRow1Links.eachText.asScala(1) mustBe "CSV (1.0MB) Download CSV of August to August (1.0MB)"
      csvLinks.size mustBe 4
      pdfLinks.size mustBe 1
    }

    "only show the link to the latest version of the file if same format and date period" in new Setup {
      val statements: Seq[CashStatementMonthToMonth] = Seq(
        CashStatementMonthToMonth(startDate.plusMonths(1), endDate, Seq(pdfFile, csvFile4, pdfFile2))
      )

      val groupedStatements: GroupedStatementsByEori = GroupedStatementsByEori(
        eoriIndex = 0,
        eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
        statementsByYear = Map(periodStartYear -> statements)
      )

      val result: Html       = CashStatementViewModel.generateStatementsByYear(groupedStatements)
      val document: Document = Jsoup.parse(result.body)
      private val pdfLinks   = document.select("a[href$=.pdf]")

      pdfLinks.size mustBe 1
      pdfLinks.first.attr("href") mustBe pdfFile2.filename
    }

    "generate HTML correctly for multiple years in the grouped statements" in new Setup {
      val statements2023: Seq[CashStatementMonthToMonth] = Seq(CashStatementMonthToMonth(startDate, endDate))
      val statements2024: Seq[CashStatementMonthToMonth] =
        Seq(CashStatementMonthToMonth(startDate.plusYears(1), endDate.plusYears(1)))
      val groupedStatements: GroupedStatementsByEori     = GroupedStatementsByEori(
        eoriIndex = 0,
        eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
        statementsByYear = Map(
          periodStartYear     -> statements2023,
          periodStartYear + 1 -> statements2024
        )
      )

      val result: Html = CashStatementViewModel.generateStatementsByYear(groupedStatements)

      result.body must include(periodStartYear.toString)
      result.body must include((periodStartYear + 1).toString)
    }

    "helpAndSupport" should {

      "generate the correct help and support content" in new Setup {
        when(mockAppConfig.cashAccountForCdsDeclarationsUrl)
          .thenReturn("https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations")

        val result: HtmlFormat.Appendable = CashStatementViewModel.helpAndSupport

        result.body must include(messages("search-transactions-support-message-heading"))
        result.body must include(messages("cf.help-and-support.link.text"))
        result.body must include(messages("cf.help-and-support.link.text.pre"))
        result.body must include(messages("cf.help-and-support.link.text.post"))
        result.body must include(appConfig.cashAccountForCdsDeclarationsUrl)
      }
    }
  }

  trait Setup {
    val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

    val eoriNumber               = "EORI456"
    val startDateJuly: LocalDate = LocalDate.parse("2023-07-10")
    val endDateJuly: LocalDate   = LocalDate.parse("2023-07-20")
    val startDateJune: LocalDate = LocalDate.parse("2023-06-01")
    val endDateJune: LocalDate   = LocalDate.parse("2023-06-30")

    val accountNumber: Option[String] = Some("123456789")
    val monthJuly                     = "July"
    val monthAugust                   = "August"

    val periodStartYear  = 2023
    val periodStartMonth = 7
    val periodStartDay   = 1
    val startDate        = LocalDate.of(periodStartYear, periodStartMonth, periodStartDay)

    val periodEndYear  = 2023
    val periodEndMonth = 8
    val periodEndDay   = 31
    val endDate        = LocalDate.of(periodEndYear, periodEndMonth, periodEndDay)

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
      fileRole = CDSCashAccount,
      cashAccountNumber = accountNumber,
      statementRequestId = None
    )

    val csvFileMetadata2: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = periodStartYear,
      periodStartMonth = periodStartMonth,
      periodStartDay = periodStartDay,
      periodEndYear = periodEndYear,
      periodEndMonth = periodEndMonth,
      periodEndDay = periodEndDay,
      fileFormat = Csv,
      fileRole = CDSCashAccount,
      cashAccountNumber = accountNumber,
      statementRequestId = None
    )

    val csvFileMetadata3: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = periodStartYear,
      periodStartMonth = periodStartMonth + 1,
      periodStartDay = periodStartDay,
      periodEndYear = periodEndYear,
      periodEndMonth = periodEndMonth + 2,
      periodEndDay = periodEndDay,
      fileFormat = Csv,
      fileRole = CDSCashAccount,
      cashAccountNumber = accountNumber,
      statementRequestId = None
    )

    val csvFileMetadata4: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = periodStartYear,
      periodStartMonth = periodStartMonth + 1,
      periodStartDay = periodStartDay,
      periodEndYear = periodEndYear,
      periodEndMonth = periodEndMonth,
      periodEndDay = periodEndDay,
      fileFormat = Csv,
      fileRole = CDSCashAccount,
      cashAccountNumber = accountNumber,
      statementRequestId = None
    )

    val pdfFileMetadata: CashStatementFileMetadata = CashStatementFileMetadata(
      periodStartYear = periodStartYear,
      periodStartMonth = periodStartMonth + 1,
      periodStartDay = periodStartDay,
      periodEndYear = periodEndYear,
      periodEndMonth = periodEndMonth,
      periodEndDay = periodEndDay,
      fileFormat = Pdf,
      fileRole = CDSCashAccount,
      cashAccountNumber = accountNumber,
      statementRequestId = None
    )

    val csvFile: CashStatementFile = CashStatementFile(
      filename = "file0.csv",
      downloadURL = "file0.csv",
      size = expectedFileSizeCsv,
      metadata = csvFileMetadata
    )

    val csvFile2: CashStatementFile = CashStatementFile(
      filename = "file0.csv",
      downloadURL = "file0.csv",
      size = expectedFileSizeCsv,
      metadata = csvFileMetadata2
    )

    val csvFile3: CashStatementFile = CashStatementFile(
      filename = "file0.csv",
      downloadURL = "file0.csv",
      size = expectedFileSizeCsv,
      metadata = csvFileMetadata3
    )

    val csvFile4: CashStatementFile = CashStatementFile(
      filename = "file0.csv",
      downloadURL = "file0.csv",
      size = expectedFileSizeCsv,
      metadata = csvFileMetadata4
    )

    val pdfFile: CashStatementFile = CashStatementFile(
      filename = "file0.pdf",
      downloadURL = "file0.pdf",
      size = expectedFileSizePdf,
      metadata = pdfFileMetadata
    )

    val pdfFile2: CashStatementFile = CashStatementFile(
      filename = "file1.pdf",
      downloadURL = "file1.pdf",
      size = expectedFileSizePdf,
      metadata = pdfFileMetadata
    )
  }
}
