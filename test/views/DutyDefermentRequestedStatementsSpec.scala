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
import models.{DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata, DutyDefermentStatementsForEori, EoriHistory}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import play.twirl.api.HtmlFormat
import utils.Utils._
import viewmodels.{DutyDefermentAccountRowContent, DutyDefermentAccountViewModel}
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
        val viewModel: DutyDefermentAccountViewModel = DutyDefermentAccountViewModel(
          accountNumber,
          statementsForAllEoris = Seq.empty,
          isNiAccount = true
        )

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
        val viewModel: DutyDefermentAccountViewModel = DutyDefermentAccountViewModel(
          accountNumber,
          statementsForAllEoris = Seq.empty,
          isNiAccount = false
        )

        val result: HtmlFormat.Appendable = viewModel.component.renderAccountHeading
        val expectedHtml: String = h2_extraContentComponent(
          msg = "cf.account.detail.requested.deferment-account-secondary-heading",
          id = Some("eori-heading"),
          classes = "govuk-caption-xl",
          extraContent = Some(accountNumber)
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "calling renderEoriHeading component" in new Setup {
        val viewModel: DutyDefermentAccountViewModel = DutyDefermentAccountViewModel(
          accountNumber,
          Seq(dutyDefermentStatementsForEori, dutyDefermentStatementsForEori),
          isNiAccount = true
        )

        val result: HtmlFormat.Appendable = viewModel.component.renderEoriHeading(viewModel.statementsData.last)
        val expectedHtml: String = h2Component(
          id = Some("historic-eori-0"),
          classes = "govuk-heading-s",
          msg = msg("cf.account.details.previous-eori", "12345678")
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "calling renderMonthHeading component" in new Setup {
        val viewModel: DutyDefermentAccountViewModel = DutyDefermentAccountViewModel(
          accountNumber,
          Seq(dutyDefermentStatementsForEori),
          isNiAccount = true
        )

        val result: HtmlFormat.Appendable = viewModel.component.renderMonthHeading(viewModel.statementsData.head)
        val expectedHtml: String = h3Component(
          id = Some(s"requested-statements-month-heading-0-2018-2"),
          msg = Formatters.dateAsMonthAndYear(monthAndYear)
        ).toString()

        result.toString mustEqual expectedHtml
      }

      "calling renderStatements component" in new Setup {
        val viewModel: DutyDefermentAccountViewModel = DutyDefermentAccountViewModel(
          accountNumber,
          Seq(dutyDefermentStatementsForEori),
          isNiAccount = true
        )

        val result: HtmlFormat.Appendable = viewModel.component.renderStatements(viewModel.statementsData.head)

        val context: DutyDefermentAccountRowContent = DutyDefermentAccountRowContent(
          viewModel.statementsData.head,
          viewModel.statementsData.head.periodsWithIndex.head._1,
          viewModel.statementsData.head.periodsWithIndex.head._2
        )

        val expectedPeriodDetailsHtml: String = context.period.defermentStatementType match {
          case Supplementary =>
            msg("cf.account.detail.row.supplementary.info")
          case Excise =>
            msg("cf.account.details.row.excise.info")
          case _ =>
            msg("cf.account.detail.period-group")
        }

        val expectedHtml: String = {
          val periodDetailsHtml = dtComponent(
            content = HtmlFormat.raw(expectedPeriodDetailsHtml),
            classes = Some("govuk-summary-list__value"),
            id = Some(s"requested-statements-list-" +
              s"${context.statement.historyIndex}-${context.statement.group.year}-" +
              s"${context.statement.group.month}-row-${context.index}-date-cell"
            )
          ).toString()

          val dutyDefermentFileHtml = ddComponent(
            content = dutyDefermentFileComponent(
              context.period,
              Pdf,
              s"requested-statements-list-" +
                s"${context.statement.historyIndex}-" +
                s"${context.statement.group.year}-${context.statement.group.month}-row-${context.index}"
            ),
            classes = Some("govuk-summary-list__actions"),
            id = Some(s"requested-statements-list-" +
              s"${context.statement.historyIndex}-${context.statement.group.year}-" +
              s"${context.statement.group.month}-row-${context.index}-link-cell")
          ).toString()

          dlComponent(
            content = HtmlFormat.raw(
              divComponent(
                content = HtmlFormat.fill(
                  List(
                    HtmlFormat.raw(periodDetailsHtml),
                    HtmlFormat.raw(dutyDefermentFileHtml)
                  )
                ),
                classes = Some("govuk-summary-list__row"),
                id = Some(s"requested-statements-list-" +
                  s"${context.statement.historyIndex}-${context.statement.group.year}-" +
                  s"${context.statement.group.month}-row-${context.index}")
              ).toString()
            ),
            classes = Some("govuk-summary-list")
          ).toString()
        }

        result.toString mustEqual expectedHtml
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

    private def dutyDefermentModel(isNiAccount: Boolean) = DutyDefermentAccountViewModel(
      "accountNumber",
      Seq(dutyDefermentStatementsForEori),
      isNiAccount = isNiAccount)

    def view(isNiAccount: Boolean = false): Document = Jsoup.parse(
      app.injector.instanceOf[DutyDefermentRequestedStatements].
        apply(dutyDefermentModel(isNiAccount), config.returnLink("dutyDeferment")).body)
  }
}
