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

package models

import base.SpecBase
import utils.TestData.*
import play.api.test.Helpers
import viewmodels.VatCertificatesByMonth

import java.time.LocalDate

class VatCertificatesByMonthSpec extends SpecBase {

  "compare" should {
    "return the correct order" in {
      val one      = 1
      val minusOne = -1

      val vatCertificatesByMonth  = VatCertificatesByMonth(LocalDate.of(year, month, day))(Helpers.stubMessages())
      val vatCertificatesByMonth2 = VatCertificatesByMonth(LocalDate.of(year2, month, day))(Helpers.stubMessages())
      vatCertificatesByMonth.compare(vatCertificatesByMonth2) mustBe minusOne
      vatCertificatesByMonth2.compare(vatCertificatesByMonth) mustBe one
    }
  }
}
