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

package connectors

import base.SpecBase
import config.FrontendAppConfig
import models.DDStatementType.Weekly
import models.FileFormat.Pdf
import models.*
import play.api.http.Status
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.Helpers
import play.api.test.Helpers.*
import uk.gov.hmrc.http.{HttpReads, HttpResponse, StringContextOps}
import utils.Utils.emptyString
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.{eq => eqTo}
import uk.gov.hmrc.http.client.{HttpClientV2, RequestBuilder}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits.global

class SdesConnectorSpec extends SpecBase {

  "getDutyDefermentStatements" should {
    "return transformed duty deferment statements" in new Setup {

      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(HttpResponse(Status.OK, Json.toJson(dutyDefermentStatementFilesSdesResponse).toString()))
        )

      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(mockHttp.get(eqTo(url"$sdesDutyDefermentStatementsUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result: Seq[DutyDefermentStatementFile] =
          await(sdesConnector.getDutyDefermentStatements(someEori, someDan)(hc))

        result must be(dutyDefermentStatementFiles)
      }
    }
  }

  "getSecurityStatements" should {
    "return transformed security statements" in new Setup {

      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(
            HttpResponse(Status.OK, Json.toJson(securityStatementFilesWithUnknownFileTypesSdesResponse).toString())
          )
        )

      when(mockHttp.get(eqTo(url"$sdesSecurityStatementsUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result: Seq[SecurityStatementFile] = await(sdesConnector.getSecurityStatements(someEori)(hc))
        result must be(securityStatementFiles)
      }
    }
  }

  "getVatCertificates" should {
    "return transformed vat certificates" in new Setup {

      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(
            HttpResponse(Status.OK, Json.toJson(vatCertificateFilesWithUnknownFileTypesSdesResponse).toString())
          )
        )

      when(mockHttp.get(eqTo(url"$sdesVatCertificatesUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        val result = await(sdesConnector.getVatCertificates(someEori)(hc))
        result must be(vatCertificateFiles)
      }
    }

    "getPostponedVatStatements" should {
      "return transformed postponed vat statements" in new Setup {

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(
              HttpResponse(
                Status.OK,
                Json.toJson(postponedVatStatementFilesWithUnknownFileTypesSdesResponse).toString()
              )
            )
          )

        when(mockHttp.get(eqTo(url"$sdesPostponedVatStatementssUrl"))(any()))
          .thenReturn(requestBuilder)

        running(app) {
          val result = await(sdesConnector.getPostponedVatStatements(someEori)(hc))
          result must be(postponedVatStatementFiles)
        }
      }
    }

    "getCashStatements" should {
      "return transformed cash statements" in new Setup {

        when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
          .thenReturn(
            Future.successful(HttpResponse(Status.OK, Json.toJson(cashStatementFilesSdesResponse).toString()))
          )

        when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
        when(mockHttp.get(eqTo(url"$sdesCashStatementsUrl"))(any()))
          .thenReturn(requestBuilder)

        sdesConnector.getCashStatements(someEori)(hc).map { cashStatements =>
          cashStatements mustBe cashStatementFiles
        }
      }
    }

    "throw exception when file with unknown fileRole" in new Setup {

      when(requestBuilder.setHeader(any[(String, String)]())).thenReturn(requestBuilder)
      when(requestBuilder.execute(any[HttpReads[HttpResponse]], any[ExecutionContext]))
        .thenReturn(
          Future.successful(
            HttpResponse(Status.OK, Json.toJson(vatCertificateFilesWithUnknownFileRoleSdesResponse).toString())
          )
        )

      when(mockHttp.get(eqTo(url"$sdesVatCertificatesUrl"))(any()))
        .thenReturn(requestBuilder)

      running(app) {
        intercept[Exception] {
          await(sdesConnector.getVatCertificates(someEori)(hc))
        }.getMessage mustBe "Unknown file role: Invalid"
      }
    }
  }

  trait Setup {
    implicit val messages: Messages = Helpers.stubMessages()

    val someDan  = "1234"
    val someEori = "eori1"

    val year   = 2018
    val month  = 3
    val day    = 14
    val hour   = 2
    val minute = 23

    val size = 111L

    val sdesCashStatementsUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/CDSCashAccount"

    val cashStatementFilesSdesResponse: Seq[FileInformation] = List(
      FileInformation(
        "cash_statement_01",
        "download_url_01",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("PeriodStartDay", "14"),
            MetadataItem("PeriodEndYear", "2018"),
            MetadataItem("PeriodEndMonth", "3"),
            MetadataItem("PeriodEndDay", "31"),
            MetadataItem("FileType", "PDF"),
            MetadataItem("FileRole", "CDSCashAccount")
          )
        )
      ),
      FileInformation(
        "cash_statement_02",
        "download_url_02",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "2"),
            MetadataItem("PeriodStartDay", "14"),
            MetadataItem("PeriodEndYear", "2018"),
            MetadataItem("PeriodEndMonth", "2"),
            MetadataItem("PeriodEndDay", "28"),
            MetadataItem("FileType", "PDF"),
            MetadataItem("FileRole", "CDSCashAccount")
          )
        )
      )
    )

    val cashStatementFiles: Seq[CashStatementFile] = List(
      CashStatementFile(
        "cash_statement_01",
        "download_url_01",
        size,
        CashStatementFileMetadata(year, month, day, year, month, day + 17, Pdf, CDSCashAccount, None)
      ),
      CashStatementFile(
        "cash_statement_02",
        "download_url_02",
        size,
        CashStatementFileMetadata(year, month - 1, day, year, month - 1, day + 14, Pdf, CDSCashAccount, None)
      )
    )

    val cashStatementFilesWithUnknownFileRoleSdesResponse: Seq[FileInformation] = List(
      FileInformation(
        "cash_statement_01",
        "download_url_01",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("PeriodStartDay", "14"),
            MetadataItem("PeriodEndYear", "2018"),
            MetadataItem("PeriodEndMonth", "3"),
            MetadataItem("PeriodEndDay", "31"),
            MetadataItem("FileType", "PDF"),
            MetadataItem("FileRole", "Invalid")
          )
        )
      )
    ) ++ cashStatementFilesSdesResponse

    val sdesDutyDefermentStatementsUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/DutyDefermentStatement"

    val dutyDefermentStatementFilesSdesResponse = List(
      FileInformation(
        "name_04",
        "download_url_06",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("PeriodStartDay", "14"),
            MetadataItem("PeriodEndYear", "2018"),
            MetadataItem("PeriodEndMonth", "3"),
            MetadataItem("PeriodEndDay", "23"),
            MetadataItem("FileType", "PDF"),
            MetadataItem("FileRole", "DutyDefermentStatement"),
            MetadataItem("DefermentStatementType", "Weekly"),
            MetadataItem("DutyOverLimit", "Y"),
            MetadataItem("DutyPaymentType", "BACS"),
            MetadataItem("DAN", someDan)
          )
        )
      ),
      FileInformation(
        "name_05",
        "download_url_06",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "2"),
            MetadataItem("PeriodStartDay", "14"),
            MetadataItem("PeriodEndYear", "2018"),
            MetadataItem("PeriodEndMonth", "2"),
            MetadataItem("PeriodEndDay", "23"),
            MetadataItem("FileType", "PDF"),
            MetadataItem("FileRole", "DutyDefermentStatement"),
            MetadataItem("DefermentStatementType", "Weekly"),
            MetadataItem("DutyOverLimit", "N"),
            MetadataItem("DutyPaymentType", "BACS"),
            MetadataItem("DAN", someDan)
          )
        )
      )
    )

    val dutyDefermentStatementFiles = List(
      DutyDefermentStatementFile(
        "name_04",
        "download_url_06",
        size,
        DutyDefermentStatementFileMetadata(
          year,
          month,
          day,
          year,
          month,
          minute,
          Pdf,
          DutyDefermentStatement,
          Weekly,
          Some(true),
          Some("BACS"),
          someDan
        )
      ),
      DutyDefermentStatementFile(
        "name_05",
        "download_url_06",
        size,
        DutyDefermentStatementFileMetadata(
          year,
          hour,
          day,
          year,
          hour,
          minute,
          Pdf,
          DutyDefermentStatement,
          Weekly,
          Some(false),
          Some("BACS"),
          someDan
        )
      )
    )

    val sdesSecurityStatementsUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/SecurityStatement"

    val securityStatementFilesSdesResponse = List(
      FileInformation(
        "name_01",
        "download_url_01",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("PeriodStartDay", "14"),
            MetadataItem("PeriodEndYear", "2018"),
            MetadataItem("PeriodEndMonth", "3"),
            MetadataItem("PeriodEndDay", "23"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "SecurityStatement"),
            MetadataItem("eoriNumber", someEori),
            MetadataItem("fileSize", "111"),
            MetadataItem("checksum", "checksum_01"),
            MetadataItem("issueDate", "3/4/2018")
          )
        )
      ),
      FileInformation(
        "name_01",
        "download_url_01",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("PeriodStartDay", "14"),
            MetadataItem("PeriodEndYear", "2018"),
            MetadataItem("PeriodEndMonth", "3"),
            MetadataItem("PeriodEndDay", "23"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "SecurityStatement"),
            MetadataItem("eoriNumber", someEori),
            MetadataItem("checksum", "checksum_01"),
            MetadataItem("issueDate", "3/4/2018")
          )
        )
      )
    )

    val securityStatementFilesWithUnknownFileTypesSdesResponse =
      List(
        FileInformation(
          "name_01",
          "download_url_01",
          size,
          Metadata(
            List(
              MetadataItem("PeriodStartYear", "2018"),
              MetadataItem("PeriodStartMonth", "3"),
              MetadataItem("PeriodStartDay", "14"),
              MetadataItem("PeriodEndYear", "2018"),
              MetadataItem("PeriodEndMonth", "3"),
              MetadataItem("PeriodEndDay", "23"),
              MetadataItem("FileType", "foo"),
              MetadataItem("FileRole", "SecurityStatement"),
              MetadataItem("eoriNumber", someEori),
              MetadataItem("fileSize", "111"),
              MetadataItem("checksum", "checksum_01"),
              MetadataItem("issueDate", "3/4/2018"),
              MetadataItem("fileSize", "111")
            )
          )
        )
      ) ++
        securityStatementFilesSdesResponse ++
        List(
          FileInformation(
            "name_01",
            "download_url_01",
            size,
            Metadata(
              List(
                MetadataItem("PeriodStartYear", "2018"),
                MetadataItem("PeriodStartMonth", "3"),
                MetadataItem("PeriodStartDay", "14"),
                MetadataItem("PeriodEndYear", "2018"),
                MetadataItem("PeriodEndMonth", "3"),
                MetadataItem("PeriodEndDay", "23"),
                MetadataItem("FileType", "bar"),
                MetadataItem("FileRole", "SecurityStatement"),
                MetadataItem("eoriNumber", someEori),
                MetadataItem("fileSize", "111"),
                MetadataItem("checksum", "checksum_01"),
                MetadataItem("issueDate", "3/4/2018")
              )
            )
          )
        )

    val securityStatementFiles = List(
      SecurityStatementFile(
        "name_01",
        "download_url_01",
        size,
        SecurityStatementFileMetadata(
          year,
          month,
          day,
          year,
          month,
          minute,
          Pdf,
          SecurityStatement,
          someEori,
          size,
          "checksum_01"
        )
      ),
      SecurityStatementFile(
        "name_01",
        "download_url_01",
        size,
        SecurityStatementFileMetadata(
          year,
          month,
          day,
          year,
          month,
          minute,
          Pdf,
          SecurityStatement,
          someEori,
          size,
          "checksum_01"
        )
      )
    )

    val sdesPostponedVatStatementssUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/PostponedVATStatement"

    val size2 = 113L
    val size3 = 114L
    val size4 = 1115L
    val size5 = 1300000L

    val postponedVatStatementsSdesResponse = List(
      FileInformation(
        "name_04",
        "download_url_06",
        size2,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "PostponedVATStatement"),
            MetadataItem("DutyPaymentMethod", "Immediate"),
            MetadataItem("statementRequestID", "a request id")
          )
        )
      ),
      FileInformation(
        "name_04",
        "download_url_04",
        size3,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "4"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "PostponedVATStatement"),
            MetadataItem("DutyPaymentMethod", "Chief")
          )
        )
      ),
      FileInformation(
        "name_03",
        "download_url_03",
        size4,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "5"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "PostponedVATStatement"),
            MetadataItem("DutyPaymentMethod", "??")
          )
        )
      ),
      FileInformation(
        "name_01",
        "download_url_01",
        size5,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "6"),
            MetadataItem("FileType", "PDF"),
            MetadataItem("FileRole", "PostponedVATStatement"),
            MetadataItem("DutyPaymentMethod", emptyString)
          )
        )
      )
    )

    val postponedVatStatementFilesWithUnknownFileTypesSdesResponse = List(
      FileInformation(
        "name_04",
        "download_url_06",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("FileType", "foo"),
            MetadataItem("FileRole", "PostponedVATStatement"),
            MetadataItem("DutyPaymentMethod", "CDS")
          )
        )
      )
    ) ++ postponedVatStatementsSdesResponse ++ List(
      FileInformation(
        "name_01",
        "download_url_01",
        size5,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "6"),
            MetadataItem("FileType", "bar"),
            MetadataItem("FileRole", "PostponedVATStatement"),
            MetadataItem("DutyPaymentMethod", "CDS")
          )
        )
      )
    )

    val startMonth3 = 3
    val startMonth4 = 4
    val startMonth5 = 5
    val startMonth6 = 6

    val postponedVatStatementFiles = List(
      PostponedVatStatementFile(
        "name_04",
        "download_url_06",
        size2,
        PostponedVatStatementFileMetadata(year, startMonth3, Pdf, PostponedVATStatement, "CDS", Some("a request id"))
      ),
      PostponedVatStatementFile(
        "name_04",
        "download_url_04",
        size3,
        PostponedVatStatementFileMetadata(year, startMonth4, Pdf, PostponedVATStatement, "CHIEF", None)
      ),
      PostponedVatStatementFile(
        "name_03",
        "download_url_03",
        size4,
        PostponedVatStatementFileMetadata(year, startMonth5, Pdf, PostponedVATStatement, "CDS", None)
      ),
      PostponedVatStatementFile(
        "name_01",
        "download_url_01",
        size5,
        PostponedVatStatementFileMetadata(year, startMonth6, Pdf, PostponedVATStatement, "CDS", None)
      )
    )

    val sdesVatCertificatesUrl =
      "http://localhost:9754/customs-financials-sdes-stub/files-available/list/C79Certificate"

    val vatCertificateFilesSdesResponse = List(
      FileInformation(
        "name_04",
        "download_url_06",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "C79Certificate")
          )
        )
      ),
      FileInformation(
        "name_04",
        "download_url_04",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "4"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "C79Certificate")
          )
        )
      ),
      FileInformation(
        "name_03",
        "download_url_03",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "5"),
            MetadataItem("FileType", "pdf"),
            MetadataItem("FileRole", "C79Certificate")
          )
        )
      ),
      FileInformation(
        "name_01",
        "download_url_01",
        size5,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "6"),
            MetadataItem("FileType", "PDF"),
            MetadataItem("FileRole", "C79Certificate")
          )
        )
      )
    )

    val vatCertificateFilesWithUnknownFileTypesSdesResponse = List(
      FileInformation(
        "name_04",
        "download_url_06",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("FileType", "foo"),
            MetadataItem("FileRole", "C79Certificate")
          )
        )
      )
    ) ++ vatCertificateFilesSdesResponse ++ List(
      FileInformation(
        "name_01",
        "download_url_01",
        size5,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "6"),
            MetadataItem("FileType", "bar"),
            MetadataItem("FileRole", "C79Certificate")
          )
        )
      )
    )

    val vatCertificateFiles = List(
      VatCertificateFile(
        "name_04",
        "download_url_06",
        size,
        VatCertificateFileMetadata(year, startMonth3, Pdf, C79Certificate, None)
      ),
      VatCertificateFile(
        "name_04",
        "download_url_04",
        size,
        VatCertificateFileMetadata(year, startMonth4, Pdf, C79Certificate, None)
      ),
      VatCertificateFile(
        "name_03",
        "download_url_03",
        size,
        VatCertificateFileMetadata(year, startMonth5, Pdf, C79Certificate, None)
      ),
      VatCertificateFile(
        "name_01",
        "download_url_01",
        size5,
        VatCertificateFileMetadata(year, startMonth6, Pdf, C79Certificate, None)
      )
    )

    val vatCertificateFilesWithUnknownFileRoleSdesResponse = List(
      FileInformation(
        "name_04",
        "download_url_06",
        size,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "3"),
            MetadataItem("FileType", "foo"),
            MetadataItem("FileRole", "Invalid")
          )
        )
      )
    ) ++ vatCertificateFilesSdesResponse ++ List(
      FileInformation(
        "name_01",
        "download_url_01",
        size5,
        Metadata(
          List(
            MetadataItem("PeriodStartYear", "2018"),
            MetadataItem("PeriodStartMonth", "6"),
            MetadataItem("FileType", "bar"),
            MetadataItem("FileRole", "Invalid")
          )
        )
      )
    )

    val mockHttp: HttpClientV2         = mock[HttpClientV2]
    val requestBuilder: RequestBuilder = mock[RequestBuilder]

    val app           = applicationBuilder()
      .overrides(
        bind[HttpClientV2].toInstance(mockHttp),
        bind[RequestBuilder].toInstance(requestBuilder)
      )
      .build()
    val mockAppConfig = app.injector.instanceOf[FrontendAppConfig]
    val sdesConnector = app.injector.instanceOf[SdesConnector]
  }
}
