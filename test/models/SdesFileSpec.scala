/*
 * Copyright 2021 HM Revenue & Customs
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
import models.DDStatementType._
import models.FileFormat.{Pdf, UnknownFileFormat}
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers._
import play.api.libs.json.{JsString, Json}

class SdesFileSpec extends SpecBase with Matchers {

  "an SdesFile" should {
    "be correctly ordered and formatted" in new Setup {

      securityStatementFile2 compare securityStatementFile1 mustBe 1
      vatCertificateFile1 compare vatCertificateFile2 mustBe 0
      postponedVatStatementFile1 compare postponedVatStatementFile2 mustBe 0

      FileFormat.unapply(Pdf).value mustBe "PDF"

      JsString("Pdf").as[FileFormat] mustBe Pdf
      Json.toJson(Pdf)(FileFormat.fileFormatFormat.writes) mustBe JsString("PDF")
      Pdf.toString mustBe "PDF"

      DDStatementType.apply("Random") mustBe UnknownStatementType
      DDStatementType.apply("Weekly") mustBe Weekly
      DDStatementType.apply("Supplementary") mustBe Supplementary
      DDStatementType.apply("Excise") mustBe Excise

      unapply(UnknownStatementType).value mustBe "UNKNOWN STATEMENT TYPE"
      unapply(Weekly).value mustBe "Weekly"
      unapply(Supplementary).value mustBe "Supplementary"
      unapply(Excise).value mustBe "Excise"

      Weekly compare Excise mustBe 1
      Weekly compare Supplementary mustBe 1
      Weekly compare UnknownStatementType mustBe -1

      Excise compare Weekly mustBe -1
      Excise compare Supplementary mustBe -1
      Excise compare UnknownStatementType mustBe -1

      Supplementary compare Weekly mustBe -1
      Supplementary compare Excise mustBe 1
      Supplementary compare UnknownStatementType mustBe -1

      DutyDefermentStatement.name mustBe "DutyDefermentStatement"

      List(dutyDefermentFile1, dutyDefermentFile2, dutyDefermentFile3).sorted mustBe List(dutyDefermentFile2, dutyDefermentFile3, dutyDefermentFile1)
    }
  }

  trait Setup {
    val securityStatementFile1 = SecurityStatementFile("file1", "/download", 10, SecurityStatementFileMetadata(
      2018, 11, 27,
      2018, 11, 27,
      Pdf, SecurityStatement, "12345678912", 12, "BACS", None)
    )

    val securityStatementFile2 = SecurityStatementFile("file2", "/download", 10, SecurityStatementFileMetadata(
      2019, 11, 27,
      2019, 11, 27,
      Pdf, SecurityStatement, "12345678912", 12, "BACS", None)
    )

    val vatCertificateFile1 = VatCertificateFile("file1", "/download", 10, VatCertificateFileMetadata(
      19, 10, Pdf, C79Certificate, None
    ), "123456789")

    val vatCertificateFile2 = VatCertificateFile("file1", "/download", 10, VatCertificateFileMetadata(
      19, 10, Pdf, C79Certificate, None
    ), "123456789")

    val postponedVatStatementFile1 = PostponedVatStatementFile("file1", "/download", 10, PostponedVatStatementFileMetadata(
      19, 10, Pdf, PostponedVATStatement, "CDS", Some("request id")
    ), "123456789")

    val postponedVatStatementFile2 = PostponedVatStatementFile("file1", "/download", 10, PostponedVatStatementFileMetadata(
      19, 10, Pdf, PostponedVATStatement, "CDS", Some("request id")
    ), "123456789")


    val dutyDefermentFile1 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://first.com/",
      47,
      DutyDefermentStatementFileMetadata(
        2018, 11, 27,
        2018, 11, 27,
        UnknownFileFormat, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), "12345678")
    )

    val dutyDefermentFile2 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://second.com/",
      47,
      DutyDefermentStatementFileMetadata(
        2011, 11, 27,
        2012, 11, 27,
        Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), "12345678")
    )

    val dutyDefermentFile3 = DutyDefermentStatementFile(
      s"12345678.123",
      s"http://second.com/",
      47,
      DutyDefermentStatementFileMetadata(
        2011, 11, 27,
        2012, 11, 27,
        Pdf, DutyDefermentStatement, Supplementary, Some(true), Some("BACS"), "12345678")
    )
  }
}
