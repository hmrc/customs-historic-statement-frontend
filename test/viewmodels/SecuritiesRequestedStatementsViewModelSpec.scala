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

package viewmodels

import base.SpecBase
import models.FileFormat.Pdf
import play.api.i18n.Messages
import models.{EoriHistory, SecurityStatementsByPeriod, SecurityStatementsForEori}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Application
import play.twirl.api.HtmlFormat
import utils.Utils.{emptyString, h2Component, spanComponent, spanLinkComponent}
import java.time.LocalDate

class SecuritiesRequestedStatementsViewModelSpec extends SpecBaseWithSetup {

  "prepareStatementRows" should {
    "return a sequence of StatementRow" in {
      val result = viewModel.statementRows

      result.size mustBe 1
      val row = result.head

      row.historyIndex mustBe 0
      row.eori mustBe None
      row.date mustBe "10 July 2023 to 20 July 2023"
      row.pdf mustBe None
      row.rowId mustBe "requested-statements-list-0-row-0"
      row.dateCellId mustBe "requested-statements-list-0-row-0-date-cell"
      row.linkCellId mustBe "requested-statements-list-0-row-0-link-cell"
      row.renderEoriHeading mustBe None
    }

    "return a sequence of StatementRow with correct data" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        ),
        SecurityStatementsForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJune), Some(endDateJune)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        )
      )

      val viewModel = createViewModel(securityStatements)
      val result    = viewModel.statementRows

      result.size mustBe 2
      result.head.eori mustBe None
      result(1).eori mustBe Some(eoriNumber)
    }
  }

  "renderEoriHeading" should {
    "render the EORI heading correctly" in {
      val mockStatementRow = StatementRow(
        historyIndex = 0,
        eori = Some(eoriNumber),
        date = emptyString,
        pdf = None,
        dlRowId = emptyString,
        rowId = emptyString,
        dateCellId = emptyString,
        linkCellId = emptyString,
        renderEoriHeading = Some(
          h2Component(
            msg = messages("cf.account.details.previous-eori", eoriNumber),
            id = Some("requested-statements-eori-heading-0"),
            classes = "govuk-heading-s govuk-!-margin-bottom-2"
          )
        ),
        renderPdfLink = HtmlFormat.empty
      )

      val viewModel = SecuritiesRequestedStatementsViewModel(
        statementRows = Seq(mockStatementRow),
        hasStatements = true,
        renderMissingDocumentsGuidance = HtmlFormat.empty
      )

      val expectedHtml = h2Component(
        msg = messages("cf.account.details.previous-eori", eoriNumber),
        id = Some("requested-statements-eori-heading-0"),
        classes = "govuk-heading-s govuk-!-margin-bottom-2"
      )

      viewModel.statementRows.head.renderEoriHeading mustBe Some(expectedHtml)
    }

    "return false if EORI is not present" in {
      val viewModel = createViewModel(Seq(emptySecurityStatementForEori))
      val result    = viewModel.statementRows

      val isEoriPresent = result.nonEmpty

      isEoriPresent mustBe false
    }
  }

  "renderPdfLink" should {
    "render the PDF link correctly" in {
      val expectedHtml = spanLinkComponent(
        msg = s"PDF (${pdfLink.formattedSize})",
        url = pdfLink.downloadURL,
        classes = "file-link govuk-link",
        spanClass = Some("govuk-visually-hidden"),
        spanMsg = Some(pdfLink.ariaText)
      )

      val mockStatementRow = StatementRow(
        historyIndex = 0,
        eori = None,
        date = emptyString,
        pdf = Some(pdfLink),
        dlRowId = emptyString,
        rowId = emptyString,
        dateCellId = emptyString,
        linkCellId = emptyString,
        renderEoriHeading = None,
        renderPdfLink = expectedHtml
      )

      val viewModel = SecuritiesRequestedStatementsViewModel(
        statementRows = Seq(mockStatementRow),
        hasStatements = true,
        renderMissingDocumentsGuidance = HtmlFormat.empty
      )

      viewModel.statementRows.head.renderPdfLink mustBe expectedHtml
    }

    "render the correct PDF title when no statements" in {
      val expectedHtml = spanComponent(
        key = s"${messages("cf.security-statements.no-statements", Pdf)}",
        classes = Some("file-link"),
        visuallyHidden = false
      )

      val mockStatementRow = StatementRow(
        historyIndex = 0,
        eori = None,
        date = emptyString,
        pdf = None,
        dlRowId = emptyString,
        rowId = emptyString,
        dateCellId = emptyString,
        linkCellId = emptyString,
        renderEoriHeading = None,
        renderPdfLink = expectedHtml
      )

      val viewModel = SecuritiesRequestedStatementsViewModel(
        statementRows = Seq(mockStatementRow),
        hasStatements = false,
        renderMissingDocumentsGuidance = HtmlFormat.empty
      )

      viewModel.statementRows.head.renderPdfLink mustBe expectedHtml
    }
  }

  "renderMissingDocumentsGuidance" should {
    "generate guidance" in {
      val result = viewModel.renderMissingDocumentsGuidance

      val document: Document = Jsoup.parse(result.body)

      val heading: Elements = document.select("#missing-documents-guidance-heading")

      heading.size() mustBe 1
    }
  }

  "hasSecurityStatementsForEori" should {
    "return true if there are requested statements" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        )
      )

      val viewModel = createViewModel(securityStatements)
      val result    = viewModel.hasStatements

      result mustBe true
    }

    "return false if there are no requested statements" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq.empty
        )
      )

      val viewModel = createViewModel(securityStatements)
      val result    = viewModel.hasStatements

      result mustBe false
    }
  }
}

trait SpecBaseWithSetup extends SpecBase {
  val pdfLink: PdfLink = PdfLink("file.pdf", "1MB", "Download PDF")
  val eoriNumber       = "EORI456"

  val startDateJuly: LocalDate = LocalDate.parse("2023-07-10")
  val endDateJuly: LocalDate   = LocalDate.parse("2023-07-20")
  val startDateJune: LocalDate = LocalDate.parse("2023-06-01")
  val endDateJune: LocalDate   = LocalDate.parse("2023-06-30")

  val requestedStatement: SecurityStatementsByPeriod = SecurityStatementsByPeriod(
    startDate = startDateJuly,
    endDate = endDateJuly,
    files = Seq.empty
  )

  val securityStatementForEori: SecurityStatementsForEori = SecurityStatementsForEori(
    eoriHistory = EoriHistory(eoriNumber, Some(startDateJuly), Some(endDateJuly)),
    currentStatements = Seq(requestedStatement),
    requestedStatements = Seq(requestedStatement)
  )

  val emptySecurityStatementForEori: SecurityStatementsForEori = SecurityStatementsForEori(
    eoriHistory = EoriHistory(emptyString, None, None),
    currentStatements = Seq.empty,
    requestedStatements = Seq.empty
  )

  val securityStatements: Seq[SecurityStatementsForEori] = Seq(securityStatementForEori)
  val viewModel: SecuritiesRequestedStatementsViewModel  = createViewModel(securityStatements)

  protected def createViewModel(
    securityStatements: Seq[SecurityStatementsForEori]
  ): SecuritiesRequestedStatementsViewModel =
    SecuritiesRequestedStatementsViewModel(securityStatements)

}
