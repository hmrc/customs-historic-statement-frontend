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

package navigation

import base.SpecBase
import controllers.routes
import models._
import pages._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator()

  "Navigator" when {

    "in Normal mode" must {
      "go to HistoricDateRequest from a page that doesn't exist in the route map" in {
        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, emptyUserAnswers, SecurityStatement) mustBe
          routes.HistoricDateRequestPageController.onPageLoad(NormalMode, SecurityStatement)
      }

      "go from HistoricDateRequest to CheckYourAnswers" in {
        navigator.nextPage(HistoricDateRequestPage(C79Certificate), NormalMode, emptyUserAnswers, C79Certificate) mustBe
          routes.CheckYourAnswersController.onPageLoad(C79Certificate)
      }
    }

    "in Check mode" must {
      "go to CheckYourAnswers from any page" in {
        navigator.nextPage(HistoricDateRequestPage(C79Certificate), CheckMode, emptyUserAnswers, C79Certificate) mustBe
          routes.CheckYourAnswersController.onPageLoad(C79Certificate)
      }
    }
  }
}
