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
import models.DDStatementType.{DutyDeferment, ExciseDeferment, Supplementary, Weekly}
import models.FileFormat.{Pdf, UnknownFileFormat}
import models.{
  DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata, DutyDefermentStatementPeriod,
  DutyDefermentStatementPeriodsByMonth, DutyDefermentStatementsForEori, EoriHistory
}
import utils.Utils.{h2Component, h3Component, missingDocumentsGuidanceComponent}
import helpers.Formatters

import java.time.LocalDate

class DutyDefermentAccountViewModelSpec extends SpecBase {

  "DutyDefermentAccountComponent.apply" should {

    "create object with correct accountHeading" in new Setup {
      ddAccountComponent.accountHeading mustBe h2Component(
        msg = "cf.account.detail.requested.deferment-account-secondary-heading.NiAccount",
        id = Some("eori-heading"),
        classes = "govuk-caption-xl",
        extraContent = Some(accountNumber)
      )
    }

    "create object with correct eoriHeading" in new Setup {
      ddAccountComponent.eoriHeading mustBe h2Component(
        id = Some(s"historic-eori-0"),
        classes = "govuk-heading-s",
        msg = messages("cf.account.details.previous-eori", eori)
      )
    }

    "create object with correct monthHeading" in new Setup {
      ddAccountComponent.monthHeading mustBe h3Component(
        id = Some(
          s"requested-statements-month-heading-" +
            s"${ddAccountStatement.historyIndex}-" +
            s"${ddAccountStatement.group.year}-${ddAccountStatement.group.month}"
        ),
        msg = Formatters.dateAsMonthAndYear(ddAccountStatement.group.monthAndYear)
      )

    }

    "create object with correct statements" ignore new Setup {
      val statementsString: String = ddAccountComponent.statements.body

      statementsString must include("<dl  class=govuk-summary-list>")
      statementsString must include(
        "<div id=requested-statements-list-0-2025-1-row-0 class=govuk-summary-list__row>"
      )

      statementsString must include(
        "<dt id=requested-statements-list-0-2025-1-row-0-date-cell class=govuk-summary-list__value>Duty deferment 1720</dt>"
      )
    }

    "create object with correct missingDocumentsGuidance" in new Setup {
      ddAccountComponent.missingDocumentsGuidance mustBe missingDocumentsGuidanceComponent("statement")
    }
  }

  trait Setup {
    val accountNumber: String = "123456"
    val eori: String          = "12345678"

    private val size                      = 1024L
    private val requestId: Option[String] = Some("Ab1234")
    private val currentDate: LocalDate    = LocalDate.now()
    private val offset                    = 10
    private val month                     = 11
    private val localDateDay              = 10
    private val day                       = 27
    private val year1                     = 2011
    private val year2                     = 2012
    private val pdfFileName               = "2018_03_01-08.pdf"
    private val pdfUrl                    = "url.pdf"
    private val pdfSize                   = 1024L
    private val periodStartYear           = 2018
    private val periodStartMonth          = 3
    private val periodStartMonth2         = 2
    private val periodStartDay            = 1
    private val periodEndYear             = 2018
    private val periodEndMonth2           = 3
    private val periodEndMonth            = 2
    private val periodEndDay              = 8
    private val dutyPaymentType           = "BACS"

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
        DutyDeferment,
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

    val ddFile3: DutyDefermentStatementFile = DutyDefermentStatementFile(
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
        DutyDeferment,
        Some(true),
        Some("BACS"),
        "12345678"
      )
    )

    val ddFile4: DutyDefermentStatementFile = DutyDefermentStatementFile(
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
        ExciseDeferment,
        Some(true),
        Some("BACS"),
        "12345678"
      )
    )

    val ddStatementPeriod: DutyDefermentStatementPeriod = DutyDefermentStatementPeriod(
      DutyDefermentStatement,
      DutyDeferment,
      currentDate,
      currentDate.minusDays(offset),
      currentDate,
      Seq(ddFile1, ddFile2, ddFile3, ddFile4)
    )

    private val eoriHistory = EoriHistory(
      eori,
      Some(LocalDate.of(year1, month, localDateDay)),
      Some(LocalDate.of(year1, month, localDateDay))
    )

    private val dutyDefermentFile: DutyDefermentStatementFile =
      DutyDefermentStatementFile(
        pdfFileName,
        pdfUrl,
        pdfSize,
        DutyDefermentStatementFileMetadata(
          periodStartYear,
          periodStartMonth,
          periodStartDay,
          periodEndYear,
          periodEndMonth,
          periodEndDay,
          Pdf,
          DutyDefermentStatement,
          Weekly,
          Some(true),
          Some(dutyPaymentType),
          accountNumber,
          requestId
        )
      )

    private val dutyDefermentFile_2: DutyDefermentStatementFile = DutyDefermentStatementFile(
      pdfFileName,
      pdfUrl,
      pdfSize,
      DutyDefermentStatementFileMetadata(
        periodStartYear,
        periodStartMonth2,
        periodStartDay,
        periodEndYear,
        periodEndMonth2,
        periodEndDay,
        Pdf,
        DutyDefermentStatement,
        Supplementary,
        Some(true),
        Some(dutyPaymentType),
        accountNumber,
        requestId
      )
    )

    val dutyDefermentStatementsForEori: DutyDefermentStatementsForEori =
      DutyDefermentStatementsForEori(eoriHistory, Seq(dutyDefermentFile), Seq(dutyDefermentFile_2))

    val ddAccountStatement: DutyDefermentAccountStatement = DutyDefermentAccountStatement(
      historyIndex = 0,
      groupIndex = 0,
      eorisStatements = Seq(dutyDefermentStatementsForEori),
      group = DutyDefermentStatementPeriodsByMonth(
        monthAndYear = ddStatementPeriod.monthAndYear,
        periods = Seq(ddStatementPeriod)
      ),
      periodIndex = 0,
      period = ddStatementPeriod,
      periodsWithIndex = Seq((ddStatementPeriod, 0)),
      isNiAccount = true,
      accountNumber = accountNumber
    )

    val ddAccountComponent: DutyDefermentAccountComponent = DutyDefermentAccountComponent(ddAccountStatement)
  }
}
