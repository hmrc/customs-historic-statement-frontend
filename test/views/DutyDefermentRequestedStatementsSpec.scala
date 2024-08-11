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

import helpers.Formatters
import models.DDStatementType.{Excise, Supplementary, Weekly}
import models.FileFormat.Pdf
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import utils.Utils._
import viewmodels.{DutyDefermentAccountRowContent, DutyDefermentAccountStatement, DutyDefermentAccountViewModel}
import views.html.DutyDefermentRequestedStatements

import java.time.LocalDate

class DutyDefermentRequestedStatementsSpec extends ViewTestHelper {
  "view" should {
    "display correct title and contents" when {
      "account is of Northern Ireland" in new Setup {

        implicit val viewDoc: Document = view(isNiAccount = true)

        titleShouldBeCorrect(viewDoc, "cf.account.detail.requested.title")
        pageShouldContainBackLinkUrl(viewDoc, config.returnLink("dutyDeferment"))
        shouldContainNiAccountNumber(viewDoc)
        headingShouldBeCorrect(viewDoc)
        subHeadingShouldBeCorrect(viewDoc)
        eoriNumberShouldBeCorrect(viewDoc)
      }

      "account is not of Northern Ireland" in new Setup {

        implicit val viewDoc: Document = view()

        titleShouldBeCorrect(viewDoc, "cf.account.detail.requested.title")
        pageShouldContainBackLinkUrl(viewDoc, config.returnLink("dutyDeferment"))
        shouldContainAccountNumber(viewDoc)
        headingShouldBeCorrect(viewDoc)
        subHeadingShouldBeCorrect(viewDoc)
        eoriNumberShouldBeCorrect(viewDoc)
      }
    }
  }

