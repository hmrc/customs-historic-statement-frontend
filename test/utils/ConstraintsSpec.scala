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

package utils

import base.SpecBase

import forms.mappings.Constraints
import play.api.data.validation.Constraints.maxLength
import play.api.data.validation.{Invalid, Valid}
import utils.Utils.emptyString

class ConstraintsSpec extends SpecBase with Constraints {

  "yearLength" must {
    "return Valid for a string shorter than the allowed length" in new Setup {
      val result = maxLength(length, "error.length")("a" * 3)
      result mustEqual Valid
    }

    "return Valid for an empty string" in new Setup {
      val result = maxLength(length, "error.length")(emptyString)
      result mustEqual Valid
    }

    "return Valid for a string equal to the allowed length" in new Setup {
      val result = maxLength(length, "error.length")("a" * 4)
      result mustEqual Valid
    }

    "return Invalid for a string longer than the allowed length" in new Setup {
      val result = maxLength(length, "error.length")("a" * 5)
      result mustEqual Invalid("error.length", length)
    }
  }

  trait Setup {
    val length = 4
  }
}
