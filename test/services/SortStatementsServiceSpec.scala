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
import models.FileFormat.{Csv, Pdf}
import models.*
import play.api.i18n.Messages
import play.api.test.Helpers
import viewmodels.*
import utils.Utils.emptyString

import java.time.{LocalDate, YearMonth}

class SortStatementsServiceSpec extends SpecBase {

  "SortStatementService" should {
    "return requested security statements for EORI" in new Setup {
      val securityStatementsForEori: SecurityStatementsForEori =
        sortStatementsService.sortSecurityCertificatesForEori(eoriHistory, securityStatementFiles)

      securityStatementsForEori.requestedStatements mustBe requestedSecurityStatements
    }

    "return requested cash statements for EORI" in new Setup {
      val cashStatementsForEori: CashStatementForEori =
        sortStatementsService.sortCashStatementsForEori(eoriHistory, cashStatementFiles)

      cashStatementsForEori.requestedStatements mustBe requestedCashStatements
    }

    "return requested c79 certificates for EORI" in new Setup {
      val vatCertificatesForEori: VatCertificatesForEori =
        sortStatementsService.sortVatCertificatesForEori(eoriHistory, c79CertificateFiles)

      vatCertificatesForEori.requestedCertificates mustBe requestedC79Certificates
    }

    "return requested pvat statements for EORI" in new Setup {
      val postponedVatStatementsForEori: PostponedVatStatementsForEori =
        sortStatementsService.sortPostponedVatStatementsForEori(eoriHistory, postponedVatStatementFiles)

      postponedVatStatementsForEori.requestedStatements mustBe requestedPVATStatements
    }

    "return requested duty deferment statements for EORI" in new Setup {
      val dutyDefermentStatementsForEori: DutyDefermentStatementsForEori =
        sortStatementsService.sortDutyDefermentStatementsForEori(eoriHistory, dutyDefermentFiles)

      dutyDefermentStatementsForEori.requestedStatements mustBe requestedDutyDefermentStatements
    }
  }

  trait Setup {
    implicit val messages: Messages = Helpers.stubMessages()

    val someEori = "12345678"
    val someDan = "12345"
    val someRequestId = Some("Ab1234")
    val periodStartYear = 2017
    val periodStartMonth = 11
    val periodStartMonth_2 = 10
    val periodStartDay = 1
    val periodEndYear = 2017
    val periodEndMonth = 11
    val periodEndDay = 8
    val fileSize = 500L
    val size = 99L
    val someAccountNumber: Option[String] = Some("123456789")

    val cashStatementFilePdf: CashStatementFile = CashStatementFile(
      "file1",
      "/download1",
      size,
      CashStatementFileMetadata(
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        periodEndYear,
        periodEndMonth,
        periodEndDay,
        Pdf,
        CDSCashAccount,
        someAccountNumber,
        None
      ), someEori)

    val cashStatementFileCsv: CashStatementFile = CashStatementFile(
      "file2",
      "/download2",
      size,
      CashStatementFileMetadata(
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        periodEndYear,
        periodEndMonth,
        periodEndDay,
        Csv,
        CDSCashAccount,
        someAccountNumber,
        None
      ), someEori)

    val cashStatementFilePdf_2: CashStatementFile = CashStatementFile(
      "file3",
      "/download3",
      size,
      CashStatementFileMetadata(
        periodStartYear - 1,
        periodStartMonth,
        periodStartDay,
        periodEndYear - 1,
        periodEndMonth,
        periodEndDay,
        Pdf,
        CDSCashAccount,
        someAccountNumber,
        someRequestId
      ), someEori)

    val requestedCashStatements: Seq[CashStatementMonthToMonth] = List(
      CashStatementMonthToMonth(LocalDate.of(periodStartYear, periodStartMonth, periodStartDay),
        YearMonth.of(periodEndYear, periodEndMonth).atEndOfMonth(),
        Seq(cashStatementFilePdf, cashStatementFileCsv))())

    val cashStatementFiles: Seq[CashStatementFile] =
      Seq(cashStatementFilePdf, cashStatementFileCsv, cashStatementFilePdf_2)

    val securityStatementFile: SecurityStatementFile = SecurityStatementFile("statementfile_00", "download_url_00",
      size, SecurityStatementFileMetadata(periodStartYear, periodStartMonth, periodStartDay, periodEndYear,
        periodEndMonth, periodEndDay, Pdf, SecurityStatement, someEori, fileSize, "0000000", None))

    val securityStatementFile_2: SecurityStatementFile = SecurityStatementFile("statementfile_00", "download_url_00",
      size, SecurityStatementFileMetadata(periodStartYear, periodStartMonth, periodStartDay, periodEndYear,
        periodEndMonth, periodEndDay, Pdf, SecurityStatement, someEori, fileSize, "0000000", None))

    val securityStatementFile_3: SecurityStatementFile = SecurityStatementFile("statementfile_00", "download_url_00",
      size, SecurityStatementFileMetadata(periodStartYear, periodStartMonth, periodStartDay, periodEndYear,
        periodEndMonth, periodEndDay, Pdf, SecurityStatement, someEori, fileSize, "0000000", someRequestId))

    val securityStatementFiles = List(securityStatementFile, securityStatementFile_2, securityStatementFile_3)

    val requestedSecurityStatements =
      List(SecurityStatementsByPeriod(LocalDate.of(periodStartYear, periodStartMonth, periodStartDay),
        LocalDate.of(periodEndYear, periodEndMonth, periodEndDay),
        List(SecurityStatementFile("statementfile_00", "download_url_00", size,
          SecurityStatementFileMetadata(periodStartYear, periodStartMonth, periodStartDay, periodEndYear,
            periodEndMonth, periodEndDay, Pdf, SecurityStatement, "12345678", fileSize, "0000000", someRequestId)))))

