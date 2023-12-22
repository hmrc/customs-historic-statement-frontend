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
import models.{C79Certificate, SecurityStatement, DutyDefermentStatement, PostponedVATStatement, FileRole}
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.ActionItem
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Actions, Value}

class CheckYourAnswersHelperSpec extends SpecBase {

  "rows" should {
    "return a sequence of historicDatesRow" in new Setup {

      val compare = List(SummaryListRow(Value(
        HtmlContent("October 2019 to October 2019"), ""),
        None,"", Some(Actions("", List(ActionItem("/customs/historic-statement/import-vat/change-request-date",
          HtmlContent("Change"), Some("statement period"), "", Map()))))))

      helperOb.rows(c79FileRole) mustBe compare
    }
  }

  "dateRows" should {
    "return correct date range string for C79Certificate file role" in new Setup {
      helperOb.dateRows(c79FileRole) mustBe Some(messages(app)("date.range","October 2019", "October 2019"))
    }
  }

  trait Setup {

    val c79FileRole: FileRole = C79Certificate
    val dutyFileRole: FileRole = DutyDefermentStatement
    val securityFileRole: FileRole = SecurityStatement
    val postFileRole: FileRole = PostponedVATStatement

    val app: Application = applicationBuilder(userAnswers = Some(populatedUserAnswers)).build()
    implicit val msgs: Messages = messages(app)
    val helperOb = new CheckYourAnswersHelper(populatedUserAnswers)
  }
}
