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

import java.time.LocalDate

case class PostponedVatViewModel(statementsForAllEoris: Seq[PostponedVatStatementsForEori])

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
