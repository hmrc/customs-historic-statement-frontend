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

object FormHelper {

  def updateFormErrorKeyForStartAndEndDate(): (String, String) => String = (key: String, errorMsg: String) => {

    val emptyStartMonthKey   = "cf.historic.document.request.form.error.start.month.date-number-invalid"
    val emptyStartYearKey    = "cf.historic.document.request.form.error.start.year.date-number-invalid"
    val emptyStartDateKey    = "cf.historic.document.request.form.error.start.date-missing"
    val invalidStartMonthKey = "cf.historic.document.request.form.error.start.month.invalid"
    val invalidStartYearKey  = "cf.historic.document.request.form.error.year.invalid"

    val emptyEndMonthKey   = "cf.historic.document.request.form.error.end.month.date-number-invalid"
    val emptyEndYearKey    = "cf.historic.document.request.form.error.end.year.date-number-invalid"
    val emptyEndDateKey    = "cf.historic.document.request.form.error.end.date-missing"
    val invalidEndMonthKey = "cf.historic.document.request.form.error.end.month.invalid"
    val invalidEndYearKey  = "cf.historic.document.request.form.error.year.invalid"

    val startDateMsgKeyList =
      List(emptyStartMonthKey, emptyStartYearKey, emptyStartDateKey, invalidStartMonthKey, invalidStartYearKey)

    val endDateMsgKeyList =
      List(emptyEndMonthKey, emptyEndYearKey, emptyEndDateKey, invalidEndMonthKey, invalidEndYearKey)

    if (key.equals("start") || key.equals("end")) {
      if (startDateMsgKeyList.contains(errorMsg) || endDateMsgKeyList.contains(errorMsg)) {
        s"$key.month"
      } else {
        s"$key.year"
      }
    } else {
      key
    }
  }
}
