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

package forms.mappings

import models.{C79Certificate, DutyDefermentStatement, FileRole, PostponedVATStatement, SecurityStatement}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}
import utils.Utils.emptyString

import java.time.{LocalDate, LocalDateTime, Period}

trait Constraints {

  val offset = 6
  private val dayOne = 1
  private val pvatStatementMonth = 1
  private val etmpStatementMonth = 10
  private val ddStatementMonth = 9
  private val etmpAndDDStatementYear = 2019
  private val pvatStatementYear = 2021
  private val validYearLength = 4

  private lazy val etmpStatementsDate: LocalDate = LocalDate.of(etmpAndDDStatementYear, etmpStatementMonth, dayOne)
  private lazy val pvatStatementsDate: LocalDate = LocalDate.of(pvatStatementYear, pvatStatementMonth, dayOne)
  private lazy val dutyDefermentStatementsDate: LocalDate = LocalDate.of(etmpAndDDStatementYear, ddStatementMonth, dayOne)
  private val olderThan = Period.ofMonths(offset)

  def currentDate: LocalDate = LocalDateTime.now().toLocalDate

  def tooRecentDate(fileRole: FileRole): Constraint[LocalDate] = {
    Constraint {
      case request if (request.getYear.toString.length != validYearLength ||
        !request.getYear.toString.matches("^[0-9]+")) =>

        Invalid(ValidationError("cf.historic.document.request.form.error.year.invalid-length"))

      case request if (Period.between(request,
        currentDate.minusMonths(pvatStatementMonth)).toTotalMonths < olderThan.toTotalMonths) =>

        if (fileRole == C79Certificate) {
          Invalid(ValidationError("cf.historic.document.request.form.error.date-too-recent.c79"))
        } else {
          Invalid(ValidationError("cf.historic.document.request.form.error.date-too-recent"))
        }
      case _ => Valid
    }
  }

  def earlierThanSystemStartDate(fileRole: FileRole): Constraint[LocalDate] = {
    val messageKey = fileRole match {
      case C79Certificate =>
        "cf.historic.document.request.form.error.date-earlier-than-system-start-date.c79"

      case SecurityStatement =>
        "cf.historic.document.request.form.error.date-earlier-than-system-start-date.securities"

      case _ => emptyString
    }

    Constraint {
      case request if (request.getYear.toString.length != validYearLength ||
        !request.getYear.toString.matches("^[0-9]+"))

        && fileRole != DutyDefermentStatement && fileRole != PostponedVATStatement =>
        Invalid(ValidationError("cf.historic.document.request.form.error.year.invalid-length"))

      case request if request.isBefore(etmpStatementsDate) && fileRole
        != PostponedVATStatement && fileRole != DutyDefermentStatement =>
        Invalid(ValidationError(messageKey))

      case _ => Valid
    }
  }

  def earlierThanPVATStartDate(fileRole: FileRole): Constraint[LocalDate] = Constraint {

    case request if (request.getYear.toString.length != validYearLength ||
      !request.getYear.toString.matches("^[0-9]+"))

      && (fileRole == PostponedVATStatement) =>
      Invalid(ValidationError("cf.historic.document.request.form.error.year.invalid-length"))

    case request if request.isBefore(pvatStatementsDate) && fileRole == PostponedVATStatement =>
      Invalid(ValidationError("cf.historic.document.request.form.error.date-earlier-than-pvat-start-date"))

    case _ => Valid
  }

  def earlierThanDDStatementStartDate(fileRole: FileRole): Constraint[LocalDate] = Constraint {

    case request if (request.getYear.toString.length != validYearLength ||
      !request.getYear.toString.matches("^[0-9]+"))

      && (fileRole == DutyDefermentStatement) =>
      Invalid(ValidationError("cf.historic.document.request.form.error.year.invalid-length"))

    case request if request.isBefore(dutyDefermentStatementsDate) && fileRole == DutyDefermentStatement =>
      Invalid(ValidationError(
        "cf.historic.document.request.form.error.date-earlier-than-dutydefermentstatement-start-date"))

    case _ => Valid
  }
}
