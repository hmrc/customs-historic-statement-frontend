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

package connectors

import base.SpecBase
import config.FrontendAppConfig
import models.DDStatementType.Weekly
import models.FileFormat.Pdf
import models.{C79Certificate, DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata, FileInformation, Metadata, MetadataItem, PostponedVATStatement, PostponedVatStatementFile, PostponedVatStatementFileMetadata, SecurityStatement, SecurityStatementFile, SecurityStatementFileMetadata, VatCertificateFile, VatCertificateFileMetadata}
import play.api.http.Status
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers
import play.api.test.Helpers._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpResponse}

import scala.concurrent.Future

class SdesConnectorSpec extends SpecBase {

  "getDutyDefermentStatements" should {
    "return transformed duty deferment statements" in new Setup {
      when[Future[HttpResponse]](mockHttp.GET(eqTo(sdesDutyDefermentStatementsUrl), any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(dutyDefermentStatementFilesSdesResponse).toString())))

      running(app) {
        val result: Seq[DutyDefermentStatementFile] = await(sdesConnector.getDutyDefermentStatements(someEori, someDan)(hc))
        result must be(dutyDefermentStatementFiles)
      }
    }
  }

  "getSecurityStatements" should {
    "return transformed security statements" in new Setup {
      when[Future[HttpResponse]](mockHttp.GET(eqTo(sdesSecurityStatementsUrl), any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(securityStatementFilesWithUnknownFileTypesSdesResponse).toString())))
      running(app) {
        val result: Seq[SecurityStatementFile] = await(sdesConnector.getSecurityStatements(someEori)(hc))
        result must be(securityStatementFiles)
      }
    }
  }

  "getVatCertificates" should {
    "return transformed vat certificates" in new Setup {
      when[Future[HttpResponse]](mockHttp.GET(eqTo(sdesVatCertificatesUrl), any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(vatCertificateFilesWithUnknownFileTypesSdesResponse).toString())))
      running(app) {
        val result = await(sdesConnector.getVatCertificates(someEori)(hc))
        result must be(vatCertificateFiles)
      }
    }

    "getPostponedVatStatements" should {
      "return transformed postponed vat statements" in new Setup {
        when[Future[HttpResponse]](mockHttp.GET(eqTo(sdesPostponedVatStatementssUrl), any, any)(any, any, any))
          .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(postponedVatStatementFilesWithUnknownFileTypesSdesResponse).toString())))
        running(app) {
          val result = await(sdesConnector.getPostponedVatStatements(someEori)(hc))
          result must be(postponedVatStatementFiles)
        }
      }
    }

    "throw exception when file with unknown fileRole" in new Setup {
      when[Future[HttpResponse]](mockHttp.GET(eqTo(sdesVatCertificatesUrl), any, any)(any, any, any))
        .thenReturn(Future.successful(HttpResponse(Status.OK, Json.toJson(vatCertificateFilesWithUnknownFileRoleSdesResponse).toString())))
      running(app) {
        intercept[Exception] {
          await(sdesConnector.getVatCertificates(someEori)(hc))
        }.getMessage mustBe "Unknown file role: Invalid"

      }
    }
  }

  trait Setup {
    implicit val messages: Messages = Helpers.stubMessages()

    val someDan = "1234"
    val someEori = "eori1"
    val sdesDutyDefermentStatementsUrl = "http://localhost:9754/customs-financials-sdes-stub/files-available/list/DutyDefermentStatement"
    val dutyDefermentStatementFilesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "PDF"), MetadataItem("FileRole", "DutyDefermentStatement"), MetadataItem("DefermentStatementType", "Weekly"), MetadataItem("DutyOverLimit", "Y"), MetadataItem("DutyPaymentType", "BACS"), MetadataItem("DAN", someDan)))),
      FileInformation("name_05", "download_url_06", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "2"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "2"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "PDF"), MetadataItem("FileRole", "DutyDefermentStatement"), MetadataItem("DefermentStatementType", "Weekly"), MetadataItem("DutyOverLimit", "N"), MetadataItem("DutyPaymentType", "BACS"), MetadataItem("DAN", someDan))))
    )
    val dutyDefermentStatementFiles = List(
      DutyDefermentStatementFile("name_04", "download_url_06", 111L, DutyDefermentStatementFileMetadata(2018, 3, 14, 2018, 3, 23, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), someDan)),
      DutyDefermentStatementFile("name_05", "download_url_06", 111L, DutyDefermentStatementFileMetadata(2018, 2, 14, 2018, 2, 23, Pdf, DutyDefermentStatement, Weekly, Some(false), Some("BACS"), someDan))
    )

    val sdesSecurityStatementsUrl = "http://localhost:9754/customs-financials-sdes-stub/files-available/list/SecurityStatement"
    val securityStatementFilesSdesResponse = List(
      FileInformation("name_01", "download_url_01", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "SecurityStatement"),
        MetadataItem("eoriNumber", someEori), MetadataItem("fileSize", "111"), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018")))),
      FileInformation("name_01", "download_url_01", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "SecurityStatement"),
        MetadataItem("eoriNumber", someEori), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018"))))
    )
    val securityStatementFilesWithUnknownFileTypesSdesResponse =
      List(FileInformation("name_01", "download_url_01", 111L, Metadata(List(
        MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
        MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "foo"), MetadataItem("FileRole", "SecurityStatement"),
        MetadataItem("eoriNumber", someEori), MetadataItem("fileSize", "111"), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018"), MetadataItem("fileSize", "111"))))) ++
        securityStatementFilesSdesResponse ++
        List(FileInformation("name_01", "download_url_01", 111L, Metadata(List(
          MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("PeriodStartDay", "14"), MetadataItem("PeriodEndYear", "2018"),
          MetadataItem("PeriodEndMonth", "3"), MetadataItem("PeriodEndDay", "23"), MetadataItem("FileType", "bar"), MetadataItem("FileRole", "SecurityStatement"),
          MetadataItem("eoriNumber", someEori), MetadataItem("fileSize", "111"), MetadataItem("checksum", "checksum_01"), MetadataItem("issueDate", "3/4/2018")))))
    val securityStatementFiles = List(
      SecurityStatementFile("name_01", "download_url_01", 111L, SecurityStatementFileMetadata(2018, 3, 14, 2018, 3, 23, Pdf, SecurityStatement, someEori, 111L, "checksum_01")),
      SecurityStatementFile("name_01", "download_url_01", 111L, SecurityStatementFileMetadata(2018, 3, 14, 2018, 3, 23, Pdf, SecurityStatement, someEori, 111L, "checksum_01"))
    )

    val sdesPostponedVatStatementssUrl = "http://localhost:9754/customs-financials-sdes-stub/files-available/list/PostponedVATStatement"
    val postponedVatStatementsSdesResponse = List(
      FileInformation("name_04", "download_url_06", 113L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "Immediate"), MetadataItem("statementRequestID", "a request id")))),
      FileInformation("name_04", "download_url_04", 114L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "4"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "PostponedVATStatement"),MetadataItem("DutyPaymentMethod", "Chief")))),
      FileInformation("name_03", "download_url_03", 115L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "5"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "??")))),
      FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "PDF"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", ""))))
    )
    val postponedVatStatementFilesWithUnknownFileTypesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "foo"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "CDS"))))) ++
      postponedVatStatementsSdesResponse ++
      List(FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "bar"), MetadataItem("FileRole", "PostponedVATStatement"), MetadataItem("DutyPaymentMethod", "CDS")))))

    val postponedVatStatementFiles = List(
      PostponedVatStatementFile("name_04", "download_url_06", 113L, PostponedVatStatementFileMetadata(2018, 3, Pdf, PostponedVATStatement, "CDS", Some("a request id"))),
      PostponedVatStatementFile("name_04", "download_url_04", 114L, PostponedVatStatementFileMetadata(2018, 4, Pdf, PostponedVATStatement, "CHIEF", None)),
      PostponedVatStatementFile("name_03", "download_url_03", 115L, PostponedVatStatementFileMetadata(2018, 5, Pdf, PostponedVATStatement, "CDS", None)),
      PostponedVatStatementFile("name_01", "download_url_01", 1300000L, PostponedVatStatementFileMetadata(2018, 6, Pdf, PostponedVATStatement, "CDS", None))
    )

    val sdesVatCertificatesUrl = "http://localhost:9754/customs-financials-sdes-stub/files-available/list/C79Certificate"
    val vatCertificateFilesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_04", "download_url_04", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "4"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_03", "download_url_03", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "5"), MetadataItem("FileType", "pdf"), MetadataItem("FileRole", "C79Certificate")))),
      FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "PDF"), MetadataItem("FileRole", "C79Certificate"))))
    )
    val vatCertificateFilesWithUnknownFileTypesSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "foo"), MetadataItem("FileRole", "C79Certificate"))))) ++
      vatCertificateFilesSdesResponse ++
      List(FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "bar"), MetadataItem("FileRole", "C79Certificate")))))

    val vatCertificateFiles = List(
      VatCertificateFile("name_04", "download_url_06", 111L, VatCertificateFileMetadata(2018, 3, Pdf, C79Certificate, None)),
      VatCertificateFile("name_04", "download_url_04", 111L, VatCertificateFileMetadata(2018, 4, Pdf, C79Certificate, None)),
      VatCertificateFile("name_03", "download_url_03", 111L, VatCertificateFileMetadata(2018, 5, Pdf, C79Certificate, None)),
      VatCertificateFile("name_01", "download_url_01", 1300000L, VatCertificateFileMetadata(2018, 6, Pdf, C79Certificate, None))
    )

    val vatCertificateFilesWithUnknownFileRoleSdesResponse = List(
      FileInformation("name_04", "download_url_06", 111L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "3"), MetadataItem("FileType", "foo"), MetadataItem("FileRole", "Invalid"))))) ++
      vatCertificateFilesSdesResponse ++
      List(FileInformation("name_01", "download_url_01", 1300000L, Metadata(List(MetadataItem("PeriodStartYear", "2018"), MetadataItem("PeriodStartMonth", "6"), MetadataItem("FileType", "bar"), MetadataItem("FileRole", "Invalid")))))

    val mockHttp = mock[HttpClient]
    val app = applicationBuilder().overrides(
      bind[HttpClient].toInstance(mockHttp)
    ).build()
    val mockAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val sdesConnector = app.injector.instanceOf[SdesConnector]
    implicit val hc: HeaderCarrier = HeaderCarrier()
  }

}
