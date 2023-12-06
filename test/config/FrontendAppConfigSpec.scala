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

package config

import base.SpecBase
import models.{C79Certificate, DutyDefermentStatement, SecurityStatement}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.RequestedLinkId
import play.api.test.Helpers.running

class FrontendAppConfigSpec extends SpecBase {

  "returnLink" should {
    "return the adjustments link if a SecurityStatement FileRole provided" in new Setup {
      running(app){
        config.returnLink(SecurityStatement, emptyUserAnswers) mustBe "http://localhost:9398/customs/documents/adjustments"
      }
    }
    

    "return the adjustments link if a C79Certificate FileRole provided" in new Setup {
      running(app) {
        config.returnLink(C79Certificate, emptyUserAnswers) mustBe "http://localhost:9398/customs/documents/import-vat"
      }
    }

    "return the DutyDeferment link if a DutyDeferment FileRole provided and linkId in user answers" in new Setup {
      
      running(app) {
        config.returnLink(
          DutyDefermentStatement,
          emptyUserAnswers.set(RequestedLinkId, "someLink").success.value) mustBe "http://localhost:9397/customs/duty-deferment/someLink/account"
      }
    }

    "throw an exception if DutyDeferment FileRole and no linkId in user answers" in new Setup {
      running(app) {
        intercept[Exception] {
          config.returnLink(DutyDefermentStatement, emptyUserAnswers) mustBe "http://localhost:9398/customs/documents/import-vat"
        }.getMessage mustBe "Unable to retrieve linkId"
      }
    }

    "throw an exception if DutyDeferment fileRole is passed " in new Setup {
      running(app) {
        intercept[Exception] {
          config.returnLink(DutyDefermentStatement) mustBe "http://localhost:9398/customs/documents/import-vat"
        }.getMessage mustBe "Invalid file role"
      }
    }
  }

  trait Setup {
    val app = applicationBuilder().build()
    val config = app.injector.instanceOf[FrontendAppConfig]
  }
}
