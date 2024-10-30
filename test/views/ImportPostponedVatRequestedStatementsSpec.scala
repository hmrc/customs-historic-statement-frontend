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
import play.twirl.api.Html
import viewmodels.{
  PostponedVatStatementsByMonth,
  PostponedVatStatementsForEori,
  PostponedVatViewModel,
  SourceDisplay,
  StatementDisplayData
}
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

  "PostponedVatViewModel" should {
    "correctly handle historiesWithDisplayData" in new Setup {
      val result: Seq[StatementDisplayData] =
        PostponedVatViewModel.historiesWithDisplayData(postponedVatViewModel.statementsForAllEoris)

      displayStatementsShouldBeCorrect(result)
    }

    "correctly handle groupedStatementsBySource" in new Setup {
      val result: Map[String, Seq[PostponedVatStatementFile]] =
        PostponedVatViewModel.groupedStatementsBySource(postponedVatStatementsByMonth_2)

      result.size mustBe 1
      result("CDS").length mustBe 1
      result("CDS").head mustBe postponedVatStatementFile_2
    }

    "correctly handle renderSourceDisplay" in new Setup {
      val sourceDisplay: SourceDisplay = SourceDisplay("CDS", Seq(postponedVatStatementFile))
      val result: Html = PostponedVatViewModel.renderSourceDisplay(sourceDisplay, 0, 0, "October 2020")

      result.body must include(postponedVatStatementFile.downloadURL)
    }

    "correctly handle missingFileMessage for CDS" in new Setup {
      val result: String = PostponedVatViewModel.missingFileMessage("CDS")

      result mustBe "No CDS statements available."
    }

    "correctly handle missingFileMessage for CHIEF" in new Setup {
      val result: String = PostponedVatViewModel.missingFileMessage("CHIEF")

      result mustBe "No CHIEF statements available."
    }
  }

  private def displayStatementsShouldBeCorrect(result: Seq[StatementDisplayData]): Assertion = {
    result.length mustBe 1
    result.head.monthYear.equalsIgnoreCase("October 2020") mustBe true
    result.head.monthYearId.equalsIgnoreCase("october-2020") mustBe true
    result.head.formattedMonth mustBe "October"
    result.head.sources.length mustBe 2
    result.head.sources.head.source mustBe "CDS"
    result.head.index mustBe 0
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

    protected val eoriHistory: EoriHistory = EoriHistory(someEori,
      Some(LocalDate.of(localDateYear, localDateMonth, localDateDay)),
      Some(LocalDate.of(localDateYear, localDateMonth2, localDateDay)))

    protected val postponedVatStatementFile: PostponedVatStatementFile = PostponedVatStatementFile(
      filename,
      downloadURL,
      size,
      PostponedVatStatementFileMetadata(periodStartYear,
        periodStartMonth,
        Pdf,
        PostponedVATStatement,
        source,
        Some(statementRequestId)))

    protected val postponedVatStatementFile_2: PostponedVatStatementFile = PostponedVatStatementFile(
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

    protected val postponedVatStatementsByMonth_2: PostponedVatStatementsByMonth = PostponedVatStatementsByMonth(
      LocalDate.of(localDateYear, localDateMonth2, localDateDay),
      Seq(postponedVatStatementFile_2))

    private val postponedVatStatementsForEori: PostponedVatStatementsForEori = PostponedVatStatementsForEori(
      eoriHistory, Seq(postponedVatStatementsByMonth_1), Seq(postponedVatStatementsByMonth_2))

    protected val postponedVatViewModel: PostponedVatViewModel =
      PostponedVatViewModel(Seq(postponedVatStatementsForEori))

    implicit val view: Document = Jsoup.parse(
      app.injector.instanceOf[ImportPostponedVatRequestedStatements].apply(
        postponedVatViewModel, config.returnLink("postponedVATStatement")).body)
  }
}
