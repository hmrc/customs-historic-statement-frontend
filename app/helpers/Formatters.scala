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

package helpers

import play.api.i18n.Messages

import java.time.LocalDate
import java.time.format.DateTimeFormatter

trait DateFormatters {
  def dateAsMonth(date: LocalDate)(implicit messages: Messages): String =
    messages(s"month.${date.getMonthValue}")

  def dateAsDayMonthAndYear(date: LocalDate)(implicit messages: Messages): String =
    s"${date.getDayOfMonth} ${dateAsMonth(date)} ${date.getYear}"

  def dateAsMonthAndYear(date: LocalDate)(implicit messages: Messages): String =
    s"${dateAsMonth(date)} ${date.getYear}"

  def dateAsMonthAndYearAsId(date: LocalDate)(implicit messages: Messages): String =
    s"${dateAsMonth(date)}-${date.getYear}"

  def dateAsDay(date: LocalDate): String = DateTimeFormatter.ofPattern("d").format(date)

  def periodAsStartToEndMonth(periodStartMonth: Int, periodEndMonth: Int)(implicit messages: Messages): String = {
    val startMonth = messages(s"month.$periodStartMonth")
    val endMonth   = messages(s"month.$periodEndMonth")

    s"$startMonth ${messages("cf.cash-statement-requested-to")} $endMonth"
  }
}

trait FileFormatters {

  private val kbThreshold      = 1024
  private val mbThreshold: Int = 1024 * 1024

  def fileSize(size: Long): String = size match {
    case kb if kb >= kbThreshold && kb < mbThreshold => s"${kb / kbThreshold}KB"
    case mb if mb >= mbThreshold                     => f"${mb / mbThreshold.toDouble}%.1fMB"
    case _                                           => "1KB"
  }
}

object Formatters extends DateFormatters with FileFormatters
