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

import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import base.SpecBase

class FormHelperSpec extends SpecBase {
  "updateFormErrorKeyForStartAndEndDate" must {
    "append .month in the FormError key when key is either start or end and error msg key is " +
      "emptyStartMonthKey, emptyStartYearKey, emptyStartDateKey, invalidStartMonthKey and invalidStartYearKey" in new SetUp {

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.historic.document.request.form.error.start.month.date-number-invalid") shouldBe s"$startKey.month"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.historic.document.request.form.error.start.date-missing") shouldBe s"$startKey.month"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.historic.document.request.form.error.start.month.invalid") shouldBe s"$startKey.month"
    }

    "append .year in the FormError key when key is either start or end and " +
      "error msg key is of invalid year length" in new SetUp {

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        startKey, "cf.historic.document.request.form.error.year.invalid-length") shouldBe s"$startKey.year"

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        endKey, "cf.historic.document.request.form.error.year.invalid-length") shouldBe s"$endKey.year"
    }

    "return the unchanged key when key in neither start or end" in new SetUp {
      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        defaultKey, "cf.historic.document.request.form.error.year.invalid-length") shouldBe defaultKey

      FormHelper.updateFormErrorKeyForStartAndEndDate()(
        defaultKey, "cf.historic.document.request.form.error.year.invalid-length") shouldBe defaultKey
    }
  }
}

trait SetUp {
  val startKey = "start"
  val endKey = "end"
  val defaultKey = "default"
}
