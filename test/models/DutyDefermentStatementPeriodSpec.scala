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

package models

import base.SpecBase
import models.DDStatementType.{Excise, Supplementary, UnknownStatementType}
import models.FileFormat.{Pdf, UnknownFileFormat}
import play.api.test.Helpers

import java.time.LocalDate

class DutyDefermentStatementPeriodSpec extends SpecBase {

  "DutyDefermentStatementPeriod" should {

    "order correctly" in new Setup {
      dutyDefermentStatementPeriodExcise compare dutyDefermentStatementPeriodSupplementary mustBe -1
      dutyDefermentStatementPeriodExcise compare dutyDefermentStatementPeriodExcise2 mustBe 0
    }

    "display the correct unavailable hidden link for Excise" in new Setup {
      dutyDefermentStatementPeriodExcise.unavailableLinkHiddenText(Pdf)(Helpers.stubMessages()) mustBe "cf.account.detail.missing-file-type-excise"
    }

    "display the correct unavailable hidden link for Supplementary" in new Setup {
      dutyDefermentStatementPeriodSupplementary.unavailableLinkHiddenText(Pdf)(Helpers.stubMessages()) mustBe "cf.account.detail.missing-file-type-supplementary"
    }

    "display the correct unavailable hidden link for missing file type" in new Setup {
      dutyDefermentStatementPeriodUnknown.unavailableLinkHiddenText(Pdf)(Helpers.stubMessages()) mustBe "cf.account.detail.missing-file-type"
    }

    "return all files for a given file format" in new Setup {
      dutyDefermentStatementPeriodExcise.findStatementFileByFormat(Pdf) mustBe Seq(ddFile1)
      dutyDefermentStatementPeriodExcise.findStatementFileByFormat(UnknownFileFormat) mustBe Seq(ddFile2)
    }
  }

  "DutyDefermentStatementPeriodsByMonth" should {
    "provide the month and year correctly" in {
      val date = LocalDate.of(2019, 10, 1)

      val periodsByMonth = DutyDefermentStatementPeriodsByMonth(date, Seq.empty)
      periodsByMonth.month mustBe 10
      periodsByMonth.year mustBe 2019
    }
  }


  trait Setup {

    val now = LocalDate.now()

    val ddFile1 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://second.com/",
      47,
      DutyDefermentStatementFileMetadata(
        2011, 11, 27,
        2012, 11, 27,
        Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), "12345678")
    )
    val ddFile2 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://second.com/",
      47,
      DutyDefermentStatementFileMetadata(
        2011, 11, 27,
        2012, 11, 27,
        UnknownFileFormat, DutyDefermentStatement, Supplementary, Some(true), Some("BACS"), "12345678")
    )

    val dutyDefermentStatementPeriodExcise = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      now,
      now.minusDays(10),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExcise2 = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      now,
      now.minusDays(10),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExcise3 = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      now,
      now.minusDays(10),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodSupplementary = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Supplementary,
      now,
      now.minusDays(10),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodUnknown = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      UnknownStatementType,
      now,
      now.minusDays(10),
      now,
      Seq(ddFile1, ddFile2)
    )
  }

}
