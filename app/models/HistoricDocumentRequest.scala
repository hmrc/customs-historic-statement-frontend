/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import models.requests.DataRequest
import pages.{AccountNumber, HistoricDateRequestPage}
import play.api.libs.json.{Json, OFormat}
import play.api.mvc.AnyContent

import java.time.LocalDate

case class HistoricDocumentRequest(
                                    documentType: FileRole,
                                    from: LocalDate,
                                    until: LocalDate,
                                    dan: Option[String]
                                  )

object HistoricDocumentRequest {
  implicit val format: OFormat[HistoricDocumentRequest] = Json.format[HistoricDocumentRequest]

  def fromRequest(fileRole: FileRole)(implicit request: DataRequest[AnyContent]): Option[HistoricDocumentRequest] = {
    for {
      dates <- request.userAnswers.get(HistoricDateRequestPage(fileRole))
      accountNumber = request.userAnswers.get(AccountNumber)
    } yield HistoricDocumentRequest(fileRole, dates.start, dates.end, accountNumber)
  }
}
