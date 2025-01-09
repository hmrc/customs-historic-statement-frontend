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
import models.DDStatementType.{DutyDeferment, Excise, ExciseDeferment, Supplementary}
import models.FileFormat.Pdf
import models.{DutyDefermentStatementPeriod, DutyDefermentStatementPeriodsByMonth, DutyDefermentStatementsForEori}
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.Utils._
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

    val statementsData = createStatements(statementsForAllEoris, isNiAccount, accountNumber)
    val components     = DutyDefermentAccountComponent(statementsData.head)

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
      content = HtmlFormat.fill(List(periodDetails(data), dutyDefermentFile(data))),
      classes = Some("govuk-summary-list__row"),
      id = Some(
        s"requested-statements-list-" +
          s"${data.statement.historyIndex}-${data.statement.group.year}-" +
          s"${data.statement.group.month}-row-${data.index}"
      )
    )

  private def periodDetails(data: DutyDefermentAccountRowContent)(implicit messages: Messages): HtmlFormat.Appendable =
    dtComponent(
      content = preparePeriodDetails(data.period),
      classes = Some("govuk-summary-list__value"),
      id = Some(
        s"requested-statements-list-" +
          s"${data.statement.historyIndex}-${data.statement.group.year}-" +
          s"${data.statement.group.month}-row-${data.index}-date-cell"
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

  private def preparePeriodDetails(
    period: DutyDefermentStatementPeriod
  )(implicit messages: Messages): HtmlFormat.Appendable = {
    val msgKey = period.defermentStatementType match {
      case Supplementary   => "cf.account.detail.row.supplementary.info"
      case Excise          => "cf.account.details.row.excise.info"
      case DutyDeferment   => "cf.account.details.row.duty-deferment.info"
      case ExciseDeferment => "cf.account.details.row.excise-deferment.info"
      case _               => "cf.account.detail.period-group"
    }

    HtmlFormat.raw(
      messages(
        msgKey,
        Formatters.dateAsDay(period.startDate),
        Formatters.dateAsDay(period.endDate),
        Formatters.dateAsMonth(period.endDate)
      )
    )
  }

  private def prepareDutyDefermentFile(data: DutyDefermentAccountRowContent)(implicit
    messages: Messages
  ): HtmlFormat.Appendable =
    new duty_deferment_file().apply(
      period = data.period,
      fileFormat = Pdf,
      idPrefix = s"requested-statements-list-" +
        s"${data.statement.historyIndex}-${data.statement.group.year}-" +
        s"${data.statement.group.month}-row-${data.index}"
    )
}
