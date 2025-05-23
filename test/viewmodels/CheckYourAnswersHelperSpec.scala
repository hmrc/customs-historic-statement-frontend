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

import base.SpecBase
import models.{C79Certificate, DutyDefermentStatement, FileRole, PostponedVATStatement, SecurityStatement}
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Value}
import utils.Utils.emptyString

class CheckYourAnswersHelperSpec extends SpecBase {

  "rows" should {
    "return a sequence of historicDatesRow" in new Setup {

      val compare = List(
        SummaryListRow(
          Value(HtmlContent("March 2018 to March 2018"), emptyString),
          None,
          emptyString,
          Some(
            Actions(
              emptyString,
              List(
                ActionItem(
                  "/customs/historic-statement/import-vat/change-request-date",
                  HtmlContent("Change"),
                  Some("statement period"),
                  emptyString,
                  Map()
                )
              )
            )
          )
        )
      )

      helperOb.rows(c79FileRole) mustBe compare
    }
  }

  "dateRows" should {
    "return correct date range string for C79Certificate file role" in new Setup {
      helperOb.dateRows(c79FileRole) mustBe Some(messages("date.range", "March 2018", "March 2018"))
    }
  }

  trait Setup {
    val c79FileRole: FileRole      = C79Certificate
    val dutyFileRole: FileRole     = DutyDefermentStatement
    val securityFileRole: FileRole = SecurityStatement
    val postFileRole: FileRole     = PostponedVATStatement
    val helperOb                   = new CheckYourAnswersHelper(populatedUserAnswers)
  }
}
