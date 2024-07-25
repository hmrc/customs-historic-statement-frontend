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

import java.time.LocalDate

class SecuritiesRequestedStatementsViewModelSpec extends Setup {

  when(mockMessages("cf.account.details.previous-eori", "EORI456"))
    .thenReturn("Previous EORI: EORI456")

  "prepareStatementRows" should {
    "return a sequence of StatementRow" in {
      val result = SecuritiesRequestedStatementsViewModel.prepareStatementRows(securityStatements)

      result mustBe Seq(
        StatementRow(
          historyIndex = 0,
          eori = None,
          date = "",
          pdf = None,
          rowId = "requested-statements-list-0-row-0",
          dateCellId = "requested-statements-list-0-row-0-date-cell",
          linkCellId = "requested-statements-list-0-row-0-link-cell"
        )
      )
    }
  }

  "renderEoriHeading" should {
    "render the EORI" in {
      val row = StatementRow(
        0,
        Some("EORI456"),
        "10 July 2023 to 20 July 2023",
        Some(pdfLink),
        "rowId",
        "dateCellId",
        "linkCellId"
      )

      val result = SecuritiesRequestedStatementsViewModel.renderEoriHeading(row)

      result.map(_.body.trim) mustBe Some(
        """<h2 id="requested-statements-eori-heading-0"
          |    class="govuk-heading-s govuk-!-margin-bottom-2">
          |  Previous EORI: EORI456
          |</h2>""".stripMargin.trim)
    }

    "return None when EORI is not present" in {
      val row = StatementRow(
        0,
        None,
        "10 July 2023 to 20 July 2023",
        Some(pdfLink),
        "rowId",
        "dateCellId",
        "linkCellId"
      )

      val result = SecuritiesRequestedStatementsViewModel.renderEoriHeading(row)

      result mustBe None
    }
  }

  "renderPdfLink" should {
    "render the correct PDF link" in {
      val pdf = Some(pdfLink)
      val result = SecuritiesRequestedStatementsViewModel.renderPdfLink(pdf)

      result.body.trim mustBe
        s"""<a class="file-link govuk-link" href="${pdfLink.downloadURL}" download>
           |<span>PDF (${pdfLink.formattedSize})</span>
           |<span class="govuk-visually-hidden">Download PDF</span>
           |</a>""".stripMargin.trim
    }
  }
}

trait Setup extends SpecBase {
  implicit val mockMessages: Messages = mock[Messages]

  val pdfLink: PdfLink = PdfLink("file.pdf", "1MB", "Download PDF")

  val requestedStatement: SecurityStatementsByPeriod = SecurityStatementsByPeriod(
    startDate = LocalDate.parse("2023-07-10"),
    endDate = LocalDate.parse("2023-07-20"),
    files = Seq.empty
  )

  val securityStatementForEori: SecurityStatementsForEori = SecurityStatementsForEori(
    eoriHistory = EoriHistory("EORI456", Some(LocalDate.parse("2023-07-10")), Some(LocalDate.parse("2023-07-20"))),
    currentStatements = Seq(requestedStatement),
    requestedStatements = Seq(requestedStatement)
  )

  val securityStatements: Seq[SecurityStatementsForEori] = Seq(securityStatementForEori)
}
