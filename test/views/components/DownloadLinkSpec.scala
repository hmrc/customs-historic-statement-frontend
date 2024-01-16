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
import models.FileFormat.Pdf
import models.{C79Certificate, FileFormat, VatCertificateFile, VatCertificateFileMetadata}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import views.ViewTestHelper
import views.html.components.download_link

class DownloadLinkSpec extends ViewTestHelper {

  "Component" should {

    "display the correct test" when {

      "vat certificate file is available" in new Setup {
        implicit val viewDoc: Document = view(Some(c79Certificates))

        shouldDisplayDownloadURL()
        shouldDisplayFileFormatAndVoiceoverInfo(Pdf, size)
      }

      "vat certificate file is not available" in new Setup {
        implicit val viewDoc: Document = view()

        shouldDisplayMissingFileIdContents(Pdf)
      }
    }
  }

  private def shouldDisplayDownloadURL(id: String = "testId",
                                       url: String = "download_url_00")(implicit view: Document): Assertion = {
    view.getElementById(id).attr("href").contains(url) mustBe true
  }

  private def shouldDisplayFileFormatAndVoiceoverInfo(fileFormat: FileFormat,
                                                      size: Long,
                                                      period: String = "periodDuration")
                                                     (implicit view: Document): Assertion = {
    val spanElements = view.getElementsByTag("span")
    spanElements.get(0).text mustBe s"${fileFormat.name} (${Formatters.fileSize(size)})"

    spanElements.get(1).text mustBe msg("cf.account.vat.download-link", fileFormat,
      period, s"${Formatters.fileSize(size)}")
  }

  private def shouldDisplayMissingFileIdContents(fileFormat: FileFormat,
                                                 period: String = "periodDuration")
                                                (implicit view: Document): Assertion = {
    val spanElements = view.getElementById("missing-file-testId").getElementsByTag("span")

    spanElements.get(0).text mustBe msg("cf.account.vat.missing-file-hidden-text", fileFormat, period)
    spanElements.get(1).text mustBe msg("cf.account.vat.missing-file")
  }

  trait Setup {

    val filename: String = "statementFile_00"
    val downloadURL: String = "download_url_00"
    val size: Long = 99L
    val periodStartYear: Int = 2017
    val periodStartMonth: Int = 12

    val c79Certificates: VatCertificateFile = VatCertificateFile(
      filename,
      downloadURL,
      size,
      VatCertificateFileMetadata(
        periodStartYear,
        periodStartMonth,
        Pdf,
        C79Certificate,
        None))

    val id = "testId"
    val period = "periodDuration"

    def view(vatCertificateFile: Option[VatCertificateFile] = None): Document =
      Jsoup.parse(app.injector.instanceOf[download_link].apply(vatCertificateFile, Pdf, id, period).body)
  }
}
