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

package forms

import forms.mappings.Mappings
import models.{FileRole, HistoricDates}
import play.api.data.Form
import play.api.data.Forms.mapping
import javax.inject.Inject

class HistoricDateRequestPageFormProvider @Inject() extends Mappings {

  def apply(fileRole: FileRole): Form[HistoricDates] = {
    Form(mapping(
      "start" -> localDate(
        emptyStartMonth = "cf.historic.document.request.form.error.start.month.date-number-invalid",
        emptyStartYear = "cf.historic.document.request.form.error.start.year.date-number-invalid",
        emptyEndMonth = "cf.historic.document.request.form.error.end.month.date-number-invalid",
        emptyEndYear = "cf.historic.document.request.form.error.end.year.date-number-invalid",
        emptyStartDate = "cf.historic.document.request.form.error.start.date-missing",
        emptyEndDate = "cf.historic.document.request.form.error.end.date-missing",
        invalidMonth = "cf.historic.document.request.form.error.month.invalid",
        invalidYear = "cf.historic.document.request.form.error.year.invalid"
      ).verifying(earlierThanSystemStartDate(fileRole))
        .verifying(earlierThanPVATStartDate(fileRole))
        .verifying(earlierThanDDStatementStartDate(fileRole)),
      "end" -> localDate(
        emptyStartMonth = "cf.historic.document.request.form.error.start.month.date-number-invalid",
        emptyStartYear = "cf.historic.document.request.form.error.start.year.date-number-invalid",
        emptyEndMonth = "cf.historic.document.request.form.error.end.month.date-number-invalid",
        emptyEndYear = "cf.historic.document.request.form.error.end.year.date-number-invalid",
        emptyStartDate = "cf.historic.document.request.form.error.start.date-missing",
        emptyEndDate = "cf.historic.document.request.form.error.end.date-missing",
        invalidMonth = "cf.historic.document.request.form.error.month.invalid",
        invalidYear = "cf.historic.document.request.form.error.year.invalid"
      ).verifying(tooRecentDate)
        .verifying(earlierThanPVATStartDate(fileRole))
    )(HistoricDates.apply)(HistoricDates.unapply)
    )
  }
}
