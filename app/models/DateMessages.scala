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

package models

case class DateMessage(labelMsgKey: String, hintMsgKey: String)

case class DateMessages(startDate: DateMessage, endDate: DateMessage)

object DateMessages {

  def apply(fileRole: FileRole): DateMessages =
    fileRole match {
      case CDSCashAccount =>
        DateMessages(
          startDate =
            DateMessage("cf.historic.document.request.from", "cf.historic.document.request.date.CashStatement.hint"),
          endDate = DateMessage("cf.historic.document.request.to", "cf.historic.document.request.endDate.hint")
        )

      case C79Certificate =>
        DateMessages(
          startDate =
            DateMessage("cf.historic.document.request.from", "cf.historic.document.request.date.C79Certificate.hint"),
          endDate = DateMessage("cf.historic.document.request.to", "cf.historic.document.request.endDate.hint")
        )

      case PostponedVATStatement =>
        DateMessages(
          startDate = DateMessage(
            "cf.historic.document.request.from",
            "cf.historic.document.request.date.PostponedVATStatement.hint"
          ),
          endDate = DateMessage("cf.historic.document.request.to", "cf.historic.document.request.endDate.hint")
        )

      case DutyDefermentStatement =>
        DateMessages(
          startDate = DateMessage(
            "cf.historic.document.request.from",
            "cf.historic.document.request.date.DutyDefermentStatement.hint"
          ),
          endDate = DateMessage("cf.historic.document.request.to", "cf.historic.document.request.endDate.hint")
        )

      case SecurityStatement =>
        DateMessages(
          startDate = DateMessage(
            "cf.historic.document.request.from",
            "cf.historic.document.request.date.SecurityStatement.hint"
          ),
          endDate = DateMessage("cf.historic.document.request.to", "cf.historic.document.request.endDate.hint")
        )
    }
}
