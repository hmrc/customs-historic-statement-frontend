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
import models.{EoriHistory, PostponedVATStatement, PostponedVatStatementFile, PostponedVatStatementFileMetadata}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import viewmodels.{PostponedVatStatementsByMonth, PostponedVatStatementsForEori, PostponedVatViewModel}
import views.html.ImportPostponedVatRequestedStatements

import java.time.LocalDate

class ImportPostponedVatRequestedStatementsSpec extends ViewTestHelper {

  "view" should {
    "display correct title and contents" in new Setup {
      titleShouldBeCorrect(view, "cf.import-postponed-vat.requested.title")
      pageShouldContainBackLinkUrl(view, config.returnLink("postponedVATStatement"))
      headingShouldBeCorrect
      requestedAvailableTextShouldBeCorrect
    }
  }

  private def headingShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("requested-import-postponed-vat-statements-heading").html().contains(
      msg("cf.import-postponed-vat.requested.title")
    ) mustBe true
  }

  private def requestedAvailableTextShouldBeCorrect(implicit view: Document): Assertion = {
    view.getElementById("available-text").html().contains(
      msg("cf.import-postponed-vat.requested.available.text")
    ) mustBe true
  }

  trait Setup {

    private val someEori = "12345678"
    private val localDateYear = 2020
    private val localDateMonth = 10
    private val localDateMonth2 = 10
    private val localDateDay = 1

    private val filename: String = "name_04"
    private val downloadURL: String = "download_url_06"
    private val size = 113L
    private val periodStartYear: Int = 2018
    private val periodStartMonth: Int = 3
    private val periodStartMonth2: Int = 4
    private val source: String = "CDS"
    private val statementRequestId = "a request id"

    private val eoriHistory = EoriHistory(someEori,
      Some(LocalDate.of(localDateYear, localDateMonth, localDateDay)),
      Some(LocalDate.of(localDateYear, localDateMonth2, localDateDay)))

    private val postponedVatStatementFile = PostponedVatStatementFile(
      filename,
      downloadURL,
      size,
      PostponedVatStatementFileMetadata(periodStartYear,
        periodStartMonth,
        Pdf,
        PostponedVATStatement,
        source,
        Some(statementRequestId)))

    private val postponedVatStatementFile_2 = PostponedVatStatementFile(
      filename,
      downloadURL,
      size,
      PostponedVatStatementFileMetadata(periodStartYear,
        periodStartMonth2,
        Pdf,
        PostponedVATStatement,
        source,
        Some(statementRequestId)))

    private val postponedVatStatementsByMonth_1 = PostponedVatStatementsByMonth(
      LocalDate.of(localDateYear, localDateMonth, localDateDay),
      Seq(postponedVatStatementFile))

    private val postponedVatStatementsByMonth_2 = PostponedVatStatementsByMonth(
      LocalDate.of(localDateYear, localDateMonth2, localDateDay),
      Seq(postponedVatStatementFile_2))

    private val postponedVatStatementsForEori: PostponedVatStatementsForEori = PostponedVatStatementsForEori(
      eoriHistory, Seq(postponedVatStatementsByMonth_1), Seq(postponedVatStatementsByMonth_2))

    private val postponedVatViewModel: PostponedVatViewModel =
      PostponedVatViewModel(Seq(postponedVatStatementsForEori))

    implicit val view: Document = Jsoup.parse(
      app.injector.instanceOf[ImportPostponedVatRequestedStatements].apply(
        postponedVatViewModel, config.returnLink("postponedVATStatement")).body)
  }
}
