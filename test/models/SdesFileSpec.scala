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
import models.DDStatementType.*
import models.FileFormat.{Csv, Pdf, UnknownFileFormat}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.*
import play.api.libs.json.{JsString, Json}

class SdesFileSpec extends SpecBase with Matchers {

  "an SdesFile" should {
    "be correctly ordered and formatted" in new Setup {

      val one      = 1
      val zero     = 0
      val minusOne = -1

      securityStatementFile2 compare securityStatementFile1 mustBe one
      vatCertificateFile1 compare vatCertificateFile2 mustBe zero
      cashStatementFile1 compare cashStatementFile2 mustBe zero
      postponedVatStatementFile1 compare postponedVatStatementFile2 mustBe zero

      FileFormat.unapply(Pdf).value mustBe "PDF"

      JsString("Pdf").as[FileFormat] mustBe Pdf
      Json.toJson[FileFormat](Pdf) mustBe JsString("PDF")
      Pdf.toString mustBe "PDF"

      DDStatementType.apply("Random") mustBe UnknownStatementType
      DDStatementType.apply("Weekly") mustBe Weekly
      DDStatementType.apply("Supplementary") mustBe Supplementary
      DDStatementType.apply("Excise") mustBe Excise

      unapply(UnknownStatementType).value mustBe "UNKNOWN STATEMENT TYPE"
      unapply(Weekly).value mustBe "Weekly"
      unapply(Supplementary).value mustBe "Supplementary"
      unapply(Excise).value mustBe "Excise"

      Weekly compare Excise mustBe one
      Weekly compare Supplementary mustBe one
      Weekly compare UnknownStatementType mustBe minusOne

      Excise compare Weekly mustBe minusOne
      Excise compare Supplementary mustBe minusOne
      Excise compare UnknownStatementType mustBe minusOne

      Supplementary compare Weekly mustBe minusOne
      Supplementary compare Excise mustBe one
      Supplementary compare UnknownStatementType mustBe minusOne

      DutyDefermentStatement.name mustBe "DutyDefermentStatement"

      List(dutyDefermentFile1, dutyDefermentFile2, dutyDefermentFile3).sorted mustBe
        List(dutyDefermentFile2, dutyDefermentFile3, dutyDefermentFile1)
    }
  }

  trait Setup {

    val year  = 2018
    val year2 = 2019
    val year3 = 19
    val year4 = 2011
    val year5 = 2012

    val month    = 11
    val day      = 27
    val size     = 10
    val fileSize = 12

    val securityStatementFile1 = SecurityStatementFile(
      "file1",
      "/download",
      size,
      SecurityStatementFileMetadata(
        year,
        month,
        day,
        year,
        month,
        day,
        Pdf,
        SecurityStatement,
        "12345678912",
        fileSize,
        "BACS",
        None
      )
    )

    val securityStatementFile2 = SecurityStatementFile(
      "file2",
      "/download",
      size,
      SecurityStatementFileMetadata(
        year2,
        month,
        day,
        year2,
        month,
        day,
        Pdf,
        SecurityStatement,
        "12345678912",
        fileSize,
        "BACS",
        None
      )
    )

    val vatCertificateFile1 = VatCertificateFile(
      "file1",
      "/download",
      size,
      VatCertificateFileMetadata(year3, size, Pdf, C79Certificate, None),
      "123456789"
    )

    val vatCertificateFile2 = VatCertificateFile(
      "file1",
      "/download",
      size,
      VatCertificateFileMetadata(year3, size, Pdf, C79Certificate, None),
      "123456789"
    )

    val cashStatementFile1: CashStatementFile = CashStatementFile(
      "file1",
      "/download",
      size,
      CashStatementFileMetadata(
        periodStartYear = year,
        periodStartMonth = month,
        periodStartDay = day,
        periodEndYear = year,
        periodEndMonth = month,
        periodEndDay = day,
        fileFormat = Csv,
        fileRole = CDSCashAccount,
        cashAccountNumber = None,
        statementRequestId = None
      ),
      "123456789"
    )

    val cashStatementFile2: CashStatementFile = CashStatementFile(
      "file2",
      "/download",
      size,
      CashStatementFileMetadata(
        periodStartYear = year,
        periodStartMonth = month,
        periodStartDay = day,
        periodEndYear = year,
        periodEndMonth = month,
        periodEndDay = day,
        fileFormat = Csv,
        fileRole = CDSCashAccount,
        cashAccountNumber = None,
        statementRequestId = None
      ),
      "123456789"
    )

    val postponedVatStatementFile1 = PostponedVatStatementFile(
      "file1",
      "/download",
      size,
      PostponedVatStatementFileMetadata(year3, size, Pdf, PostponedVATStatement, "CDS", Some("request id")),
      "123456789"
    )

    val postponedVatStatementFile2 = PostponedVatStatementFile(
      "file1",
      "/download",
      size,
      PostponedVatStatementFileMetadata(year3, size, Pdf, PostponedVATStatement, "CDS", Some("request id")),
      "123456789"
    )

    val dutyFileSize = 47

    val dutyDefermentFile1 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://first.com/",
      dutyFileSize,
      DutyDefermentStatementFileMetadata(
        year,
        month,
        day,
        year,
        month,
        day,
        UnknownFileFormat,
        DutyDefermentStatement,
        Weekly,
        Some(true),
        Some("BACS"),
        "12345678"
      )
    )

    val dutyDefermentFile2 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://second.com/",
      dutyFileSize,
      DutyDefermentStatementFileMetadata(
        year4,
        month,
        day,
        year5,
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

    val dutyDefermentFile3 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://second.com/",
      dutyFileSize,
      DutyDefermentStatementFileMetadata(
        year4,
        month,
        day,
        year5,
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

  }
}
