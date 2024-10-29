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

package services

import models._
import play.api.i18n.Messages
import viewmodels._

import javax.inject.{Inject, Singleton}

@Singleton
class SortStatementsService @Inject()() {

  def sortDutyDefermentStatementsForEori(historicEori: EoriHistory,
                                         dutyDefermentFiles: Seq[DutyDefermentStatementFile]): DutyDefermentStatementsForEori = {

    dutyDefermentFiles.partition(_.metadata.statementRequestId.isDefined) match {
      case (requested, current) => DutyDefermentStatementsForEori(historicEori, current, requested)
    }
  }

  def sortSecurityCertificatesForEori(historicEori: EoriHistory,
                                      securityStatementsFiles: Seq[SecurityStatementFile]): SecurityStatementsForEori = {

    val groupedByStartAndEndDates = securityStatementsFiles.groupBy(file => (file.startDate, file.endDate)).map {
      case ((startDate, endDate), filesForMonth) => SecurityStatementsByPeriod(startDate, endDate, filesForMonth)
    }.toList.sorted.reverse

    val (requested, current) = filteredByStatementReqId(groupedByStartAndEndDates).partition(_.files.nonEmpty)

    SecurityStatementsForEori(historicEori, current, requested)
  }

  def sortCashStatementsForEori(historicEori: EoriHistory, cashStatementFiles: Seq[CashStatementFile])
                               (implicit messages: Messages): CashStatementForEori = {

    val groupedByMonth = cashStatementFiles.groupBy(_.monthAndYear).map {
      case (month, filesForMonth) => CashStatementByMonth(month, filesForMonth)
    }.toList

    val filteredByStatementRequestId = groupedByMonth.map { statementByMonth =>
      val filteredFiles = statementByMonth.files.filter(_.metadata.statementRequestId.isEmpty)

      CashStatementByMonth(statementByMonth.date, filteredFiles)
    }

    val (requested, current) = filteredByStatementRequestId.partition(_.files.nonEmpty)

    CashStatementForEori(historicEori, current, requested)
  }

  def sortVatCertificatesForEori(historicEori: EoriHistory, vatCertificateFiles: Seq[VatCertificateFile])
                                (implicit messages: Messages): VatCertificatesForEori = {

    val groupedByMonth = vatCertificateFiles.groupBy(_.monthAndYear).map {
      case (month, filesForMonth) => VatCertificatesByMonth(month, filesForMonth)
    }.toList

    val filteredByStatementRequestId = groupedByMonth.map { statementByMonth =>
      val filteredFiles = statementByMonth.files.filter(_.metadata.statementRequestId.isDefined)

      VatCertificatesByMonth(statementByMonth.date, filteredFiles)
    }

    val (requested, current) = filteredByStatementRequestId.partition(_.files.nonEmpty)

    VatCertificatesForEori(historicEori, current, requested)
  }

  def sortPostponedVatStatementsForEori(historicEori: EoriHistory,
                                        postponedVatStatementsFile: Seq[PostponedVatStatementFile])
                                       (implicit messages: Messages): PostponedVatStatementsForEori = {

    val groupedByMonth = postponedVatStatementsFile.groupBy(_.monthAndYear).map {
      case (month, filesForMonth) => PostponedVatStatementsByMonth(month, filesForMonth)
    }.toList

    val filteredByStatementRequestId = groupedByMonth.map { statementByMonth =>
      val filteredFiles = statementByMonth.files.filter(_.metadata.statementRequestId.isDefined)

      PostponedVatStatementsByMonth(statementByMonth.date, filteredFiles)
    }

    val (requested, current) = filteredByStatementRequestId.partition(_.files.nonEmpty)

    PostponedVatStatementsForEori(historicEori, current, requested)
  }

  private def filteredByStatementReqId(securityStatements: List[SecurityStatementsByPeriod]): List[SecurityStatementsByPeriod] =

    securityStatements.map {
      statementsByStartAndEndDatePeriod =>

        val filteredFiles = statementsByStartAndEndDatePeriod.files.filter(_.metadata.statementRequestId.isDefined)

        SecurityStatementsByPeriod(
          statementsByStartAndEndDatePeriod.startDate,
          statementsByStartAndEndDatePeriod.endDate,
          filteredFiles)
    }
}
