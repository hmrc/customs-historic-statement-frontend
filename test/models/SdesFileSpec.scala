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

  "DDStatementType.apply" should {
    "produce correct object" in new Setup {

      DDStatementType("DD1920") mustBe ExciseDeferment
      DDStatementType("DD1720") mustBe DutyDeferment
      DDStatementType("Random") mustBe UnknownStatementType
      DDStatementType("Weekly") mustBe Weekly
      DDStatementType("Supplementary") mustBe Supplementary
      DDStatementType("Excise") mustBe Excise
    }
  }

  "DDStatementType.unapply" should {
    "produce correct output" in new Setup {

      unapply(ExciseDeferment).value mustBe "DD1920"
      unapply(DutyDeferment).value mustBe "DD1720"
      unapply(UnknownStatementType).value mustBe "UNKNOWN STATEMENT TYPE"
      unapply(Weekly).value mustBe "Weekly"
      unapply(Supplementary).value mustBe "Supplementary"
      unapply(Excise).value mustBe "Excise"
    }
  }

  "DDStatementType.compare" should {
    "order the different types in correct order" in {

      List(UnknownStatementType, Weekly, Excise, ExciseDeferment, DutyDeferment, Supplementary).sorted mustBe List(
        ExciseDeferment,
        DutyDeferment,
        Excise,
        Supplementary,
        Weekly,
        UnknownStatementType
      )
    }
  }

  "FileFormat.apply" should {
    "create correct object" in new Setup {

      FileFormat("PDF") mustBe Pdf
      FileFormat("CSV") mustBe Csv
      FileFormat("UNKNOWN FILE FORMAT") mustBe UnknownFileFormat
      FileFormat("abc") mustBe UnknownFileFormat
    }
  }

  "FileFormat.unapply" should {
    "produce correct output" in new Setup {

      FileFormat.unapply(Pdf).value mustBe "PDF"
      FileFormat.unapply(Csv).value mustBe "CSV"
      FileFormat.unapply(UnknownFileFormat).value mustBe "UNKNOWN FILE FORMAT"
    }
  }

  "FileFormat.reads" should {
    "create correct object" in {

      JsString("Pdf").as[FileFormat] mustBe Pdf
      JsString("PDF").as[FileFormat] mustBe Pdf
      JsString("Csv").as[FileFormat] mustBe Csv
      JsString("CSV").as[FileFormat] mustBe Csv
    }
  }

  "FileFormat.writes" should {
    "create correct object" in {

      Json.toJson[FileFormat](Pdf) mustBe JsString("PDF")
      Json.toJson[FileFormat](Csv) mustBe JsString("CSV")
    }
  }

  "SecurityStatementFile.compare" should {
    "correctly order" in new Setup {

      securityStatementFile2 compare securityStatementFile1 mustBe 1
      vatCertificateFile1 compare vatCertificateFile2 mustBe 0
      cashStatementFile1 compare cashStatementFile2 mustBe 0
      postponedVatStatementFile1 compare postponedVatStatementFile2 mustBe 0

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

    val securityStatementFile1: SecurityStatementFile = SecurityStatementFile(
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

    val securityStatementFile2: SecurityStatementFile = SecurityStatementFile(
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

    val vatCertificateFile1: VatCertificateFile = VatCertificateFile(
      "file1",
      "/download",
      size,
      VatCertificateFileMetadata(year3, size, Pdf, C79Certificate, None),
      "123456789"
    )

    val vatCertificateFile2: VatCertificateFile = VatCertificateFile(
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
