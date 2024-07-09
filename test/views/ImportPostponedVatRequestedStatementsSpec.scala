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
import viewmodels.{PostponedVatStatementsByMonth, PostponedVatStatementsForEori, PostponedVatViewModel, StatementDisplayData}
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
    "correctly handle requestedStatements" in new Setup {
      val result: Seq[(PostponedVatStatementsByMonth, Int)] = postponedVatViewModel.requestedStatements(0)
      result.length mustBe 1
      result.head._1 mustBe postponedVatStatementsByMonth_2
      result.head._2 mustBe 0
    }

    "correctly handle displayStatements" in new Setup {
      val result: Seq[StatementDisplayData] = postponedVatViewModel.displayStatements(0)
      displayStatementsShouldBeCorrect(result)
    }

    "correctly handle groupedStatements" in new Setup {
      val result: Map[String, Seq[PostponedVatStatementFile]] = postponedVatViewModel.groupedStatements(postponedVatStatementsByMonth_2)
      result.size mustBe 1
      result("CDS").length mustBe 1
      result("CDS").head mustBe postponedVatStatementFile_2
    }

    "correctly handle hasRequestedStatements" in new Setup {
      postponedVatViewModel.hasRequestedStatements(0) mustBe true

      val emptyViewModel: PostponedVatViewModel = PostponedVatViewModel(Seq(PostponedVatStatementsForEori(eoriHistory, Seq.empty, Seq.empty)))
      emptyViewModel.hasRequestedStatements(0) mustBe false
    }

    "correctly handle createStatementDisplayData" in new Setup {
      val statementWithIndex: (PostponedVatStatementsByMonth, Int) = (postponedVatStatementsByMonth_2, 0)
      val result: StatementDisplayData = postponedVatViewModel.createStatementDisplayData(statementWithIndex)
      statementDisplayDataShouldBeCorrect(result)
    }

    "correctly handle empty requestedStatements" in new Setup {
      val emptyViewModel: PostponedVatViewModel = PostponedVatViewModel(Seq(PostponedVatStatementsForEori(eoriHistory, Seq.empty, Seq.empty)))
      val result: Seq[(PostponedVatStatementsByMonth, Int)] = emptyViewModel.requestedStatements(0)
      result.length mustBe 0
    }

    "correctly handle single file in statement" in new Setup {
      val singleFileStatementsByMonth: PostponedVatStatementsByMonth = postponedVatStatementsByMonth_2.copy(files = Seq(postponedVatStatementFile))
      val result: Map[String, Seq[PostponedVatStatementFile]] = postponedVatViewModel.groupedStatements(singleFileStatementsByMonth)
      result.size mustBe 1
      result("CDS").length mustBe 1
      result("CDS").head mustBe postponedVatStatementFile
    }

    "correctly handle multiple files from the same source" in new Setup {
      val multipleFiles: Seq[PostponedVatStatementFile] = Seq(postponedVatStatementFile, postponedVatStatementFile_2)
      val multipleFilesStatementsByMonth: PostponedVatStatementsByMonth = postponedVatStatementsByMonth_2.copy(files = multipleFiles)
      val result: Map[String, Seq[PostponedVatStatementFile]] = postponedVatViewModel.groupedStatements(multipleFilesStatementsByMonth)
      result.size mustBe 1
      result("CDS").length mustBe 2
    }

    "correctly handle groupedStatements with multiple sources" in new Setup {
      val cdsFile: PostponedVatStatementFile = postponedVatStatementFile_2.copy(metadata = postponedVatStatementFile_2.metadata.copy(source = "CDS"))
      val chiefFile: PostponedVatStatementFile = postponedVatStatementFile_2.copy(metadata = postponedVatStatementFile_2.metadata.copy(source = "CHIEF"))
      val mixedStatementsByMonth: PostponedVatStatementsByMonth = postponedVatStatementsByMonth_2.copy(files = Seq(cdsFile, chiefFile))
      val result: Map[String, Seq[PostponedVatStatementFile]] = postponedVatViewModel.groupedStatements(mixedStatementsByMonth)

      result.size mustBe 2
      result("CDS") must contain only cdsFile
      result("CHIEF") must contain only chiefFile
    }
  }

  private def displayStatementsShouldBeCorrect(result: Seq[StatementDisplayData]): Assertion = {
    result.length mustBe 1
    result.head.monthYear.equalsIgnoreCase("october 2020") mustBe true
    result.head.monthYearId.equalsIgnoreCase("october-2020") mustBe true
    result.head.formattedMonth mustBe "October"
    result.head.sources.length mustBe 2
    result.head.index mustBe 0
  }

  private def statementDisplayDataShouldBeCorrect(result: StatementDisplayData): Assertion = {
    result.monthYear.equalsIgnoreCase("october 2020") mustBe true
    result.monthYearId.equalsIgnoreCase("october-2020") mustBe true
    result.formattedMonth mustBe "October"
    result.sources.length mustBe 2
    result.sources.find(_.source == "CDS").get.files.length mustBe 1
    result.sources.find(_.source == "CHIEF").get.files.length mustBe 0
    result.index mustBe 0
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
