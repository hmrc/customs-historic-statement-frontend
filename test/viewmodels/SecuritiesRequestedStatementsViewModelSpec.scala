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
import play.twirl.api.HtmlFormat
import utils.Utils.{h2Component, spanLinkComponent}

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

    "return a sequence of StatementRow with correct data" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory("EORI456", Some(LocalDate.parse("2023-07-10")), Some(LocalDate.parse("2023-07-20"))),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        ),
        SecurityStatementsForEori(
          eoriHistory = EoriHistory("EORI789", Some(LocalDate.parse("2023-06-01")), Some(LocalDate.parse("2023-06-30"))),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        )
      )

      val result = SecuritiesRequestedStatementsViewModel.prepareStatementRows(securityStatements)

      result.size mustBe 2
      result.head.eori mustBe None
      result(1).eori mustBe Some("EORI789")
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

      val expectedHtml = h2Component(
        msg = "cf.account.details.previous-eori",
        id = Some("requested-statements-eori-heading-0"),
        classes = "govuk-heading-s govuk-!-margin-bottom-2"
      ).body.trim

      result.map(_.body.trim.replaceAll("\\s+", " ")) mustBe Some(expectedHtml.replaceAll("\\s+", " "))
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

      val expectedHtml = spanLinkComponent(
        msg = s"PDF (${pdfLink.formattedSize})",
        url = pdfLink.downloadURL,
        classes = "file-link govuk-link",
        spanClass = Some("govuk-visually-hidden"),
        spanMsg = Some("Download PDF")
      ).body.trim

      result.body.trim.replaceAll("\\s+", " ") mustBe expectedHtml.replaceAll("\\s+", " ")
    }
  }

  "renderMissingDocumentsGuidance" should {
    "generate guidance" in {
      val result: HtmlFormat.Appendable = SecuritiesRequestedStatementsViewModel.renderMissingDocumentsGuidance

      val document: Document = Jsoup.parse(result.body)

      val heading: Elements = document.select("#missing-documents-guidance-heading")

      heading.size() mustBe 1
    }
  }

  "hasSecurityStatementsForEori" should {
    "return true if there are requested statements" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory("EORI456", Some(LocalDate.parse("2023-07-10")), Some(LocalDate.parse("2023-07-20"))),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq(requestedStatement)
        )
      )

      val result = SecuritiesRequestedStatementsViewModel.hasSecurityStatementsForEori(securityStatements)

      result mustBe true
    }

    "return false if there are no requested statements" in {
      val securityStatements = Seq(
        SecurityStatementsForEori(
          eoriHistory = EoriHistory("EORI456", Some(LocalDate.parse("2023-07-10")), Some(LocalDate.parse("2023-07-20"))),
          currentStatements = Seq(requestedStatement),
          requestedStatements = Seq.empty
        )
      )

      val result = SecuritiesRequestedStatementsViewModel.hasSecurityStatementsForEori(securityStatements)

      result mustBe false
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
