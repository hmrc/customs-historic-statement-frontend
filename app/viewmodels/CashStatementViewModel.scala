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

import config.FrontendAppConfig
import controllers.OrderedByEoriHistory
import helpers.Formatters
import models.FileFormat.{Csv, Pdf}
import models.{CashStatementFile, EoriHistory, FileFormat}
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import utils.Utils.{ddComponent, divComponent, dlComponent, dtComponent, emptyString, h2Component, hmrcNewTabLinkComponent}
import views.html.components.download_link_cash_account

import java.time.LocalDate

case class CashStatementViewModel(statementsForAllEoris: Seq[CashStatementForEori])

case class CashStatementForEori(eoriHistory: EoriHistory,
                                currentStatements: Seq[CashStatementMonthToMonth],
                                requestedStatements: Seq[CashStatementMonthToMonth])
  extends OrderedByEoriHistory[CashStatementForEori]

case class CashStatementMonthToMonth(startDate: LocalDate, endDate: LocalDate, files: Seq[CashStatementFile] = Seq.empty)
                                    (implicit messages: Messages) extends Ordered[CashStatementMonthToMonth] {

  val formattedMonthToMonth: String = files.headOption.map { file =>
    Formatters.periodAsStartToEndMonth(startDate.getMonthValue, endDate.getMonthValue)
  }.getOrElse(emptyString)

  val cashAccountNumber: Option[String] = files.headOption.flatMap(_.metadata.cashAccountNumber)
  val formattedMonthYear: String = Formatters.dateAsMonthAndYear(startDate)

  val pdf: Option[CashStatementFile] = files.find(_.fileFormat == Pdf)
  val csv: Option[CashStatementFile] = files.find(_.fileFormat == Csv)

  override def compare(that: CashStatementMonthToMonth): Int = {
    val startComparison = this.startDate.compareTo(that.startDate)
    if (startComparison != 0) startComparison else this.endDate.compareTo(that.endDate)
  }
}

case class GroupedStatementsByEori(eoriIndex: Int,
                                   eoriHistory: EoriHistory,
                                   statementsByYear: Map[Int, Seq[CashStatementMonthToMonth]])

object CashStatementViewModel {

  def helpAndSupport(implicit appConfig: FrontendAppConfig, messages: Messages): HtmlFormat.Appendable = {

    val heading = h2Component(
      id = Some("search-transactions-support-message-heading"),
      msg = "site.support.heading",
      classes = "govuk-heading-m govuk-!-padding-top-9")

    val link = hmrcNewTabLinkComponent(
      linkMessage = "cf.help-and-support.link.text",
      href = appConfig.cashAccountForCdsDeclarationsUrl,
      preLinkMessage = Some("cf.help-and-support.link.text.pre"),
      postLinkMessage = Some("cf.help-and-support.link.text.post"))

    HtmlFormat.fill(Seq(heading, link))
  }

  def generateCashAccountHeading(model: CashStatementViewModel)(implicit messages: Messages): Html = {
    val cashAccountNumberHtml = getCashAccountHeading(model)

    if (cashAccountNumberHtml.body.nonEmpty) {
      cashAccountNumberHtml
    } else {
      Html(emptyString)
    }
  }

  def getRequestedStatementsGroupedByYear(model: CashStatementViewModel): Seq[GroupedStatementsByEori] = {
    model.statementsForAllEoris.zipWithIndex.collect {
      case (eoriStatements, index) if eoriStatements.requestedStatements.nonEmpty =>
        GroupedStatementsByEori(
          eoriIndex = index,
          eoriHistory = eoriStatements.eoriHistory,
          statementsByYear = eoriStatements.requestedStatements.groupBy(_.startDate.getYear))
    }
  }

  def generateStatementsByYear(groupedStatements: GroupedStatementsByEori)(implicit messages: Messages): Html = {
    val statementRows = groupedStatements.statementsByYear.toSeq.sortBy(_._1).map {
      case (year, statementsOfYear) =>
        val statementList = createStatementList(groupedStatements.eoriIndex, statementsOfYear)
        val yearHeading = h2Component(
          msg = year.toString,
          classes = "govuk-heading-s govuk-!-margin-bottom-0 govuk-!-margin-top-7")

        Html(yearHeading.body + statementList.body)
    }.mkString

    Html(statementRows)
  }

  private def getCashAccountHeading(model: CashStatementViewModel)(implicit messages: Messages): Html = {
    model.statementsForAllEoris
      .flatMap(_.requestedStatements)
      .flatMap(_.cashAccountNumber)
      .headOption match {
      case Some(cashAccountNumber) => h2Component(
        msg = messages("cf.cash-statement-requested-account-heading", cashAccountNumber),
        classes = "govuk-caption-xl")

      case None => Html(emptyString)
    }
  }

  private def createStatementList(eoriIndex: Int, statementsOfYear: Seq[CashStatementMonthToMonth])
                                 (implicit messages: Messages): Html = {
    val statementListItems = statementsOfYear.sorted.zipWithIndex.map {
      case (statement, index) => createStatementRow(eoriIndex, statement, index).body
    }.mkString

    dlComponent(
      content = Html(statementListItems),
      id = Some(s"requested-statements-list-$eoriIndex"),
      classes = Some("govuk-summary-list statement-list"))
  }

  private def createStatementRow(eoriIndex: Int, statement: CashStatementMonthToMonth, index: Int)
                                (implicit messages: Messages): Html = {

    val dateCell = dtComponent(
      content = Html(statement.formattedMonthToMonth),
      id = Some(s"requested-statements-list-$eoriIndex-row-$index-date-cell"),
      classes = Some("govuk-summary-list__value"))

    val downloadLink = createDownloadLinks(eoriIndex, statement, index)

    val downloadCell = ddComponent(
      content = Html(downloadLink),
      id = Some(s"requested-statements-list-$eoriIndex-row-$index-link-cell"),
      classes = Some("govuk-summary-list__actions"))

    divComponent(
      content = Html(dateCell.body + downloadCell.body),
      id = Some(s"requested-statements-list-$eoriIndex-row-$index"),
      classes = Some("govuk-summary-list__row"))
  }

  private def createDownloadLinks(eoriIndex: Int, statement: CashStatementMonthToMonth, index: Int)
                                 (implicit messages: Messages): String = {

    val groupedFiles = statement.files.groupBy(_.fileFormat)

    groupedFiles.flatMap { case (format, files) =>
      files.zipWithIndex.map { case (file, fileIndex) =>
        downloadLink(
          file = Some(file),
          format = format.toString,
          id = s"requested-statements-list-$eoriIndex-row-$index-${format.toString.toLowerCase}-$fileIndex-download-link",
          period = statement.formattedMonthToMonth).body
      }
    }.mkString
  }

  private def downloadLink(file: Option[CashStatementFile], format: String, id: String, period: String)
                          (implicit messages: Messages): Html = {
    file match {
      case Some(f) => new download_link_cash_account().apply(
        file = Some(f),
        fileFormat = FileFormat(format),
        id = id,
        period = period)

      case None => Html(emptyString)
    }
  }
}
