/*
 * Copyright 2024 HM Revenue & Customs
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

package services

import base.SpecBase
import models.*
import utils.Utils.emptyString

class SdesGatekeeperServiceSpec extends SpecBase {

  "SdesGatekeeperService" should {

    "convert FileInformation to CashStatementFile correctly" in new Setup {
      val sdesFileInformation: FileInformation = validCashStatementFileInformation

      val result: CashStatementFile = sdesGatekeeperService.convertToCashStatementFile(sdesFileInformation)

      result.filename mustBe sdesFileInformation.filename
      result.downloadURL mustBe sdesFileInformation.downloadURL
      result.metadata.periodStartYear mustBe periodStartYear
      result.metadata.periodStartMonth mustBe periodStartMonth
      result.metadata.periodStartDay mustBe periodStartDay
      result.metadata.periodEndYear mustBe periodEndYear
      result.metadata.periodEndMonth mustBe periodEndMonth
      result.metadata.periodEndDay mustBe periodEndDay
      result.metadata.fileFormat mustBe FileFormat(csv)
      result.metadata.fileRole mustBe CashStatement
      result.metadata.cashAccountNumber mustBe someAccountNumber
      result.metadata.statementRequestId mustBe someRequestId
    }

    "convert FileInformation to VatCertificateFile correctly" in new Setup {
      val sdesFileInformation: FileInformation = validVatCertificateFileInformation

      val result: VatCertificateFile = sdesGatekeeperService.convertToVatCertificateFile(sdesFileInformation)

      result.metadata.fileRole mustBe C79Certificate
    }

    "convert FileInformation to PostponedVatStatementFile correctly" in new Setup {
      val sdesFileInformation: FileInformation = validPostponedVatStatementFileInformation

      val result: PostponedVatStatementFile =
        sdesGatekeeperService.convertToPostponedVatStatementFile(sdesFileInformation)

      result.metadata.fileRole mustBe PostponedVATStatement
      result.metadata.source mustBe "CDS"
    }

    "convert FileInformation to SecurityStatementFile correctly" in new Setup {
      val sdesFileInformation: FileInformation = validSecurityStatementFileInformation

      val result: SecurityStatementFile = sdesGatekeeperService.convertToSecurityStatementFile(sdesFileInformation)

      result.metadata.fileRole mustBe SecurityStatement
      result.metadata.checksum mustBe "MISSING CHECKSUM"
    }

    "convert FileInformation to DutyDefermentStatementFile correctly" in new Setup {
      val sdesFileInformation: FileInformation = validDutyDefermentStatementFileInformation

      val result: DutyDefermentStatementFile =
        sdesGatekeeperService.convertToDutyDefermentStatementFile(sdesFileInformation)

      result.metadata.fileRole mustBe DutyDefermentStatement
      result.metadata.defermentStatementType mustBe DDStatementType("DefermentStatement")
      result.metadata.dutyOverLimit mustBe Some(false)
      result.metadata.dutyPaymentType mustBe Some("Unknown")
      result.metadata.dan mustBe "Unknown"
    }
  }

  trait Setup {
    val sdesGatekeeperService = new SdesGatekeeperService()
    val periodStartYear = 2017
    val periodStartMonth = 11
    val periodStartDay = 1
    val periodEndYear = 2017
    val periodEndMonth = 11
    val periodEndDay = 8
    val fileSize = 500L
    val someAccountNumber: Option[String] = Some("123456789")
    val someRequestId: Option[String] = Some("Ab1234")
    val csv = "csv"
    val fileName = "test-file.csv"
    val downloadURL = "test-file.csv"

    val validCashStatementFileInformation: FileInformation = FileInformation(
      filename = fileName,
      downloadURL = downloadURL,
      fileSize = fileSize,
      metadata = Metadata(Seq(
        MetadataItem("PeriodStartYear", periodStartYear.toString),
        MetadataItem("PeriodStartMonth", periodStartMonth.toString),
        MetadataItem("PeriodStartDay", periodStartDay.toString),
        MetadataItem("PeriodEndYear", periodEndYear.toString),
        MetadataItem("PeriodEndMonth", periodEndMonth.toString),
        MetadataItem("PeriodEndDay", periodEndDay.toString),
        MetadataItem("FileType", csv),
        MetadataItem("FileRole", "CashStatement"),
        MetadataItem("CashAccountNumber", someAccountNumber.getOrElse(emptyString)),
        MetadataItem("statementRequestID", someRequestId.getOrElse(emptyString))
      ))
    )

    val validVatCertificateFileInformation: FileInformation = FileInformation(
      filename = fileName,
      downloadURL = downloadURL,
      fileSize = fileSize,
      metadata = Metadata(Seq(
        MetadataItem("PeriodStartYear", periodStartYear.toString),
        MetadataItem("PeriodStartMonth", periodStartMonth.toString),
        MetadataItem("FileType", csv),
        MetadataItem("FileRole", "C79Certificate"),
        MetadataItem("statementRequestID", someRequestId.getOrElse(emptyString))
      ))
    )

    val validPostponedVatStatementFileInformation: FileInformation = FileInformation(
      filename = fileName,
      downloadURL = downloadURL,
      fileSize = fileSize,
      metadata = Metadata(Seq(
        MetadataItem("PeriodStartYear", periodStartYear.toString),
        MetadataItem("PeriodStartMonth", periodStartMonth.toString),
        MetadataItem("FileType", csv),
        MetadataItem("FileRole", "PostponedVATStatement"),
        MetadataItem("DutyPaymentMethod", "CDS"),
        MetadataItem("statementRequestID", someRequestId.getOrElse(emptyString))
      ))
    )

    val validSecurityStatementFileInformation: FileInformation = FileInformation(
      filename = fileName,
      downloadURL = downloadURL,
      fileSize = fileSize,
      metadata = Metadata(Seq(
        MetadataItem("PeriodStartYear", periodStartYear.toString),
        MetadataItem("PeriodStartMonth", periodStartMonth.toString),
        MetadataItem("PeriodStartDay", periodStartDay.toString),
        MetadataItem("PeriodEndYear", periodEndYear.toString),
        MetadataItem("PeriodEndMonth", periodEndMonth.toString),
        MetadataItem("PeriodEndDay", periodEndDay.toString),
        MetadataItem("FileType", csv),
        MetadataItem("FileRole", "SecurityStatement"),
        MetadataItem("eoriNumber", "MISSING EORI NUMBER"),
        MetadataItem("fileSize", fileSize.toString),
        MetadataItem("checksum", "MISSING CHECKSUM"),
        MetadataItem("statementRequestID", someRequestId.getOrElse(emptyString))
      ))
    )

    val validDutyDefermentStatementFileInformation: FileInformation = FileInformation(
      filename = fileName,
      downloadURL = downloadURL,
      fileSize = fileSize,
      metadata = Metadata(Seq(
        MetadataItem("PeriodStartYear", periodStartYear.toString),
        MetadataItem("PeriodStartMonth", periodStartMonth.toString),
        MetadataItem("PeriodStartDay", periodStartDay.toString),
        MetadataItem("PeriodEndYear", periodEndYear.toString),
        MetadataItem("PeriodEndMonth", periodEndMonth.toString),
        MetadataItem("PeriodEndDay", periodEndDay.toString),
        MetadataItem("FileType", csv),
        MetadataItem("FileRole", "DutyDefermentStatement"),
        MetadataItem("DefermentStatementType", "DefermentStatement"),
        MetadataItem("DutyOverLimit", "false"),
        MetadataItem("DutyPaymentType", "Unknown"),
        MetadataItem("DAN", "Unknown"),
        MetadataItem("statementRequestID", someRequestId.getOrElse(emptyString))
      ))
    )
  }
}
