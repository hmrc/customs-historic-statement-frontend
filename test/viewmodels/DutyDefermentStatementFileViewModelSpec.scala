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

package viewmodels

import base.SpecBase
import models.DDStatementType.{Excise, Supplementary, UnknownStatementType}
import models.FileFormat.Pdf
import models.{DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata}
import play.api.test.Helpers
import viewmodels.SdesFileViewModels.DutyDefermentStatementFileViewModel

class DutyDefermentStatementFileViewModelSpec extends SpecBase {

  "download link Aria label" should {
    "return correct label for excise, supplementary and unknown" in new Setup {

      val excise = DutyDefermentStatementFile(
        s"12345678.123",
        s"http://second.com/",
        size,
        DutyDefermentStatementFileMetadata(
          year,
          month,
          day,
          year2,
          month,
          day,
          Pdf,
          DutyDefermentStatement,
          Excise,
          Some(true),
          Some("BACS"),
          "12345678"
        )
      )

      val supplementary = DutyDefermentStatementFile(
        s"12345678.123",
        s"http://second.com/",
        size,
        DutyDefermentStatementFileMetadata(
          year,
          month,
          day,
          year2,
          month,
          day,
          Pdf,
          DutyDefermentStatement,
          Supplementary,
          Some(true),
          Some("BACS"),
          "12345678"
        )
      )

      val unknown = DutyDefermentStatementFile(
        s"12345678.123",
        s"http://second.com/",
        size,
        DutyDefermentStatementFileMetadata(
          year,
          month,
          day,
          year2,
          month,
          day,
          Pdf,
          DutyDefermentStatement,
          UnknownStatementType,
          Some(true),
          Some("BACS"),
          "12345678"
        )
      )

      val exciseViewModel = new DutyDefermentStatementFileViewModel(excise)

      exciseViewModel.downloadLinkAriaLabel()(Helpers.stubMessages()) mustBe
        "cf.account.detail.excise-download-link"

      val supplementaryViewModel = new DutyDefermentStatementFileViewModel(supplementary)

      supplementaryViewModel.downloadLinkAriaLabel()(Helpers.stubMessages()) mustBe
        "cf.account.detail.supplementary-download-link"

      val unknownViewModel = new DutyDefermentStatementFileViewModel(unknown)

      unknownViewModel.downloadLinkAriaLabel()(Helpers.stubMessages()) mustBe
        "cf.account.detail.download-link"
    }
  }

  trait Setup {
    val size  = 47
    val year  = 2011
    val year2 = 2021
    val month = 11
    val day   = 27
  }
}
