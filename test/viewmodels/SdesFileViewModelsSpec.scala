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
import models.DDStatementType.{DutyDeferment, Excise, ExciseDeferment, Supplementary, UnknownStatementType}
import models.FileFormat.Pdf
import models.{DDStatementType, DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata}
import play.api.Application
import play.api.i18n.Messages
import viewmodels.SdesFileViewModels.DutyDefermentStatementFileViewModel

class SdesFileViewModelsSpec extends SpecBase {

  "DutyDefermentStatementFileViewModel.downloadLinkAriaLabel" should {

    "return correct label for different defermentStatementType" in new Setup {

      val exciseViewModel = new DutyDefermentStatementFileViewModel(excise)

      exciseViewModel.downloadLinkAriaLabel() mustBe "Download excise summary PDF for November 2021 (1KB)"

      val supplementaryViewModel = new DutyDefermentStatementFileViewModel(supplementary)

      supplementaryViewModel
        .downloadLinkAriaLabel() mustBe "Download supplementary end of month PDF for November 2021 (1KB)"

      val unknownViewModel = new DutyDefermentStatementFileViewModel(unknown)

      unknownViewModel.downloadLinkAriaLabel() mustBe "Download PDF for 27 to 27 November 2021 (1KB)"

      val exciseDefermentViewModel = new DutyDefermentStatementFileViewModel(exciseDeferment)

      exciseDefermentViewModel
        .downloadLinkAriaLabel() mustBe "Download excise deferment 1920 summary PDF for November 2021 (1KB)"

      val dutyDefermentViewModel = new DutyDefermentStatementFileViewModel(dutyDeferment)

      dutyDefermentViewModel
        .downloadLinkAriaLabel() mustBe "Download duty deferment 1720 summary PDF for November 2021 (1KB)"
    }
  }

  trait Setup {
    val size  = 47
    val year  = 2011
    val year2 = 2021
    val month = 11
    val day   = 27

    val fileName    = "test_file"
    val downloadUrl = "http://second.com/"

    def metaData(defermentStatementType: DDStatementType): DutyDefermentStatementFileMetadata =
      DutyDefermentStatementFileMetadata(
        year,
        month,
        day,
        year2,
        month,
        day,
        Pdf,
        DutyDefermentStatement,
        defermentStatementType,
        Some(true),
        Some("BACS"),
        "12345678"
      )

    val excise: DutyDefermentStatementFile = DutyDefermentStatementFile(fileName, downloadUrl, size, metaData(Excise))

    val supplementary: DutyDefermentStatementFile =
      DutyDefermentStatementFile(fileName, downloadUrl, size, metaData(Supplementary))

    val unknown: DutyDefermentStatementFile =
      DutyDefermentStatementFile(fileName, downloadUrl, size, metaData(UnknownStatementType))

    val exciseDeferment: DutyDefermentStatementFile =
      DutyDefermentStatementFile(fileName, downloadUrl, size, metaData(ExciseDeferment))

    val dutyDeferment: DutyDefermentStatementFile =
      DutyDefermentStatementFile(fileName, downloadUrl, size, metaData(DutyDeferment))

    val app: Application        = applicationBuilder().build()
    implicit val msgs: Messages = messages(app)
  }
}
