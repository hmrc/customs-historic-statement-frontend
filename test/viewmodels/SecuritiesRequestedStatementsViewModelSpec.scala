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
import play.api.i18n.Messages
import models.{EoriHistory, SecurityStatementsByPeriod, SecurityStatementsForEori}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Application
import utils.Utils.{emptyString, spanComponent}

import java.time.LocalDate

class SecuritiesRequestedStatementsViewModelSpec extends SpecBaseWithSetup {

  "prepareStatementRows" should {
    "return a sequence of StatementRow" in {
      val viewModel = createViewModel(securityStatements)
      val result = viewModel.statementRows

      result mustBe Seq(
        StatementRow(
          historyIndex = 0,
          eori = None,
          date = "10 July 2023 to 20 July 2023",
          pdf = None,
          rowId = "requested-statements-list-0-row-0",
          dateCellId = "requested-statements-list-0-row-0-date-cell",
          linkCellId = "requested-statements-list-0-row-0-link-cell"
        )
      )
    }

    "return a sequence of StatementRow with correct data" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory("EORI456", Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        ),
        SecurityStatementsForEori(
          eoriHistory = EoriHistory("EORI789", Some(startDateJune), Some(endDateJune)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        )
      )

      val viewModel = createViewModel(securityStatements)
      val result = viewModel.statementRows

      result.size mustBe 2
      result.head.eori mustBe None
      result(1).eori mustBe Some("EORI789")
    }
  }

  "renderEoriHeading" should {
    "render the EORI" in {
      val row = StatementRow(
        0,
        Some(securityStatementForEori.eoriHistory.eori),
        "10 July 2023 to 20 July 2023",
        Some(pdfLink),
        "rowId",
        "dateCellId",
        "linkCellId"
      )

      val result = row.eori

      result mustBe Some(securityStatementForEori.eoriHistory.eori)

      val expectedSize = securityStatementForEori.eoriHistory.eori.length
      val actualSize = result.map(_.length)

      actualSize mustBe Some(expectedSize)
    }

    "return false if EORI is not present" in {
      val viewModel = createViewModel(Seq(emptySecurityStatementForEori))
      val result = viewModel.statementRows

      val isEoriPresent = result.nonEmpty

      isEoriPresent mustBe false
    }
  }

  "renderPdfLink" should {
    "return true if PDF size is 1 and true" in {
      val viewModel = createViewModel(securityStatements)
      val pdfLink = viewModel.renderPdfLink

      val isSizeOne = pdfLink.size == 1

      isSizeOne mustBe true
    }

    "render the correct PDF title when no statements" in {
      val viewModel = createViewModel(securityStatements)
      val result = viewModel.renderPdfLink

      result mustBe Some(spanComponent("There are no statements available to view.", classes = Some(emptyString), visuallyHidden = false))
    }
  }

  "renderMissingDocumentsGuidance" should {
    "generate guidance" in {
      val viewModel = createViewModel(securityStatements)
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
          eoriHistory = EoriHistory("EORI456", Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        )
      )

      val viewModel = createViewModel(securityStatements)
      val result = viewModel.hasStatements

      result mustBe true
    }

    "return false if there are no requested statements" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory("EORI456", Some(startDateJuly), Some(endDateJuly)),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq.empty
        )
      )

      val viewModel = createViewModel(securityStatements)
      val result = viewModel.hasStatements

      result mustBe false
    }
  }

  private def createViewModel(securityStatements: Seq[SecurityStatementsForEori]) = {
    SecuritiesRequestedStatementsViewModel(securityStatements)
  }
}

trait SpecBaseWithSetup extends SpecBase {

  val app: Application = applicationBuilder().build()
  implicit val messages: Messages = messages(app)

  val pdfLink: PdfLink = PdfLink("file.pdf", "1MB", "Download PDF")

  val startDateJuly: LocalDate = LocalDate.parse("2023-07-10")
  val endDateJuly: LocalDate = LocalDate.parse("2023-07-20")
  val startDateJune: LocalDate = LocalDate.parse("2023-06-01")
  val endDateJune: LocalDate = LocalDate.parse("2023-06-30")

  val requestedStatement: SecurityStatementsByPeriod = SecurityStatementsByPeriod(
    startDate = startDateJuly,
    endDate = endDateJuly,
    files = Seq.empty
  )

  val securityStatementForEori: SecurityStatementsForEori = SecurityStatementsForEori(
    eoriHistory = EoriHistory("EORI456", Some(startDateJuly), Some(endDateJuly)),
    currentStatements = Seq(requestedStatement),
    requestedStatements = Seq(requestedStatement)
  )

  val emptySecurityStatementForEori: SecurityStatementsForEori = SecurityStatementsForEori(
    eoriHistory = EoriHistory(emptyString, None, None),
    currentStatements = Seq.empty,
    requestedStatements = Seq.empty
  )

  val securityStatements: Seq[SecurityStatementsForEori] = Seq(securityStatementForEori)

  when(messages("cf.account.details.previous-eori", "EORI456"))
    .thenReturn("Previous EORI: EORI456")
}
