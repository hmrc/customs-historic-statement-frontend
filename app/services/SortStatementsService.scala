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
import viewmodels.{PostponedVatStatementsByMonth, PostponedVatStatementsForEori, VatCertificatesByMonth, VatCertificatesForEori}

import javax.inject.{Inject, Singleton}

@Singleton
class SortStatementsService @Inject()() {

  def sortDutyDefermentStatementsForEori(historicEori: EoriHistory,
                                         dutyDefermentFiles: Seq[DutyDefermentStatementFile]): DutyDefermentStatementsForEori = {
    dutyDefermentFiles.partition(_.metadata.statementRequestId.isEmpty) match {
      case (current, requested) => DutyDefermentStatementsForEori(historicEori, current, requested)
    }
  }

  def sortSecurityCertificatesForEori(historicEori: EoriHistory,
                                      securityStatementsFiles: Seq[SecurityStatementFile]): SecurityStatementsForEori = {
    securityStatementsFiles.groupBy(file => (file.startDate, file.endDate)).map {
      case ((startDate, endDate), filesForMonth) => SecurityStatementsByPeriod(startDate, endDate, filesForMonth)
    }.toList.sorted.reverse
      .partition(_.files.exists(v => v.metadata.statementRequestId.isEmpty)) match {
      case (current, requested) => SecurityStatementsForEori(historicEori, current, requested)
    }
  }

  def sortVatCertificatesForEori(historicEori: EoriHistory, vatCertificateFiles: Seq[VatCertificateFile])
                                (implicit messages: Messages): VatCertificatesForEori =
    vatCertificateFiles.groupBy(_.monthAndYear).map {
      case (month, filesForMonth) => VatCertificatesByMonth(month, filesForMonth)
    }.toList
      .partition(_.files.exists(_.metadata.statementRequestId.isEmpty)) match {
      case (current, requested) => VatCertificatesForEori(historicEori, current, requested)
    }

  def sortPostponedVatStatementsForEori(historicEori: EoriHistory, postponedVatStatementsFile: Seq[PostponedVatStatementFile])
                                (implicit messages: Messages): PostponedVatStatementsForEori =
    postponedVatStatementsFile.groupBy(_.monthAndYear).map {
      case (month, filesForMonth) => PostponedVatStatementsByMonth(month, filesForMonth)
    }.toList
      .partition(_.files.exists(_.metadata.statementRequestId.isEmpty)) match {
      case (current, requested) => PostponedVatStatementsForEori(historicEori, current, requested)
    }
}
