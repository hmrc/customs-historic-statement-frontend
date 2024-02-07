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
import play.api.test.Helpers
import viewmodels.VatCertificatesByMonth

import java.time.LocalDate

class VatCertificatesByMonthSpec extends SpecBase {

  "compare" should {
    "return the correct order" in {

      val year = 2019
      val year2 = 2018
      val ten = 10
      val one = 1
      val minusOne = -1

      val vatCertificatesByMonth = VatCertificatesByMonth(LocalDate.of(year, ten, ten))(Helpers.stubMessages())
      val vatCertificatesByMonth2 = VatCertificatesByMonth(LocalDate.of(year2, ten, ten))(Helpers.stubMessages())
      vatCertificatesByMonth.compare(vatCertificatesByMonth2) mustBe one
      vatCertificatesByMonth2.compare(vatCertificatesByMonth) mustBe minusOne
    }
  }
}