  "DutyDefermentAccountComponents" should {
    "display correct content" when {
      "calling renderNiAccountHeading component for Northern Ireland accounts" in new Setup {

        val result: HtmlFormat.Appendable = viewModel.component.renderAccountHeading

        val expectedHtml: String = h2_extraContentComponent(
          msg = "cf.account.detail.requested.deferment-account-secondary-heading.NiAccount",
          id = Some("eori-heading"),
          classes = "govuk-caption-xl",
          extraContent = Some(accountNumber)
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "calling renderNiAccountHeading component for Non-Northern Ireland accounts" in new Setup {

        val viewModelNiFalse: DutyDefermentAccountViewModel = DutyDefermentAccountViewModel(
          accountNumber,
          statementsForAllEoris = Seq.empty,
          isNiAccount = false
        )

        val result: HtmlFormat.Appendable = viewModelNiFalse.component.renderAccountHeading
        val expectedHtml: String = h2_extraContentComponent(
          msg = "cf.account.detail.requested.deferment-account-secondary-heading",
          id = Some("eori-heading"),
          classes = "govuk-caption-xl",
          extraContent = Some(accountNumber)
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "calling renderEoriHeading component" in new Setup {

        val result: HtmlFormat.Appendable = viewModel.component.renderEoriHeading(viewModel.statementsData.last)

        val expectedHtml: String = h2Component(
          id = Some("historic-eori-0"),
          classes = "govuk-heading-s",
          msg = msg("cf.account.details.previous-eori", "12345678")
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "calling renderMonthHeading component" in new Setup {

        val result: HtmlFormat.Appendable = viewModel.component.renderMonthHeading(viewModel.statementsData.head)

        val expectedHtml: String = h3Component(
          id = Some(s"requested-statements-month-heading-0-2018-2"),
          msg = Formatters.dateAsMonthAndYear(monthAndYear)
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "calling renderStatements component" in new Setup {

        val result: HtmlFormat.Appendable = viewModel.component.renderStatements(statement)

        val expectedPeriodDetailsHtml: String = statementRowContent.period.defermentStatementType match {
          case Supplementary => msg("cf.account.detail.row.supplementary.info")
          case Excise => msg("cf.account.details.row.excise.info")
          case _ => msg("cf.account.detail.period-group")
        }

        val rowId = s"${statement.historyIndex}-${statement.group.year}-${statement.group.month}-row-$statementIndex"

        val expectedHtml: String = dlComponent(
          content = HtmlFormat.raw(
            divComponent(
              content = HtmlFormat.fill(List(
                dtComponent(
                  content = HtmlFormat.raw(expectedPeriodDetailsHtml),
                  classes = Some("govuk-summary-list__value"),
                  id = Some(s"requested-statements-list-$rowId-date-cell")
                ),
                ddComponent(
                  content = dutyDefermentFileComponent(statementPeriod, Pdf, s"requested-statements-list-$rowId"),
                  classes = Some("govuk-summary-list__actions"),
                  id = Some(s"requested-statements-list-$rowId-link-cell")
                )
              )),
              classes = Some("govuk-summary-list__row"),
              id = Some(s"requested-statements-list-$rowId")
            ).toString()
          ),
          classes = Some("govuk-summary-list")
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "handle different statement types in preparePeriodDetails" in new Setup {

        val renderStatement: DutyDefermentAccountStatement => HtmlFormat.Appendable =
          viewModel.component.renderStatements

        val supplementaryPeriod: DutyDefermentStatementPeriod =
          basePeriod.copy(fileRole = C79Certificate, defermentStatementType = Supplementary)

        val supplementaryResult: HtmlFormat.Appendable = renderStatement(createTestStatement(supplementaryPeriod))
        val supplementaryMessage: String = msg("cf.account.detail.row.supplementary.info")
        supplementaryResult.body must include(supplementaryMessage)

        val excisePeriod: DutyDefermentStatementPeriod = basePeriod.copy(defermentStatementType = Excise)
        val exciseResult: HtmlFormat.Appendable = renderStatement(createTestStatement(excisePeriod))
        val exciseMessage: String = msg("cf.account.details.row.excise.info")
        exciseResult.body must include(exciseMessage)

        val result: HtmlFormat.Appendable = renderStatement(createTestStatement(basePeriod))
        val expectedMessage: String = msg("cf.account.detail.period-group",
          Formatters.dateAsDay(basePeriod.startDate),
          Formatters.dateAsDay(basePeriod.endDate),
          Formatters.dateAsMonth(basePeriod.endDate))
        result.body must include(expectedMessage)
      }
    }
  }

  private def shouldContainAccountNumber(implicit view: Document): Assertion = {
    view.getElementById("eori-heading").html().contains(
      msg("cf.account.detail.requested.deferment-account-secondary-heading")
    ) mustBe true
  }

  private def shouldContainNiAccountNumber(implicit view: Document): Assertion = {
    view.getElementById("eori-heading").html().contains(
      msg("cf.account.detail.requested.deferment-account-secondary-heading.NiAccount")
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

    protected val accountNumber = "123456"
    protected val monthAndYear: LocalDate = LocalDate.of(periodStartYear, periodStartMonth2, periodStartDay)

    private val dutyDefermentFile: DutyDefermentStatementFile =
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

    private val dutyDefermentFile_2: DutyDefermentStatementFile = DutyDefermentStatementFile(pdfFileName,
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

    protected val dutyDefermentStatementsForEori: DutyDefermentStatementsForEori =
      DutyDefermentStatementsForEori.apply(eoriHistory, Seq(dutyDefermentFile), Seq(dutyDefermentFile_2))

    protected val viewModel: DutyDefermentAccountViewModel = DutyDefermentAccountViewModel(
      accountNumber,
      Seq(dutyDefermentStatementsForEori),
      isNiAccount = true
    )

    val basePeriod: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      fileRole = DutyDefermentStatement,
      defermentStatementType = Weekly,
      monthAndYear = monthAndYear,
      startDate = monthAndYear,
      endDate = monthAndYear,
      statementFiles = Seq.empty
    )

    protected val statement: DutyDefermentAccountStatement = viewModel.statementsData.head
    protected val statementPeriod: DutyDefermentStatementPeriod = statement.periodsWithIndex.head._1
    protected val statementIndex: Int = statement.periodsWithIndex.head._2

    protected val statementRowContent: DutyDefermentAccountRowContent =
      DutyDefermentAccountRowContent(statement, statementPeriod, statementIndex)

    private def dutyDefermentModel(isNiAccount: Boolean) = DutyDefermentAccountViewModel(
      "accountNumber",
      Seq(dutyDefermentStatementsForEori),
      isNiAccount = isNiAccount)

    protected def view(isNiAccount: Boolean = false): Document = Jsoup.parse(
      app.injector.instanceOf[DutyDefermentRequestedStatements].
        apply(dutyDefermentModel(isNiAccount), config.returnLink("dutyDeferment")).body)

    protected def createTestStatement(period: DutyDefermentStatementPeriod): DutyDefermentAccountStatement = {
      DutyDefermentAccountStatement(
        historyIndex = 0,
        groupIndex = 0,
        eorisStatements = Seq.empty,
        group = DutyDefermentStatementPeriodsByMonth(
          monthAndYear = period.monthAndYear,
          periods = Seq(period)
        ),
        periodIndex = 0,
        period = period,
        periodsWithIndex = Seq((period, 0))
      )
    }
  }
}
