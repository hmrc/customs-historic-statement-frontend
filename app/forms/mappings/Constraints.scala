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

import models.{C79Certificate, FileRole}
import play.api.data.validation.{Constraint, Invalid, Valid, ValidationError}

import java.time.{LocalDate, LocalDateTime, Period}

trait Constraints {

  val offset            = 6
  private val oneMonth  = 1
  private val olderThan = Period.ofMonths(offset)

  def currentDate: LocalDate = LocalDateTime.now().toLocalDate

  def tooRecentDate(fileRole: FileRole): Constraint[LocalDate] =
    Constraint {
      case request
          if Period
            .between(request, currentDate.minusMonths(oneMonth))
            .toTotalMonths < olderThan.toTotalMonths =>
        if (fileRole == C79Certificate) {
          Invalid(ValidationError("cf.historic.document.request.form.error.date-too-recent.c79"))
        } else {
          Invalid(ValidationError("cf.historic.document.request.form.error.date-too-recent"))
        }
      case _ => Valid
    }
}
