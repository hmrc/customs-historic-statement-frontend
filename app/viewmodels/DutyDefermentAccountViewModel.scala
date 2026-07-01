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

package viewmodels

import helpers.Formatters
import models.DDStatementType.{DutyDeferment, Excise, ExciseDeferment, Supplementary, UnknownStatementType, Weekly}
import models.FileFormat.Pdf
import models.{
  DDStatementType, DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata,
  DutyDefermentStatementPeriod, DutyDefermentStatementPeriodsByMonth, DutyDefermentStatementsForEori
}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.Utils.*
import views.html.components.duty_deferment_file

case class DutyDefermentAccountRowContent(
  statement: DutyDefermentAccountStatement,
  period: DutyDefermentStatementPeriod,
  index: Int
)

case class DutyDefermentAccountComponent(
  accountHeading: HtmlFormat.Appendable,
  eoriHeading: HtmlFormat.Appendable,
  monthHeading: HtmlFormat.Appendable,
  statements: HtmlFormat.Appendable,
  missingDocumentsGuidance: HtmlFormat.Appendable
)

case class DutyDefermentAccountStatement(
  historyIndex: Int,
  groupIndex: Int,
  eorisStatements: Seq[DutyDefermentStatementsForEori],
  group: DutyDefermentStatementPeriodsByMonth,
  periodsWithIndex: Seq[(DutyDefermentStatementPeriod, Int)],
  isNiAccount: Boolean,
  accountNumber: String
)

case class DutyDefermentAccountViewModel(
  statementsForAllEoris: Seq[DutyDefermentStatementsForEori],
  statementsData: Seq[DutyDefermentAccountStatement],
  component: DutyDefermentAccountComponent
)

object DutyDefermentAccountViewModel {

  def apply(accountNumber: String, statementsForAllEoris: Seq[DutyDefermentStatementsForEori], isNiAccount: Boolean)(
    implicit messages: Messages
  ): DutyDefermentAccountViewModel = {

    val filledStatementData = addEmptyStatements(statementsForAllEoris)
    val statementsData      = createStatements(filledStatementData, isNiAccount, accountNumber)
    val components          = DutyDefermentAccountComponent(statementsData.head)

    DutyDefermentAccountViewModel(
      statementsForAllEoris,
      statementsData,
      components
    )
  }

  private def createStatements(
    statementsForAllEoris: Seq[DutyDefermentStatementsForEori],
    isNiAccount: Boolean,
    accountNumber: String
  ): Seq[DutyDefermentAccountStatement] =
    for {
      (eorisStatements, historyIndex) <- statementsForAllEoris.zipWithIndex.reverse
      (group, groupIndex)             <- eorisStatements.groupsRequested.zipWithIndex.reverse
    } yield DutyDefermentAccountStatement(
      historyIndex,
      groupIndex,
      Seq(eorisStatements),
      group,
      group.periods.reverse.zipWithIndex,
      isNiAccount,
      accountNumber
    )

  private def addEmptyStatements(
    statementList: Seq[DutyDefermentStatementsForEori]
  ): Seq[DutyDefermentStatementsForEori] =
    statementList.map { statementForEori =>

      val grouped = statementForEori.requestedStatements
        .groupBy(statement => (statement.metadata.periodStartYear, statement.metadata.periodStartMonth))
        .map { case ((year, month), items) =>
          val filled = fillMissingStatements(items, year, month)
          (year, month) -> filled
        }

      statementForEori.copy(requestedStatements = grouped.values.flatten.toSeq)
    }

