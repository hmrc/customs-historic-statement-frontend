/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase
import forms.HistoricDateRequestPageFormProvider
import models.{C79Certificate, HistoricDates, PostponedVATStatement}
import play.api.data.{Form, FormError}
import utils.Utils.emptyString

class HistoricDateRequestPageFormProviderSpec extends SpecBase {

  "apply" should {

    "bind the form correctly with correct date" in new Setup {
      val form: Form[HistoricDates] = histDateReqPageForm(PostponedVATStatement)

      val formAfterBinding: Form[HistoricDates] = form.bind(
        Map(
          "start.year"  -> s"$year2021",
          "start.month" -> s"$month3",
          "start.day"   -> s"$day1",
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10",
          "end.day"     -> s"$day12"
        )
      )

      formAfterBinding.hasErrors mustBe false
    }

    "throw error when file role is C79Certificate and start date is before 2019-10-1" in new Setup {
      val form: Form[HistoricDates] = histDateReqPageForm(C79Certificate)

      val formAfterBinding: Form[HistoricDates] = form.bind(
        Map(
          "start.year"  -> s"$year2019",
          "start.month" -> s"$month3",
          "start.day"   -> s"$day1",
          "end.year"    -> s"$year2019",
          "end.month"   -> s"$month10",
          "end.day"     -> s"$day12"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start",
          List(
            "cf.historic.document.request.form.error.date-earlier-than-system-start-date.c79"
          ),
          List()
        )
      ) mustBe true
    }

    "throw error for empty start and end dates" in new Setup {
      val form: Form[HistoricDates] = histDateReqPageForm(C79Certificate)

      val formAfterBinding: Form[HistoricDates] = form.bind(
        Map(
          "start.year"  -> emptyString,
          "start.month" -> emptyString,
          "start.day"   -> emptyString,
          "end.year"    -> emptyString,
          "end.month"   -> emptyString,
          "end.day"     -> emptyString
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start",
          List(
            "cf.historic.document.request.form.error.start.date-missing.C79Certificate"
          ),
          List()
        )
      ) mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "end",
          List(
            "cf.historic.document.request.form.error.end.date-missing.C79Certificate"
          ),
          List()
        )
      ) mustBe true

    }

    "return an error for empty year field" in new Setup {
      val form: Form[HistoricDates] = histDateReqPageForm(C79Certificate)

      val formAfterBinding: Form[HistoricDates] = form.bind(
        Map(
          "start.year"  -> emptyString,
          "start.month" -> s"$month3",
          "start.day"   -> s"$day1",
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10",
          "end.day"     -> s"$day12"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start.year",
          List(
            "cf.historic.document.request.form.error.start.year.date-number-invalid.C79Certificate"
          ),
          List()
        )
      ) mustBe true
    }

    "return an error for empty month field" in new Setup {
      val form: Form[HistoricDates] = histDateReqPageForm(C79Certificate)

      val formAfterBinding: Form[HistoricDates] = form.bind(
        Map(
          "start.year"  -> s"$year2021",
          "start.month" -> emptyString,
          "start.day"   -> s"$day1",
          "end.year"    -> s"$year2021",
          "end.month"   -> s"$month10",
          "end.day"     -> s"$day12"
        )
      )

      formAfterBinding.hasErrors mustBe true

      formAfterBinding.errors.contains(
        FormError(
          "start.month",
          List(
            "cf.historic.document.request.form.error.start.month.date-number-invalid.C79Certificate"
          ),
          List()
        )
      ) mustBe true
    }
  }

  trait Setup {
    val histDateReqPageForm = new HistoricDateRequestPageFormProvider()
    val year2021            = 2021
    val year2024            = 2024
    val year2019            = 2019

    val month3  = 3
    val month4  = 4
    val month10 = 10

    val day12 = 12
    val day10 = 10
    val day1  = 1
  }
}
