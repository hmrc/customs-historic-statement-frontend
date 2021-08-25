/*
 * Copyright 2021 HM Revenue & Customs
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

import controllers.OrderedByEoriHistory
import helpers.Formatters
import models.FileFormat.Pdf
import models.{EoriHistory, VatCertificateFile}
import play.api.i18n.Messages

import java.time.LocalDate

case class VatViewModel(certificatesForAllEoris: Seq[VatCertificatesForEori]) {
  val hasRequestedCertificates: Boolean = certificatesForAllEoris.exists(_.requestedCertificates.nonEmpty)
  val hasCurrentCertificates: Boolean = certificatesForAllEoris.exists(_.currentCertificates.nonEmpty)
}

case class VatCertificatesForEori(eoriHistory: EoriHistory, currentCertificates: Seq[VatCertificatesByMonth], requestedCertificates: Seq[VatCertificatesByMonth])
  extends OrderedByEoriHistory[VatCertificatesForEori]

case class VatCertificatesByMonth(date: LocalDate, files: Seq[VatCertificateFile] = Seq.empty)(implicit messages: Messages)
  extends Ordered[VatCertificatesByMonth] {

  val formattedMonthYear: String = Formatters.dateAsMonthAndYear(date)
  val pdf: Option[VatCertificateFile] = files.find(_.fileFormat == Pdf)

  override def compare(that: VatCertificatesByMonth): Int = date.compareTo(that.date)
}