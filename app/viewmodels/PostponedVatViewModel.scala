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
import play.twirl.api.HtmlFormat
import utils.Utils
import utils.Utils.h2Component
import views.html.components.download_link_pvat

import java.time.LocalDate

case class PostponedVatViewModel(statementsForAllEoris: Seq[PostponedVatStatementsForEori],
                                 statementDisplayData: Seq[StatementDisplayData])

case class StatementDisplayData(historyIndex: Int,
                                monthYear: String,
                                monthYearId: String,
                                formattedMonth: String,
                                sources: Seq[SourceDisplay],
                                index: Int,
                                dateHeader: HtmlFormat.Appendable,
                                statementItem: HtmlFormat.Appendable)

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

  def apply(statementsForAllEoris: Seq[PostponedVatStatementsForEori])(implicit messages: Messages): PostponedVatViewModel = {

    val statementItems = generateStatementData(statementsForAllEoris)

    PostponedVatViewModel(statementsForAllEoris, statementItems)
  }

  private def generateStatementData(statementsForAllEoris: Seq[PostponedVatStatementsForEori])
                                   (implicit messages: Messages): Seq[StatementDisplayData] = {
    statementsForAllEoris.zipWithIndex.flatMap {
      case (statementsForEori, historyIndex) =>
        statementsForEori.requestedStatements.sorted.zipWithIndex.map {
          case (statement, index) => generateStatementDataItem(historyIndex, statement, index)
        }
    }
  }

  private def generateStatementDataItem(historyIndex: Int, statementsByMonth: PostponedVatStatementsByMonth, index: Int)
                                       (implicit messages: Messages): StatementDisplayData = {
    val sources: Seq[String] = Seq("CDS", "CHIEF")

    val sourceDisplay = sources.map(source => SourceDisplay(
      source, groupStatementsBySource(statementsByMonth).getOrElse(source, Seq.empty)))

    val dateHeader = generateDateHeaderHtml(
      statementsByMonth.formattedMonthYear, statementsByMonth.formattedMonthYearAsId)

    val statementItem = generateStatementItemHtml(
      sourceDisplay, historyIndex, index, statementsByMonth.formattedMonthYear)

    StatementDisplayData(
      historyIndex = historyIndex,
      monthYear = statementsByMonth.formattedMonthYear,
      monthYearId = statementsByMonth.formattedMonthYearAsId,
      formattedMonth = statementsByMonth.formattedMonth,
      sources = sourceDisplay,
      index = index,
      dateHeader = dateHeader,
      statementItem = statementItem)
  }

  private def groupStatementsBySource(statementsByMonth: PostponedVatStatementsByMonth
                                     ): Map[String, Seq[PostponedVatStatementFile]] =
    statementsByMonth.files.groupBy(_.metadata.source)

  private def generateStatementItemHtml(sourceDisplays: Seq[SourceDisplay],
                                        historyIndex: Int,
                                        index: Int,
                                        date: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    val items = sourceDisplays.map { sourceDisplay =>
      s"<li>${generateSourceItemHtml(sourceDisplay, historyIndex, index, date).body}</li>"
    }.mkString

    HtmlFormat
      .raw(s"""<ul class="govuk-list" id="requested-statements-list-$historyIndex-row-$index">$items</ul>""")
  }

  private def generateSourceItemHtml(sourceDisplay: SourceDisplay,
                                     historyIndex: Int,
                                     index: Int,
                                     date: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    if (sourceDisplay.files.isEmpty) {
      HtmlFormat.raw(generateMissingFileMessage(sourceDisplay.source))
    } else {
      generateDownloadLinkHtml(sourceDisplay.files, historyIndex, index, date)
    }
  }

  private def generateDownloadLinkHtml(files: Seq[PostponedVatStatementFile],
                                       historyIndex: Int,
                                       index: Int,
                                       date: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    HtmlFormat.raw(files.map { file =>
      new download_link_pvat().apply(
        file = Some(file),
        fileFormat = Pdf,
        source = file.metadata.source,
        id = s"requested-${file.metadata.source}-statements-list-$historyIndex-row-$index-pdf-download-link",
        period = date)
    }.mkString)
  }

  private def generateDateHeaderHtml(monthYear: String,
                                     monthYearId: String)(implicit messages: Messages): HtmlFormat.Appendable =
    HtmlFormat.raw(h2Component(msg = monthYear, id = Some(s"period-$monthYearId")).toString)

  private def generateMissingFileMessage(source: String)(implicit messages: Messages): String =
    messages("cf.account.postponed-vat.missing-file-type", source)
}
