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

package views

import models.FileFormat.Pdf
import models.{C79Certificate, EoriHistory, VatCertificateFile, VatCertificateFileMetadata}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import viewmodels.{VatCertificatesByMonth, VatCertificatesForEori, VatViewModel}
import views.html.ImportVatRequestedStatements
import utils.TestData.*

import java.time.LocalDate

class ImportVatRequestedStatementsSpec extends ViewTestHelper {
  "view" should {
    "display correct title and contents" in new Setup {
      titleShouldBeCorrect(view, "cf.import-vat.requested.title")
      pageShouldContainBackLinkUrl(view, appConfig.returnLink("c79Certificate"))
      headingShouldBeCorrect
      requestedAvailableTextShouldBeCorrect
    }
  }

  private def headingShouldBeCorrect(implicit view: Document): Assertion =
    view
      .getElementById("requested-import-vat-certificates-heading")
      .html()
      .contains(
        messages("cf.import-vat.requested.title")
      ) mustBe true

  private def requestedAvailableTextShouldBeCorrect(implicit view: Document): Assertion =
    view
      .getElementById("available-text")
      .html()
      .contains(
        messages("cf.import-vat.requested.available.text")
      ) mustBe true

  trait Setup {
    private val size                   = 113L
    private val periodStartYear: Int   = 2018
    private val periodStartMonth: Int  = 3
    private val periodStartMonth2: Int = 4

    private val eoriHistory = EoriHistory(
      eori,
      Some(LocalDate.of(year, month, day)),
      Some(LocalDate.of(year, month_2, day))
    )

    val vatCertificateFiles: VatCertificateFile = VatCertificateFile(
      fileName,
      downloadUrl,
      size,
      VatCertificateFileMetadata(periodStartYear, periodStartMonth, Pdf, C79Certificate, None)
    )

    val vatCertificateFiles_2: VatCertificateFile = VatCertificateFile(
      fileName,
      downloadUrl,
      size,
      VatCertificateFileMetadata(periodStartYear, periodStartMonth2, Pdf, C79Certificate, None)
    )

    val vatCertificatesByMonth_1: VatCertificatesByMonth = VatCertificatesByMonth(
      LocalDate.of(year, month, day),
      Seq(vatCertificateFiles)
    )

    val vatCertificatesByMonth_2: VatCertificatesByMonth = VatCertificatesByMonth(
      LocalDate.of(year, month_2, day),
      Seq(vatCertificateFiles_2)
    )

    private val vatCertificatesForEori =
      VatCertificatesForEori(eoriHistory, Seq(vatCertificatesByMonth_1), Seq(vatCertificatesByMonth_2))

    private val vatViewModel = VatViewModel(Seq(vatCertificatesForEori))

    implicit val view: Document = Jsoup.parse(
      application.injector
        .instanceOf[ImportVatRequestedStatements]
        .apply(
          vatViewModel,
          appConfig.returnLink("c79Certificate")
        )
        .body
    )
  }
}
