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
import models.{SecurityStatementsByPeriod, SecurityStatementsForEori}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import utils.Utils.{h2Component, missingDocumentsGuidanceComponent, spanComponent, spanLinkComponent}

case class StatementRow(historyIndex: Int,
                        eori: Option[String],
                        date: String,
                        pdf: Option[PdfLink],
                        dlRowId: String,
                        rowId: String,
                        dateCellId: String,
                        linkCellId: String,
                        renderEoriHeading: Option[HtmlFormat.Appendable],
                        renderPdfLink: Html)

case class PdfLink(downloadURL: String, formattedSize: String, ariaText: String)

case class SecuritiesRequestedStatementsViewModel(statementRows: Seq[StatementRow],
                                                  hasStatements: Boolean,
                                                  renderMissingDocumentsGuidance: HtmlFormat.Appendable)

object SecuritiesRequestedStatementsViewModel {

  def apply(securityStatementsForEori: Seq[SecurityStatementsForEori])
           (implicit messages: Messages): SecuritiesRequestedStatementsViewModel = {

    val preparedStatementRows = prepareStatementRows(securityStatementsForEori)
    val hasStatements = hasSecurityStatementsForEori(securityStatementsForEori)

    SecuritiesRequestedStatementsViewModel(
      statementRows = preparedStatementRows,
      hasStatements = hasStatements,
      renderMissingDocumentsGuidance = renderMissingDocumentsGuidance
    )
  }

  private def prepareStatementRows(securityStatements: Seq[SecurityStatementsForEori])
                                  (implicit messages: Messages): Seq[StatementRow] = {
    for {
      (statement, statementIndex) <- securityStatements.zipWithIndex
      (requestedStatement, requestedIndex) <- statement.requestedStatements.sorted.zipWithIndex
    } yield createStatementRow(statement, statementIndex, requestedStatement, requestedIndex)
  }

  private def createStatementRow(statement: SecurityStatementsForEori,
                                 statementIndex: Int,
                                 requestedStatement: SecurityStatementsByPeriod,
                                 requestedIndex: Int)
                                (implicit messages: Messages): StatementRow = {

    val eori = if (statementIndex > 0) Some(statement.eoriHistory.eori) else None

    val renderEoriHeading = eori.map { eori =>
      h2Component(
        msg = messages("cf.account.details.previous-eori", eori),
        id = Some(s"requested-statements-eori-heading-$statementIndex"),
        classes = "govuk-heading-s govuk-!-margin-bottom-2"
      )
    }

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
      dlRowId = s"requested-statements-list-$statementIndex",
      rowId = s"requested-statements-list-$statementIndex-row-$requestedIndex",
      dateCellId = s"requested-statements-list-$statementIndex-row-$requestedIndex-date-cell",
      linkCellId = s"requested-statements-list-$statementIndex-row-$requestedIndex-link-cell",
      renderEoriHeading,
      renderPdfLink(pdf)
    )
  }

  private def hasSecurityStatementsForEori(securityStatements: Seq[SecurityStatementsForEori]): Boolean = {
    securityStatements.exists(_.requestedStatements.nonEmpty)
  }

  private def renderPdfLink(pdf: Option[PdfLink])(implicit messages: Messages): Html = {
    pdf.fold(
      spanComponent(
        key = s"${messages("cf.security-statements.no-statements", Pdf)}",
        classes = Some("file-link"),
        visuallyHidden = false
      )
    ) { link =>
      spanLinkComponent(
        msg = s"$Pdf (${link.formattedSize})",
        url = link.downloadURL,
        classes = "file-link govuk-link",
        spanMsg = Some(link.ariaText),
        spanClass = Some("govuk-visually-hidden"),
        download = true
      )
    }
  }

  private def renderMissingDocumentsGuidance(implicit messages: Messages): HtmlFormat.Appendable = {
    missingDocumentsGuidanceComponent("statement")
  }
}