    val c79Certificates: VatCertificateFile = VatCertificateFile("statementfile_00", "download_url_00", size,
      VatCertificateFileMetadata(periodStartYear, periodStartMonth, Pdf, C79Certificate, None))

    val c79Certificates_2: VatCertificateFile = VatCertificateFile("statementfile_00", "download_url_00", size,
      VatCertificateFileMetadata(periodStartYear, periodStartMonth_2, Pdf, C79Certificate, None))

    val c79Certificates_3: VatCertificateFile = VatCertificateFile("statementfile_00", "download_url_00", size,
      VatCertificateFileMetadata(periodStartYear, periodStartMonth, Pdf, C79Certificate, someRequestId))

    val c79Certificates_4: VatCertificateFile = VatCertificateFile("statementfile_00", "download_url_00", size,
      VatCertificateFileMetadata(periodStartYear, periodStartMonth_2, Pdf, C79Certificate, someRequestId))

    val c79CertificateFiles = Seq(c79Certificates, c79Certificates_2, c79Certificates_3, c79Certificates_4)

    val requestedC79Certificates = List(
      VatCertificatesByMonth(LocalDate.of(periodStartYear, periodStartMonth, periodStartDay),
        List(VatCertificateFile("statementfile_00", "download_url_00", size,
          VatCertificateFileMetadata(periodStartYear, periodStartMonth, Pdf,
            C79Certificate, someRequestId), emptyString))),
      VatCertificatesByMonth(LocalDate.of(periodStartYear, periodStartMonth_2, periodStartDay),
        List(VatCertificateFile("statementfile_00", "download_url_00", size,
          VatCertificateFileMetadata(periodStartYear, periodStartMonth_2, Pdf,
            C79Certificate, someRequestId), emptyString))))

    val postponedVatStatement: PostponedVatStatementFile =
      PostponedVatStatementFile("statementfile_00", "download_url_00", size,
        PostponedVatStatementFileMetadata(periodStartYear, periodStartMonth, Pdf, PostponedVATStatement, "CDS", None))

    val postponedVatStatement_2: PostponedVatStatementFile =
      PostponedVatStatementFile("statementfile_00", "download_url_00", size,
        PostponedVatStatementFileMetadata(periodStartYear, periodStartMonth_2, Pdf, PostponedVATStatement, "Chief",
          someRequestId))

    val postponedVatStatement_3: PostponedVatStatementFile =
      PostponedVatStatementFile("statementfile_00", "download_url_00", size,
        PostponedVatStatementFileMetadata(periodStartYear, periodStartMonth, Pdf, PostponedVATStatement, "CDS",
          someRequestId))

    val postponedVatStatementFiles = Seq(postponedVatStatement, postponedVatStatement_2, postponedVatStatement_3)

    val requestedPVATStatements = List(
      PostponedVatStatementsByMonth(LocalDate.of(periodStartYear, periodStartMonth, periodStartDay),
        List(PostponedVatStatementFile("statementfile_00", "download_url_00", size,
          PostponedVatStatementFileMetadata(periodStartYear, periodStartMonth, Pdf, PostponedVATStatement, "CDS",
            someRequestId), emptyString))),
      PostponedVatStatementsByMonth(LocalDate.of(periodStartYear, periodStartMonth_2, periodStartDay),
        List(PostponedVatStatementFile("statementfile_00", "download_url_00", size,
          PostponedVatStatementFileMetadata(periodStartYear, periodStartMonth_2, Pdf, PostponedVATStatement, "Chief",
            someRequestId), emptyString))))

    val dutyDeferementFile: DutyDefermentStatementFile =
      DutyDefermentStatementFile("2018_03_01-08.pdf", "url.pdf", size,
        DutyDefermentStatementFileMetadata(periodStartYear, periodStartMonth, periodStartDay, periodEndYear,
          periodEndMonth, periodEndDay, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), someDan,
          someRequestId))

    val dutyDeferementFile_2: DutyDefermentStatementFile =
      DutyDefermentStatementFile("2018_03_01-08.pdf", "url.pdf", size,
        DutyDefermentStatementFileMetadata(periodStartYear, periodStartMonth, periodStartDay, periodEndYear,
          periodEndMonth, periodEndDay, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), someDan, None))

    val dutyDeferementFile_3: DutyDefermentStatementFile =
      DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", size,
        DutyDefermentStatementFileMetadata(periodStartYear, periodStartMonth_2, periodStartDay, periodEndYear,
          periodStartMonth_2, periodEndDay, Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), someDan,
          someRequestId))

    val dutyDeferementFile_4: DutyDefermentStatementFile =
      DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", size,
        DutyDefermentStatementFileMetadata(periodStartYear, periodStartMonth_2, periodStartDay, periodEndYear,
          periodStartMonth_2, periodEndDay, Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), someDan,
          None))

    val dutyDefermentFiles = Seq(dutyDeferementFile, dutyDeferementFile_2, dutyDeferementFile_3, dutyDeferementFile_4)

    val requestedDutyDefermentStatements = List(
      DutyDefermentStatementFile("2018_03_01-08.pdf", "url.pdf", size,
        DutyDefermentStatementFileMetadata(periodStartYear, periodStartMonth, periodStartDay, periodEndYear,
          periodStartMonth, periodEndDay, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), "12345",
          someRequestId)),
      DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", size,
        DutyDefermentStatementFileMetadata(periodStartYear, periodStartMonth_2, periodStartDay, periodEndYear,
          periodStartMonth_2, periodEndDay, Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), "12345",
          someRequestId)))

    val eoriHistory: EoriHistory = EoriHistory("eori1", Some(LocalDate.now()), Some(LocalDate.now()))

    val sortStatementsService = new SortStatementsService()
  }
}
