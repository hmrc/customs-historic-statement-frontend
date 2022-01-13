/*
 * Copyright 2022 HM Revenue & Customs
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

package forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter
import java.time.LocalDate
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
                                            emptyStartMonth: String,
                                            emptyStartYear: String,
                                            emptyEndMonth: String,
                                            emptyEndYear: String,
                                            emptyStartDate: String,
                                            emptyEndDate: String,
                                            invalidMonth: String,
                                            invalidYear: String,
                                            args: Seq[String] = Seq.empty
                                          ) extends Formatter[LocalDate] with Formatters {

  private val fieldKeys: List[String] = List("month", "year")

  private def toDate(key: String, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
  validMonth(month) match {
      case true =>
        Try(LocalDate.of(year, month, 1)) match {
          case Success(date) =>
            Right(date)
          case Failure(_) =>
            Left(Seq(FormError(key, invalidYear, args)))
        }
      case _ => Left(Seq(FormError(key, invalidMonth, args)))
    }

  private def formatDate(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val intMonth = intFormatter(
      requiredKey = invalidMonth,
      wholeNumberKey = invalidMonth,
      nonNumericKey = invalidMonth,
      args
    )

    val intYear = intFormatter(
      requiredKey = invalidYear,
      wholeNumberKey = invalidYear,
      nonNumericKey = invalidYear,
      args
    )

    for {
      month <- intMonth.bind(s"$key.month", data).right
      year  <- intYear.bind(s"$key.year", data).right
      date  <- toDate(key, month, year).right
    } yield date
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {

    val fields = fieldKeys.map {
      field =>
        field -> data.get(s"$key.$field").filter(_.nonEmpty)
    }.toMap

    lazy val missingFields = fields
      .withFilter(_._2.isEmpty)
      .map(_._1)
      .toList

    fields.count(_._2.isDefined) match {
      case 2 =>
        formatDate(key, data).left.map {
          _.map(_.copy(key = key, args = args))
        }
      case 1 =>
        (key, missingFields.head) match {
        case ("start", "month") => Left(List(FormError(key, emptyStartMonth, args)))
        case ("start", "year") => Left(List(FormError(key, emptyStartYear, args)))
        case ("end", "month") => Left(List(FormError(key, emptyEndMonth, args)))
        case ("end", "year") => Left(List(FormError(key, emptyEndYear, args)))
      }
      case _ =>
        (key, missingFields) match {
          case ("start", List("month", "year")) => Left(List(FormError(key, emptyStartDate, args)))
          case ("end", List("month","year")) => Left(List(FormError(key, emptyEndDate, args)))
        }
      }
  }

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year" -> value.getYear.toString
    )

  private def validMonth(month: Int): Boolean = {
    month > 0 && month < 13
  }
}
