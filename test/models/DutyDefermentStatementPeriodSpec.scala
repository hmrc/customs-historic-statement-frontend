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
import models.DDStatementType.{DutyDeferment, Excise, ExciseDeferment, Supplementary, UnknownStatementType}
import models.FileFormat.{Pdf, UnknownFileFormat}

class DutyDefermentStatementPeriodSpec extends SpecBase {

  "compare" should {

    "order correctly" in new Setup {
      dutyDefermentStatementPeriodExcise compare dutyDefermentStatementPeriodSupplementary mustBe -1
      dutyDefermentStatementPeriodExcise compare dutyDefermentStatementPeriodExcise2 mustBe 0
    }
  }

  "findStatementFileByFormat" should {

    "return all files for a given file format" in new Setup {
      dutyDefermentStatementPeriodExcise.findStatementFileByFormat(Pdf) mustBe Seq(ddFile1)
      dutyDefermentStatementPeriodExcise.findStatementFileByFormat(UnknownFileFormat) mustBe Seq(ddFile2)
    }
  }

  "DutyDefermentStatementPeriodsByMonth" should {

    "provide the month and year correctly" in {

      val periodsByMonth = DutyDefermentStatementPeriodsByMonth(date, Seq.empty)
      periodsByMonth.month mustBe month
      periodsByMonth.year mustBe year
    }
  }

  "unavailableLinkHiddenText" should {

    "return correct text for ExciseDeferment" in new Setup {
      dutyDefermentStatementPeriodExciseDeferment.unavailableLinkHiddenText(
        Pdf
      ) mustBe "Excise deferment 1920 summary PDF for March 2018 unavailable"
    }

    "return correct text for DutyDeferment" in new Setup {
      dutyDefermentStatementPeriodDutyDeferment.unavailableLinkHiddenText(
        Pdf
      ) mustBe "Duty deferment 1720 summary PDF for March 2018 unavailable"
    }

    "return correct text for Excise" in new Setup {
      dutyDefermentStatementPeriodExcise
        .copy(endDate = endDate)
        .unavailableLinkHiddenText(Pdf) mustBe "Excise summary PDF for March 2018 unavailable"
    }

    "return correct text for Supplementary" in new Setup {
      dutyDefermentStatementPeriodSupplementary.unavailableLinkHiddenText(
        Pdf
      ) mustBe "Supplementary end of month PDF for March 2018 unavailable"
    }

    "return correct text for UnknownStatementType" in new Setup {
      dutyDefermentStatementPeriodUnknown
        .copy(startDate = startDate, endDate = endDate)
        .unavailableLinkHiddenText(Pdf) mustBe "PDF for 14 to 28 March 2018 unavailable"
    }
  }

  trait Setup {
    val size   = 47
    val offset = 10

    val ddFile1: DutyDefermentStatementFile = DutyDefermentStatementFile(
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

    val ddFile2: DutyDefermentStatementFile = DutyDefermentStatementFile(
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
        UnknownFileFormat,
        DutyDefermentStatement,
        Supplementary,
        Some(true),
        Some("BACS"),
        "12345678"
      )
    )

    val dutyDefermentStatementPeriodExcise: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      currentDate,
      currentDate.minusDays(offset),
      currentDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExcise2: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      currentDate,
      currentDate.minusDays(offset),
      currentDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExcise3: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      currentDate,
      currentDate.minusDays(offset),
      currentDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodSupplementary: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Supplementary,
      currentDate,
      currentDate.minusDays(offset),
      endDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodUnknown: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      UnknownStatementType,
      currentDate,
      currentDate.minusDays(offset),
      currentDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExciseDeferment: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      ExciseDeferment,
      currentDate,
      currentDate.minusDays(offset),
      endDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodDutyDeferment: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      DutyDeferment,
      currentDate,
      currentDate.minusDays(offset),
      endDate,
      Seq(ddFile1, ddFile2)
    )
  }
}
