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

import base.SpecBase

class DateMessagesSpec extends SpecBase {

  "DateMessages" should {

    "populate the model correctly" when {

      "fileRole is DutyDefermentStatement" in {
        val actual = DateMessages(DutyDefermentStatement)

        actual.startDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.from",
          hintMsgKey = "cf.historic.document.request.date.DutyDefermentStatement.hint")

        actual.endDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.to",
          hintMsgKey = "cf.historic.document.request.endDate.hint")
      }

      "fileRole is C79Certificate" in {
        val actual = DateMessages(C79Certificate)

        actual.startDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.from",
          hintMsgKey = "cf.historic.document.request.date.C79Certificate.hint")

        actual.endDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.to",
          hintMsgKey = "cf.historic.document.request.endDate.hint")
      }

      "fileRole is CDSCashAccount" in {
        val actual = DateMessages(CDSCashAccount)

        actual.startDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.from",
          hintMsgKey = "cf.historic.document.request.date.CashStatement.hint")

        actual.endDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.to",
          hintMsgKey = "cf.historic.document.request.endDate.hint")
      }

      "fileRole is SecurityStatement" in {
        val actual = DateMessages(SecurityStatement)

        actual.startDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.from",
          hintMsgKey = "cf.historic.document.request.date.SecurityStatement.hint")

        actual.endDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.to",
          hintMsgKey = "cf.historic.document.request.endDate.hint")
      }

      "fileRole is PostponedVATStatement" in {
        val actual = DateMessages(PostponedVATStatement)

        actual.startDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.from",
          hintMsgKey = "cf.historic.document.request.date.PostponedVATStatement.hint")

        actual.endDate mustBe DateMessage(
          labelMsgKey = "cf.historic.document.request.to",
          hintMsgKey = "cf.historic.document.request.endDate.hint")
      }
    }
  }
}
