/*
 * Copyright 2021 HM Revenue & Customs
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

import models.{FileRole, PostponedVATStatement}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import java.time.{LocalDate, LocalDateTime, Period}

trait Constraints {

  private lazy val etmpStatementsDate: LocalDate = LocalDate.of(2019, 10, 1)
  private lazy val pvatStatementsDate: LocalDate = LocalDate.of(2021, 1, 1)
  private lazy val currentDate: LocalDate = LocalDateTime.now().toLocalDate
  private val olderThan = Period.ofMonths(6)

  def tooRecentDate: Constraint[LocalDate] = {
    Constraint {
      // exclude the current month
      case request if Period.between(request, currentDate.minusMonths(1)).toTotalMonths < olderThan.toTotalMonths =>
        Invalid(ValidationError("cf.historic.document.request.form.error.date-too-recent"))
      case _ => Valid
    }
  }

  def earlierThanSystemStartDate(fileRole: FileRole): Constraint[LocalDate] = Constraint {
    case request if (Period.between(request, etmpStatementsDate).toTotalMonths > 0 && fileRole != PostponedVATStatement) =>
      Invalid(ValidationError("cf.historic.document.request.form.error.date-earlier-than-system-start-date"))
    case _ => Valid
  }

  def earlierThanPVATStartDate(fileRole: FileRole): Constraint[LocalDate] = Constraint {
    case request if (Period.between(request, pvatStatementsDate).toTotalMonths > 0  && fileRole == PostponedVATStatement) =>
      Invalid(ValidationError("cf.historic.document.request.form.error.date-earlier-than-pvat-start-date"))
    case _ => Valid
  }
}
