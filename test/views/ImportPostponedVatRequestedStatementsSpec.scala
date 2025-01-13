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
import viewmodels.{
  PostponedVatStatementsByMonth, PostponedVatStatementsForEori, PostponedVatViewModel, SourceDisplay,
  StatementDisplayData
}
import views.html.ImportPostponedVatRequestedStatements
import utils.TestData.*
import java.time.LocalDate

class ImportPostponedVatRequestedStatementsSpec extends ViewTestHelper {

  "view" should {
    "display correct title and contents" in new Setup {
      titleShouldBeCorrect(view, "cf.import-postponed-vat.requested.title")
      pageShouldContainBackLinkUrl(view, appConfig.returnLink("postponedVATStatement"))
      headingShouldBeCorrect
      requestedAvailableTextShouldBeCorrect
    }
  }

  "PostponedVatViewModel" should {
    "correctly handle statementsDisplayData" in new Setup {
      displayStatementsShouldBeCorrect(viewModel.statementDisplayData)
    }

    "correctly group statements by source" in new Setup {
      val cdsSource: SourceDisplay = statementData.sources.find(_.source == "CDS").get

      statementData.sources.length mustBe 2
      cdsSource.files.length mustBe 1
      cdsSource.files.head mustBe postponedVatStatementFile_2
    }

    "correctly display statement content" in new Setup {
      statementData.statementItem.body must include(monthAndYear)
      statementData.statementItem.body must include(postponedVatStatementFile.downloadURL)
    }

    "correctly handle missing files" in new Setup {
      statementData.statementItem.body must include(messages("cf.account.postponed-vat.missing-file-type", "CHIEF"))
    }
  }

  private def displayStatementsShouldBeCorrect(result: Seq[StatementDisplayData]): Assertion = {
    result.length mustBe 1
    result.head.monthYear.equalsIgnoreCase("March 2018") mustBe true
    result.head.monthYearId.equalsIgnoreCase("March-2018") mustBe true
    result.head.formattedMonth mustBe "March"
    result.head.sources.length mustBe 2
    result.head.sources.head.source mustBe "CDS"
    result.head.index mustBe 0
  }

  private def headingShouldBeCorrect(implicit view: Document): Assertion =
    view
      .getElementById("requested-import-postponed-vat-statements-heading")
      .html()
      .contains(
        messages("cf.import-postponed-vat.requested.title")
      ) mustBe true

  private def requestedAvailableTextShouldBeCorrect(implicit view: Document): Assertion =
    view
      .getElementById("available-text")
      .html()
      .contains(
        messages("cf.import-postponed-vat.requested.available.text")
      ) mustBe true

  trait Setup {
    protected val monthAndYear = "March 2018"
    private val size           = 113L
    private val source: String = "CDS"

    protected val eoriHistory: EoriHistory = EoriHistory(
      eori,
      Some(LocalDate.of(year, month, day)),
      Some(LocalDate.of(year, month_2, day))
    )

    protected val postponedVatStatementFile: PostponedVatStatementFile = PostponedVatStatementFile(
      fileName,
      downloadUrl,
      size,
      PostponedVatStatementFileMetadata(
        periodStartYear,
        periodStartMonth,
        Pdf,
        PostponedVATStatement,
        source,
        Some(requestId)
      )
    )

    protected val postponedVatStatementFile_2: PostponedVatStatementFile = PostponedVatStatementFile(
      fileName,
      downloadUrl,
      size,
      PostponedVatStatementFileMetadata(
        periodStartYear,
        periodStartMonth_2,
        Pdf,
        PostponedVATStatement,
        source,
        Some(requestId)
      )
    )

    private val postponedVatStatementsByMonth_1 = PostponedVatStatementsByMonth(
      LocalDate.of(year, month, day),
      Seq(postponedVatStatementFile)
    )

    protected val postponedVatStatementsByMonth_2: PostponedVatStatementsByMonth = PostponedVatStatementsByMonth(
      LocalDate.of(year, month_2, day),
      Seq(postponedVatStatementFile_2)
    )

    private val postponedVatStatementsForEori: PostponedVatStatementsForEori = PostponedVatStatementsForEori(
      eoriHistory,
      Seq(postponedVatStatementsByMonth_1),
      Seq(postponedVatStatementsByMonth_2)
    )

    protected val postponedVatViewModel: PostponedVatViewModel =
      PostponedVatViewModel(Seq(postponedVatStatementsForEori))

    implicit val view: Document = Jsoup.parse(
      application.injector
        .instanceOf[ImportPostponedVatRequestedStatements]
        .apply(postponedVatViewModel, appConfig.returnLink("postponedVATStatement"))
        .body
    )

    protected val viewModel: PostponedVatViewModel                = PostponedVatViewModel(Seq(postponedVatStatementsForEori))
    protected val statementDisplayData: Seq[StatementDisplayData] = viewModel.statementDisplayData
    protected val statementData: StatementDisplayData             = statementDisplayData.head
  }
}