  private def fillMissingStatements(
    inner: Seq[DutyDefermentStatementFile],
    startYear: Int,
    startMonth: Int
  ): Seq[DutyDefermentStatementFile] = {

    val (weeklyPeriods, nonWeeklyPeriods) = inner.partition { statementFile =>
      statementFile.metadata.defermentStatementType == Weekly
    }

    val weeklyPeriodsBySeq = weeklyPeriods.map(r => r.metadata.periodIssueNumber -> r).toMap

    val completedWeekly = (1 to 4).map { seq =>
      weeklyPeriodsBySeq.getOrElse(
        seq,
        createNewEmptyStatementFile(seq, startYear, startMonth, Weekly)
      )
    }

    val expectedWeeklyTypes = Seq(Supplementary, Excise, DutyDeferment, ExciseDeferment)

    val nonWeeklyByType =
      nonWeeklyPeriods.map(period => period.metadata.defermentStatementType -> period).toMap

    val completeNonWeekly = expectedWeeklyTypes.map { statementType =>
      nonWeeklyByType.getOrElse(
        statementType,
        createNewEmptyStatementFile(statementType.order, startYear, startMonth, statementType)
      )
    }

    completedWeekly ++ completeNonWeekly
  }

  private def createNewEmptyStatementFile(
    sequence: Int,
    startYear: Int,
    startMonth: Int,
    statementType: DDStatementType
  ): DutyDefermentStatementFile =
    DutyDefermentStatementFile(
      filename = "",
      downloadURL = "",
      size = 0,
      createNewEmptyStatement(sequence, startYear, startMonth, statementType)
    )

  private def createNewEmptyStatement(
    sequence: Int,
    startYear: Int,
    startMonth: Int,
    statementType: DDStatementType
  ): DutyDefermentStatementFileMetadata =
    DutyDefermentStatementFileMetadata(
      periodStartYear = startYear,
      periodStartMonth = startMonth,
      periodStartDay = sequence,
      periodEndYear = startYear,
      periodEndMonth = startMonth,
      periodEndDay = sequence,
      periodIssueNumber = sequence,
      fileFormat = Pdf,
      fileRole = DutyDefermentStatement,
      defermentStatementType = statementType,
      dutyOverLimit = None,
      dutyPaymentType = None,
      dan = "",
      statementRequestId = None,
      available = false
    )
}

object DutyDefermentAccountComponent {

  def apply(statement: DutyDefermentAccountStatement)(implicit messages: Messages): DutyDefermentAccountComponent =
    DutyDefermentAccountComponent(
      accountHeading = createAccountHeading(statement.isNiAccount, statement.accountNumber),
      eoriHeading = createEoriHeading(statement),
      monthHeading = createMonthHeading(statement),
      statements = createStatements(statement),
      missingDocumentsGuidance = createMissingDocumentsGuidance()
    )

  private def createAccountHeading(isNiAccount: Boolean, accountNumber: String)(implicit
    messages: Messages
  ): HtmlFormat.Appendable = {

    val msgKey = if (isNiAccount) {
      "cf.account.detail.requested.deferment-account-secondary-heading.NiAccount"
    } else {
      "cf.account.detail.requested.deferment-account-secondary-heading"
    }

    h2Component(
      msg = msgKey,
      id = Some("eori-heading"),
      classes = "govuk-caption-xl",
      extraContent = Some(accountNumber)
    )
  }

  private def createEoriHeading(
    statement: DutyDefermentAccountStatement
  )(implicit messages: Messages): HtmlFormat.Appendable = {

    val eori = statement.eorisStatements.head.eoriHistory.eori

    h2Component(
      id = Some(s"historic-eori-${statement.historyIndex}"),
      classes = "govuk-heading-s",
      msg = messages("cf.account.details.previous-eori", eori)
    )
  }

  private def createMonthHeading(statement: DutyDefermentAccountStatement)(implicit
    messages: Messages
  ): HtmlFormat.Appendable =
    h3Component(
      id = Some(
        s"requested-statements-month-heading-" +
          s"${statement.historyIndex}-" +
          s"${statement.group.year}-${statement.group.month}"
      ),
      msg = Formatters.dateAsMonthAndYear(statement.group.monthAndYear)
    )

  private def createStatements(
    statement: DutyDefermentAccountStatement
  )(implicit messages: Messages): HtmlFormat.Appendable = {

    val result = statement.periodsWithIndex.map { case (period, index) =>
      statementRow(DutyDefermentAccountRowContent(statement, period, index))
    }.mkString

    dlComponent(
      content = HtmlFormat.raw(result),
      classes = Some("govuk-summary-list")
    )
  }

