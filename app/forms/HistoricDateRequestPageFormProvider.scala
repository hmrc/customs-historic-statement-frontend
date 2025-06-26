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

package forms

import forms.mappings.Mappings
import models.{FileRole, HistoricDates}
import play.api.data.Form
import play.api.data.Forms.mapping
import javax.inject.Inject

class HistoricDateRequestPageFormProvider @Inject() extends Mappings {

  def apply(fileRole: FileRole): Form[HistoricDates] =
    Form(
      mapping(
        "start" -> localDate(
          emptyStartMonth = s"cf.historic.document.request.form.error.start.month.date-number-invalid.$fileRole",
          emptyStartYear = s"cf.historic.document.request.form.error.start.year.date-number-invalid.$fileRole",
          emptyEndMonth = s"cf.historic.document.request.form.error.end.month.date-number-invalid.$fileRole",
          emptyEndYear = s"cf.historic.document.request.form.error.end.year.date-number-invalid.$fileRole",
          emptyStartDate = s"cf.historic.document.request.form.error.start.date-missing.$fileRole",
          emptyEndDate = s"cf.historic.document.request.form.error.end.date-missing.$fileRole",
          invalidMonth = "cf.historic.document.request.form.error.start.month.invalid",
          invalidYear = "cf.historic.document.request.form.error.start.year.invalid",
          invalidDate = "cf.historic.document.request.form.error.start.date.invalid"
        ).verifying(earlierThanSystemStartDate(fileRole))
          .verifying(earlierThanPVATStartDate(fileRole))
          .verifying(earlierThanDDStatementStartDate(fileRole)),
        "end"   -> localDate(
          emptyStartMonth = s"cf.historic.document.request.form.error.start.month.date-number-invalid.$fileRole",
          emptyStartYear = s"cf.historic.document.request.form.error.start.year.date-number-invalid.$fileRole",
          emptyEndMonth = s"cf.historic.document.request.form.error.end.month.date-number-invalid.$fileRole",
          emptyEndYear = s"cf.historic.document.request.form.error.end.year.date-number-invalid.$fileRole",
          emptyStartDate = s"cf.historic.document.request.form.error.start.date-missing.$fileRole",
          emptyEndDate = s"cf.historic.document.request.form.error.end.date-missing.$fileRole",
          invalidMonth = "cf.historic.document.request.form.error.end.month.invalid",
          invalidYear = "cf.historic.document.request.form.error.end.year.invalid",
          invalidDate = "cf.historic.document.request.form.error.end.date.invalid"
        ).verifying(tooRecentDate(fileRole))
      )(HistoricDates.apply)(hd => Some(Tuple.fromProductTyped(hd)))
    )
}
