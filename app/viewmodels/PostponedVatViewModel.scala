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

import controllers.OrderedByEoriHistory
import helpers.Formatters
import models.FileFormat.Pdf
import models.{EoriHistory, PostponedVatStatementFile}
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.download_link_pvat

import java.time.LocalDate

case class PostponedVatViewModel(statementsForAllEoris: Seq[PostponedVatStatementsForEori])

case class StatementDisplayData(historyIndex: Int,
                                monthYear: String,
                                monthYearId: String,
                                formattedMonth: String,
                                sources: Seq[SourceDisplay],
                                index: Int)

case class SourceDisplay(source: String, files: Seq[PostponedVatStatementFile])

case class PostponedVatStatementsForEori(eoriHistory: EoriHistory,
                                         currentStatements: Seq[PostponedVatStatementsByMonth],
                                         requestedStatements: Seq[PostponedVatStatementsByMonth])
  extends OrderedByEoriHistory[PostponedVatStatementsForEori]

case class PostponedVatStatementsByMonth(date: LocalDate,
                                         files: Seq[PostponedVatStatementFile] = Seq.empty)
                                        (implicit messages: Messages)
  extends Ordered[PostponedVatStatementsByMonth] {

  val formattedMonthYear: String = Formatters.dateAsMonthAndYear(date)
  val formattedMonthYearAsId: String = Formatters.dateAsMonthAndYearAsId(date)

  val formattedMonth: String = Formatters.dateAsMonth(date)
  val pdf: Option[PostponedVatStatementFile] = files.find(_.fileFormat == Pdf)

  override def compare(that: PostponedVatStatementsByMonth): Int = date.compareTo(that.date)
}

object PostponedVatViewModel {
  val sources: Seq[String] = Seq("CDS", "CHIEF")

  def historiesWithDisplayData(statementsForAllEoris: Seq[PostponedVatStatementsForEori]): Seq[StatementDisplayData] = {
    for {
      (statementsForEori, historyIndex) <- statementsForAllEoris.zipWithIndex
      if statementsForEori.requestedStatements.nonEmpty
      (statement, index) <- statementsForEori.requestedStatements.sorted.zipWithIndex
    } yield createStatementDisplayData(historyIndex, statement, index)
  }

  private def createStatementDisplayData(historyIndex: Int,
                                         statementsOfOneMonth: PostponedVatStatementsByMonth,
                                         index: Int): StatementDisplayData = {
    val groupedStatements = groupedStatementsBySource(statementsOfOneMonth)
    val sourceDisplays = sources.map { source =>
      SourceDisplay(source, groupedStatements.getOrElse(source, Seq.empty))
    }

    StatementDisplayData(
      historyIndex,
      statementsOfOneMonth.formattedMonthYear,
      statementsOfOneMonth.formattedMonthYearAsId,
      statementsOfOneMonth.formattedMonth,
      sourceDisplays,
      index
    )
  }

  def groupedStatementsBySource(
                                 statementsOfOneMonth: PostponedVatStatementsByMonth
                               ): Map[String, Seq[PostponedVatStatementFile]] = {
    statementsOfOneMonth.files.groupBy(_.metadata.source)
  }

  def renderSourceDisplay(sourceDisplay: SourceDisplay,
                          historyIndex: Int,
                          index: Int,
                          date: String)
                         (implicit messages: Messages): Html = {
    if (sourceDisplay.files.isEmpty) {
      Html(missingFileMessage(sourceDisplay.source))
    } else {
      renderDisplayRows(sourceDisplay.files, historyIndex, index, date)
    }
  }

  private def renderDisplayRows(files: Seq[PostponedVatStatementFile],
                                historyIndex: Int,
                                index: Int,
                                date: String)
                               (implicit messages: Messages): Html = {
    Html(files.map { file =>
      new download_link_pvat().apply(
        Some(file),
        Pdf,
        file.metadata.source,
        s"requested-${file.metadata.source}-statements-list-$historyIndex-row-$index-pdf-download-link",
        date)
    }.mkString)
  }

  def missingFileMessage(source: String)(implicit messages: Messages): String = {
    messages("cf.account.postponed-vat.missing-file-type", source)
  }
}
