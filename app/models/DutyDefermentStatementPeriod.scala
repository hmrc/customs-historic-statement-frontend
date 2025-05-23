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

package models

import helpers.Formatters
import models.DDStatementType.{DutyDeferment, Excise, ExciseDeferment, Supplementary}
import services.DateConverters._
import play.api.i18n.Messages

import java.time.LocalDate

case class DutyDefermentStatementPeriod(
  fileRole: FileRole,
  defermentStatementType: DDStatementType,
  monthAndYear: LocalDate,
  startDate: LocalDate,
  endDate: LocalDate,
  statementFiles: Seq[DutyDefermentStatementFile] = Seq.empty
) extends Ordered[DutyDefermentStatementPeriod] {

  def compare(that: DutyDefermentStatementPeriod): Int =
    this.defermentStatementType compare that.defermentStatementType match {
      case 0 =>
        that.endDate compare endDate match {
          case 0 => startDate compare that.startDate
          case c => c
        }
      case c => c
    }

  def findStatementFileByFormat(fileFormat: FileFormat): Seq[DutyDefermentStatementFile] =
    statementFiles.filter(_.metadata.fileFormat == fileFormat)

  def unavailableLinkHiddenText(fileFormat: FileFormat)(implicit messages: Messages): String = {
    lazy val endDateMonthAndYear    = Formatters.dateAsMonthAndYear(endDate)
    lazy val endDateDayMonthAndYear = Formatters.dateAsDayMonthAndYear(endDate)

    defermentStatementType match {
      case Supplementary =>
        messages("cf.account.detail.missing-file-type-supplementary", fileFormat, endDateMonthAndYear)

      case Excise => messages("cf.account.detail.missing-file-type-excise", fileFormat, endDateMonthAndYear)

      case ExciseDeferment =>
        messages("cf.account.detail.missing-file-type-excise-deferment", fileFormat, endDateMonthAndYear)

      case DutyDeferment =>
        messages("cf.account.detail.missing-file-type-duty-deferment", fileFormat, endDateMonthAndYear)

      case _ =>
        messages(
          "cf.account.detail.missing-file-type",
          fileFormat,
          Formatters.dateAsDay(startDate),
          endDateDayMonthAndYear
        )
    }
  }
}