  private def createMissingDocumentsGuidance()(implicit messages: Messages): HtmlFormat.Appendable =
    missingDocumentsGuidanceComponent("statement")

  private def statementRow(data: DutyDefermentAccountRowContent)(implicit messages: Messages): HtmlFormat.Appendable =
    divComponent(
      content = HtmlFormat.fill(List(statementTypes(data), periodDetails(data), dutyDefermentFile(data))),
      classes = Some("govuk-summary-list__row"),
      id = Some(
        s"requested-statements-list-" +
          s"${data.statement.historyIndex}-${data.statement.group.year}-" +
          s"${data.statement.group.month}-row-${data.index}"
      )
    )

  private def periodDetails(data: DutyDefermentAccountRowContent)(implicit messages: Messages): HtmlFormat.Appendable =
    ddComponent(
      content = preparePeriodDetails(data.period),
      classes = Some("govuk-summary-list__value"),
      id = Some(
        s"requested-statements-list-" +
          s"${data.statement.historyIndex}-${data.statement.group.year}-" +
          s"${data.statement.group.month}-row-${data.index}-date-cell"
      )
    )

  private def statementTypes(data: DutyDefermentAccountRowContent)(implicit messages: Messages): HtmlFormat.Appendable =
    dtComponent(
      content = prepareStatementTypeDetails(data.period),
      classes = Some("govuk-summary-list__value"),
      id = Some(
        s"requested-statements-list-" +
          s"${data.statement.historyIndex}-${data.statement.group.year}-" +
          s"${data.statement.group.month}-row-${data.index}-type-cell"
      )
    )

  private def dutyDefermentFile(data: DutyDefermentAccountRowContent)(implicit
    messages: Messages
  ): HtmlFormat.Appendable =
    ddComponent(
      content = prepareDutyDefermentFile(data),
      classes = Some("govuk-summary-list__actions"),
      id = Some(
        s"requested-statements-list-" +
          s"${data.statement.historyIndex}-${data.statement.group.year}-" +
          s"${data.statement.group.month}-row-${data.index}-link-cell"
      )
    )

  private def prepareStatementTypeDetails(period: DutyDefermentStatementPeriod)(implicit
    messages: Messages
  ): HtmlFormat.Appendable = {

    val msgKey = period.defermentStatementType match {
      case Supplementary   => "cf.account.detail.row.supplementary.info"
      case Excise          => "cf.account.details.row.excise.info"
      case DutyDeferment   => "cf.account.details.row.duty-deferment.info"
      case ExciseDeferment => "cf.account.details.row.excise-deferment.info"
      case _               => "cf.account.detail.row.period-group.info"
    }

    HtmlFormat.raw(
      messages(
        msgKey,
        period.periodIssueNumber
      )
    )
  }

  private def preparePeriodDetails(
    period: DutyDefermentStatementPeriod
  )(implicit messages: Messages): HtmlFormat.Appendable =
    period.defermentStatementType match {
      case Weekly if period.available =>
        HtmlFormat.raw(
          messages(
            "cf.account.detail.row.period-group",
            Formatters.dateAsDay(period.startDate),
            Formatters.dateAsDay(period.endDate),
            Formatters.dateAsMonth(period.endDate)
          )
        )
      case _ if !period.available     =>
        HtmlFormat.raw(
          messages(
            "cf.account.detail.no-statement"
          )
        )
      case _                          => HtmlFormat.raw("")

    }

  private def prepareDutyDefermentFile(data: DutyDefermentAccountRowContent)(implicit
    messages: Messages
  ): HtmlFormat.Appendable =
    if (data.period.available) {
      new duty_deferment_file().apply(
        period = data.period,
        fileFormat = Pdf,
        idPrefix = s"requested-statements-list-" +
          s"${data.statement.historyIndex}-${data.statement.group.year}-" +
          s"${data.statement.group.month}-row-${data.index}"
      )
    } else {
      HtmlFormat.raw(
        messages(
          "cf.account.detail.unavailable"
        )
      )
    }
}
