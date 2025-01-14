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

package views.components

import helpers.Formatters
import models.FileFormat.{Csv, Pdf}
import models.{
  C79Certificate, CDSCashAccount, CashStatementFile, CashStatementFileMetadata, FileFormat, VatCertificateFile,
  VatCertificateFileMetadata
}
import utils.TestData.{downloadUrl, fileName, periodStartDay, periodStartMonth, periodStartYear, test_Id}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import views.ViewTestHelper
import views.html.components.{download_link, download_link_cash_account}

class DownloadLinkSpec extends ViewTestHelper {

  "Component" should {

    "display the correct text for VAT certificate" when {

      "vat certificate file is available" in new Setup {
        implicit val viewDoc: Document = viewVat(Some(c79Certificates))

        shouldDisplayDownloadURL(downloadUrl)
        shouldDisplayFileFormatAndVoiceoverInfo(Pdf, vatSize)
      }

      "vat certificate file is not available" in new Setup {
        implicit val viewDoc: Document = viewVat()

        shouldDisplayMissingFileIdContents(
          Pdf,
          "cf.account.vat.missing-file-hidden-text",
          "cf.account.vat.missing-file"
        )
      }
    }

    "display the correct text for Cash Statement" when {

      "cash statement file is available" in new Setup {
        implicit val viewDoc: Document = viewCash(Some(cashStatementFile))

        shouldDisplayDownloadURL(cashDownloadURL)
        shouldDisplayFileFormatAndVoiceoverInfo(Csv, cashSize)
      }

      "cash statement file is not available" in new Setup {
        implicit val viewDoc: Document = viewCash()

        shouldDisplayMissingFileIdContents(
          Csv,
          "cf.cash-statement-requested.missing-file-hidden-text",
          "cf.cash-statement-requested.missing-file"
        )
      }
    }
  }

  private def shouldDisplayDownloadURL(url: String)(implicit view: Document): Assertion =
    view.getElementById("testId").attr("href").contains(url) mustBe true

  private def shouldDisplayFileFormatAndVoiceoverInfo(
    fileFormat: FileFormat,
    size: Long,
    period: String = "periodDuration"
  )(implicit view: Document): Assertion = {
    val spanElements = view.getElementsByTag("span")
    spanElements.get(0).text mustBe s"${fileFormat.name} (${Formatters.fileSize(size)})"

    spanElements.get(1).text mustBe messages(
      "cf.cash-statement-requested.download-link",
      fileFormat,
      period,
      s"${Formatters.fileSize(size)}"
    )
  }

  private def shouldDisplayMissingFileIdContents(
    fileFormat: FileFormat,
    hiddenTextKey: String,
    missingFileKey: String,
    period: String = "periodDuration"
  )(implicit view: Document): Assertion = {
    val spanElements = view.getElementById("missing-file-testId").getElementsByTag("span")

    spanElements.get(0).text mustBe messages(hiddenTextKey, fileFormat, period)
    spanElements.get(1).text mustBe messages(missingFileKey)
  }

  trait Setup {
    val vatSize: Long           = 99L
    val cashFilename: String    = "cashStatementFile_00"
    val cashDownloadURL: String = "download_cash_url_00"
    val cashSize: Long          = 150L

    val c79Certificates: VatCertificateFile = VatCertificateFile(
      fileName,
      downloadUrl,
      vatSize,
      VatCertificateFileMetadata(periodStartYear, periodStartMonth, Pdf, C79Certificate, None)
    )

    val cashStatementFile: CashStatementFile = CashStatementFile(
      cashFilename,
      cashDownloadURL,
      cashSize,
      CashStatementFileMetadata(
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        periodStartYear,
        periodStartMonth,
        periodStartDay,
        Csv,
        CDSCashAccount,
        Some("requestId")
      ),
      "12345678"
    )

    val period = "periodDuration"

    def viewVat(vatCertificateFile: Option[VatCertificateFile] = None): Document =
      Jsoup.parse(instanceOf[download_link].apply(vatCertificateFile, Pdf, test_Id, period).body)

    def viewCash(cashStatementFile: Option[CashStatementFile] = None): Document =
      Jsoup.parse(
        instanceOf[download_link_cash_account].apply(cashStatementFile, Csv, test_Id, period).body
      )
  }
}
