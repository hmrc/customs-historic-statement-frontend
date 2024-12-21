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
import helpers.Formatters
import models.DDStatementType.{DutyDeferment, Excise, ExciseDeferment, Supplementary, UnknownStatementType}
import models.FileFormat.{Pdf, UnknownFileFormat}
import play.api.Application
import play.api.i18n.Messages
import play.api.test.Helpers

import java.time.LocalDate

class DutyDefermentStatementPeriodSpec extends SpecBase {

  "DutyDefermentStatementPeriod" should {

    "order correctly" in new Setup {
      dutyDefermentStatementPeriodExcise compare dutyDefermentStatementPeriodSupplementary mustBe -1
      dutyDefermentStatementPeriodExcise compare dutyDefermentStatementPeriodExcise2 mustBe 0
    }

    "display the correct unavailable hidden link for Excise" in new Setup {
      dutyDefermentStatementPeriodExcise.unavailableLinkHiddenText(Pdf)(Helpers.stubMessages()) mustBe
        "cf.account.detail.missing-file-type-excise"
    }

    "display the correct unavailable hidden link for Supplementary" in new Setup {
      dutyDefermentStatementPeriodSupplementary.unavailableLinkHiddenText(Pdf)(Helpers.stubMessages()) mustBe
        "cf.account.detail.missing-file-type-supplementary"
    }

    "display the correct unavailable hidden link for missing file type" in new Setup {
      dutyDefermentStatementPeriodUnknown.unavailableLinkHiddenText(Pdf)(Helpers.stubMessages()) mustBe
        "cf.account.detail.missing-file-type"
    }

    "return all files for a given file format" in new Setup {
      dutyDefermentStatementPeriodExcise.findStatementFileByFormat(Pdf) mustBe Seq(ddFile1)
      dutyDefermentStatementPeriodExcise.findStatementFileByFormat(UnknownFileFormat) mustBe Seq(ddFile2)
    }
  }

  "DutyDefermentStatementPeriodsByMonth" should {
    "provide the month and year correctly" in {

      val year  = 2019
      val month = 10
      val day   = 1

      val date = LocalDate.of(year, month, day)

      val periodsByMonth = DutyDefermentStatementPeriodsByMonth(date, Seq.empty)
      periodsByMonth.month mustBe month
      periodsByMonth.year mustBe year
    }
  }

  "unavailableLinkHiddenText" should {

    "return correct text for defermentStatementType" in new Setup {
      val app: Application        = applicationBuilder().build()
      implicit val msgs: Messages = messages(app)

      dutyDefermentStatementPeriodExciseDeferment.unavailableLinkHiddenText(Pdf) mustBe msgs(
        "cf.account.detail.missing-file-type-excise-deferment",
        Pdf,
        Formatters.dateAsMonthAndYear(endDate)
      )

      dutyDefermentStatementPeriodDutyDeferment.unavailableLinkHiddenText(Pdf) mustBe msgs(
        "cf.account.detail.missing-file-type-duty-deferment",
        Pdf,
        Formatters.dateAsMonthAndYear(endDate)
      )

      dutyDefermentStatementPeriodExcise.copy(endDate = endDate).unavailableLinkHiddenText(Pdf) mustBe msgs(
        "cf.account.detail.missing-file-type-excise",
        Pdf,
        Formatters.dateAsMonthAndYear(endDate)
      )

      dutyDefermentStatementPeriodSupplementary.unavailableLinkHiddenText(Pdf) mustBe msgs(
        "cf.account.detail.missing-file-type-supplementary",
        Pdf,
        Formatters.dateAsMonthAndYear(endDate)
      )

      dutyDefermentStatementPeriodUnknown
        .copy(startDate = startDate, endDate = endDate)
        .unavailableLinkHiddenText(Pdf) mustBe "PDF for 10 to 27 November 2011 unavailable"
    }
  }

  trait Setup {

    val now: LocalDate = LocalDate.now()

    val size   = 47
    val offset = 10

    val month = 11
    val day   = 27
    val day10 = 10
    val year1 = 2011
    val year2 = 2012

    val startDate: LocalDate = LocalDate.of(year1, month, day10)
    val endDate: LocalDate   = LocalDate.of(year1, month, day)

    val ddFile1: DutyDefermentStatementFile = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://second.com/",
      size,
      DutyDefermentStatementFileMetadata(
        year1,
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
        year1,
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
      now,
      now.minusDays(offset),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExcise2: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      now,
      now.minusDays(offset),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExcise3: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Excise,
      now,
      now.minusDays(offset),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodSupplementary: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      Supplementary,
      now,
      now.minusDays(offset),
      endDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodUnknown: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      UnknownStatementType,
      now,
      now.minusDays(offset),
      now,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodExciseDeferment: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      ExciseDeferment,
      now,
      now.minusDays(offset),
      endDate,
      Seq(ddFile1, ddFile2)
    )

    val dutyDefermentStatementPeriodDutyDeferment: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      DutyDeferment,
      now,
      now.minusDays(offset),
      endDate,
      Seq(ddFile1, ddFile2)
    )
  }
}
