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
import models.DDStatementType.{Excise, Weekly}
import models.FileFormat.Pdf
import models._
import play.api.i18n.Messages
import play.api.test.Helpers
import viewmodels.{PostponedVatStatementsByMonth, VatCertificatesByMonth}

import java.time.LocalDate

class SortStatementsServiceSpec extends SpecBase {

  "SortStatementService" should {
    "return requested security statements for EORI" in new Setup {
      val securityStatementsForEori = sortStatementsService.sortSecurityCertificatesForEori(eoriHistory, securityStatementFiles)
      securityStatementsForEori.requestedStatements mustBe requestedSecurityStatements
    }

    "return requested c79 certificates for EORI" in new Setup {
      val vatCertificatesForEori = sortStatementsService.sortVatCertificatesForEori(eoriHistory, c79CertificateFiles)
      vatCertificatesForEori.requestedCertificates mustBe requestedC79Certificates
    }

    "return requested pvat statements for EORI" in new Setup {
      val postponedVatStatementsForEori = sortStatementsService.sortPostponedVatStatementsForEori(eoriHistory, postponedVatStatementFiles)
      postponedVatStatementsForEori.requestedStatements mustBe requestedPVATStatements
    }

    "return requested duty deferment statements for EORI" in new Setup {
      val dutyDefermentStatementsForEori = sortStatementsService.sortDutyDefermentStatementsForEori(eoriHistory, dutyDefermentFiles)
      dutyDefermentStatementsForEori.requestedStatements mustBe requestedDutyDefermentStatements
    }
  }

  trait Setup {
    implicit val messages: Messages = Helpers.stubMessages()

    val someEori = "12345678"
    val someDan = "12345"
    val someRequestId = Some("Ab1234")

    val securityStatementFile = SecurityStatementFile("statementfile_00", "download_url_00", 99L,
      SecurityStatementFileMetadata(2017, 12, 28, 2018, 1, 1, Pdf, SecurityStatement, someEori, 500L, "0000000", None))
    val securityStatementFile_2 = SecurityStatementFile("statementfile_00", "download_url_00", 99L,
      SecurityStatementFileMetadata(2017, 11, 28, 2018, 2, 2, Pdf, SecurityStatement, someEori, 500L, "0000000", None))
    val securityStatementFile_3 = SecurityStatementFile("statementfile_00", "download_url_00", 99L,
      SecurityStatementFileMetadata(2017, 11, 28, 2018, 2, 2, Pdf, SecurityStatement, someEori, 500L, "0000000", someRequestId))
    val securityStatementFiles = List(securityStatementFile, securityStatementFile_2, securityStatementFile_3)

    val requestedSecurityStatements = List(SecurityStatementsByPeriod(LocalDate.of(2017, 11, 28),LocalDate.of(2018,2,2),
      List(SecurityStatementFile("statementfile_00", "download_url_00", 99, SecurityStatementFileMetadata(2017, 11, 28, 2018, 2, 2, Pdf, SecurityStatement, "12345678", 500, "0000000", someRequestId)))))

    val c79Certificates = VatCertificateFile("statementfile_00", "download_url_00", 99L,
      VatCertificateFileMetadata(2017, 12, Pdf, C79Certificate,None))
    val c79Certificates_2 = VatCertificateFile("statementfile_00", "download_url_00", 99L,
      VatCertificateFileMetadata(2017, 11, Pdf, C79Certificate,None))
    val c79Certificates_3 = VatCertificateFile("statementfile_00", "download_url_00", 99L,
      VatCertificateFileMetadata(2017, 12, Pdf, C79Certificate,someRequestId))
    val c79Certificates_4 = VatCertificateFile("statementfile_00", "download_url_00", 99L,
      VatCertificateFileMetadata(2017, 11, Pdf, C79Certificate,someRequestId))
    val c79CertificateFiles = Seq(c79Certificates, c79Certificates_2, c79Certificates_3, c79Certificates_4)

    val requestedC79Certificates = List(VatCertificatesByMonth(LocalDate.of(2017,12,1),
      List(VatCertificateFile("statementfile_00", "download_url_00", 99,
        VatCertificateFileMetadata(2017, 12, Pdf, C79Certificate, Some("Ab1234")), ""))),
      VatCertificatesByMonth(LocalDate.of(2017,11,1),
        List(VatCertificateFile("statementfile_00", "download_url_00", 99,
          VatCertificateFileMetadata(2017, 11, Pdf, C79Certificate, Some("Ab1234")), ""))))

    val postponedVatStatement = PostponedVatStatementFile("statementfile_00", "download_url_00", 99L,
      PostponedVatStatementFileMetadata(2017, 12, Pdf, PostponedVATStatement, "CDS", None))
    val postponedVatStatement_2 = PostponedVatStatementFile("statementfile_00", "download_url_00", 99L,
      PostponedVatStatementFileMetadata(2017, 11, Pdf, PostponedVATStatement, "Chief", someRequestId))
    val postponedVatStatement_3 = PostponedVatStatementFile("statementfile_00", "download_url_00", 99L,
      PostponedVatStatementFileMetadata(2017, 12, Pdf, PostponedVATStatement, "CDS", someRequestId))
    val postponedVatStatementFiles = Seq(postponedVatStatement, postponedVatStatement_2, postponedVatStatement_3)

    val requestedPVATStatements = List(PostponedVatStatementsByMonth(LocalDate.of(2017,12,1),
      List(PostponedVatStatementFile("statementfile_00", "download_url_00", 99,
        PostponedVatStatementFileMetadata(2017, 12, Pdf, PostponedVATStatement, "CDS", Some("Ab1234")), ""))),
      PostponedVatStatementsByMonth(LocalDate.of(2017,11,1),
        List(PostponedVatStatementFile("statementfile_00", "download_url_00", 99,
          PostponedVatStatementFileMetadata(2017, 11, Pdf, PostponedVATStatement, "Chief", Some("Ab1234")), ""))))

    val dutyDeferementFile: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_03_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 3, 1, 2018, 3, 8, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), someDan, someRequestId))
    val dutyDeferementFile_2: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_03_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 3, 1, 2018, 3, 8, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), someDan, None))
    val dutyDeferementFile_3: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 1, 1, 2018, 1, 8, Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), someDan, someRequestId))
    val dutyDeferementFile_4: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 1, 1, 2018, 1, 8, Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), someDan, None))
    val dutyDefermentFiles = Seq(dutyDeferementFile, dutyDeferementFile_2, dutyDeferementFile_3, dutyDeferementFile_4)

    val requestedDutyDefermentStatements = List(DutyDefermentStatementFile("2018_03_01-08.pdf", "url.pdf", 1024,
      DutyDefermentStatementFileMetadata(2018, 3, 1, 2018, 3, 8, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), "12345", someRequestId)),
      DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", 1024,
        DutyDefermentStatementFileMetadata(2018, 1, 1, 2018, 1, 8, Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), "12345", someRequestId)))

    val eoriHistory = EoriHistory("eori1", Some(LocalDate.now()), Some(LocalDate.now()))

    val sortStatementsService = new SortStatementsService()
  }
}
