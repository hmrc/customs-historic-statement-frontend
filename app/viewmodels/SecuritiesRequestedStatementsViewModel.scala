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

import helpers.Formatters.dateAsDayMonthAndYear
import models.FileFormat.Pdf
import models.SecurityStatementsForEori
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}

case class StatementRow(historyIndex: Int,
                        eori: Option[String],
                        date: String,
                        pdf: Option[PdfLink],
                        rowId: String,
                        dateCellId: String,
                        linkCellId: String)

case class PdfLink(downloadURL: String, formattedSize: String, ariaText: String)

object SecuritiesRequestedStatementsViewModel {
  def prepareStatementRows(securityStatements: Seq[SecurityStatementsForEori])
                          (implicit messages: Messages): Seq[StatementRow] = {

    securityStatements.zipWithIndex.flatMap { case (statement, statementIndex) =>
      statement.requestedStatements.sorted.zipWithIndex.map { case (requestedStatement, requestedIndex) =>

        val eori = if (statementIndex > 0) Some(statement.eoriHistory.eori) else None

        val date = messages("cf.security-statements.requested.period",
          dateAsDayMonthAndYear(requestedStatement.startDate),
          dateAsDayMonthAndYear(requestedStatement.endDate))

        val pdf = requestedStatement.pdf.map { pdf =>
          PdfLink(
            downloadURL = pdf.downloadURL,
            formattedSize = pdf.formattedSize,
            ariaText = messages(
              "cf.security-statements.requested.download-link.aria-text",
              Pdf,
              dateAsDayMonthAndYear(requestedStatement.startDate),
              dateAsDayMonthAndYear(requestedStatement.endDate),
              pdf.formattedSize
            )
          )
        }

        StatementRow(
          historyIndex = statementIndex,
          eori,
          date,
          pdf,
          rowId = s"requested-statements-list-$statementIndex-row-$requestedIndex",
          dateCellId = s"requested-statements-list-$statementIndex-row-$requestedIndex-date-cell",
          linkCellId = s"requested-statements-list-$statementIndex-row-$requestedIndex-link-cell"
        )
      }
    }
  }

  def renderEoriHeading(row: StatementRow)(implicit messages: Messages): Option[HtmlFormat.Appendable] = {
    row.eori.map { eori =>
      Html(
        s"""<h2 id="requested-statements-eori-heading-${row.historyIndex}"
           |    class="govuk-heading-s govuk-!-margin-bottom-2">
           |  ${messages("cf.account.details.previous-eori", eori)}
           |</h2>""".stripMargin)
    }
  }

  def renderPdfLink(pdf: Option[PdfLink])(implicit messages: Messages): HtmlFormat.Appendable = {
    pdf.fold(
      Html(s"""<span class="file-link">${messages("cf.security-statements.no-statements", Pdf)}</span>""")
    ) { link =>
      Html(
        s"""<a class="file-link govuk-link" href="${link.downloadURL}" download>
           |<span>$Pdf (${link.formattedSize})</span>
           |<span class="govuk-visually-hidden">${link.ariaText}</span>
           |</a>""".stripMargin)
    }
  }
}
