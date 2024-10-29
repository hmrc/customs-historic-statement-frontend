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
import models.{C79Certificate, CDSCashAccount, DutyDefermentStatement, PostponedVATStatement, SecurityStatement}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.RequestedLinkId
import play.api.Application

class FrontendAppConfigSpec extends SpecBase {

  "FrontendAppConfig" should {
    "contain correct values for the provided configuration" in new Setup {
      config.cashAccountForCdsDeclarationsUrl mustBe
        "https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations"
    }
  }

  "sdesCashStatementListUrl" should {
    "return correct sdes url" in new Setup {
      config.sdesCashStatementListUrl mustBe
        "http://localhost:9754/customs-financials-sdes-stub/files-available/list/CDSCashAccount"
    }
  }

  "returnLink" should {
    "return the adjustments link if a SecurityStatement FileRole provided" in new Setup {
      config.returnLink(SecurityStatement, emptyUserAnswers) mustBe
        "http://localhost:9398/customs/documents/adjustments"
    }

    "return the adjustments link if a C79Certificate FileRole provided" in new Setup {
      config.returnLink(C79Certificate, emptyUserAnswers) mustBe
        "http://localhost:9398/customs/documents/import-vat"
    }

    "return the cash account link if a CDSCashAccount FileRole and User Answers provided" in new Setup {
      config.returnLink(CDSCashAccount, emptyUserAnswers) mustBe
        "http://localhost:9394/customs/cash-account"
    }

    "return the DutyDeferment link if a DutyDeferment FileRole provided and linkId in user answers" in new Setup {
      config.returnLink(
        DutyDefermentStatement,
        emptyUserAnswers.set(RequestedLinkId, "someLink").success.value) mustBe
        "http://localhost:9397/customs/duty-deferment/someLink/account"
    }

    "return the cash account link if a CDSCashAccount FileRole provided" in new Setup {
      config.returnLink(CDSCashAccount) mustBe
        "http://localhost:9394/customs/cash-account"
    }

    "throw an exception if DutyDeferment FileRole and no linkId in user answers" in new Setup {
      intercept[Exception] {
        config.returnLink(DutyDefermentStatement, emptyUserAnswers) mustBe
          "http://localhost:9398/customs/documents/import-vat"
      }.getMessage mustBe "Unable to retrieve linkId"
    }

    "throw an exception if DutyDeferment fileRole is passed " in new Setup {
      intercept[Exception] {
        config.returnLink(DutyDefermentStatement) mustBe "http://localhost:9398/customs/documents/import-vat"
      }.getMessage mustBe "Invalid file role"
    }
  }

  "feedbackService" should {
    "return correct url" in new Setup {
      config.feedbackService mustBe "http://localhost:9514/feedback/CDS-FIN"
    }
  }

  "deleteNotificationUrl" should {
    "return correct url for cash account FileRole" in new Setup {
      config.deleteNotificationUrl(CDSCashAccount, "GB123456789000") mustBe
        "http://localhost:9878/customs-financials-api/eori/GB123456789000/notifications/CDSCashAccount"
    }

    "return correct url for C79 certificate FileRole" in new Setup {
      config.deleteNotificationUrl(C79Certificate, "GB123456789000") mustBe
        "http://localhost:9878/customs-financials-api/eori/GB123456789000/requested-notifications/C79Certificate"
    }

    "return correct url for Duty deferment statement FileRole" in new Setup {
      config.deleteNotificationUrl(DutyDefermentStatement, "GB123456789000") mustBe
        "http://localhost:9878/customs-financials-api/eori/GB123456789000/requested-notifications/DutyDefermentStatement"
    }

    "return correct url for Postponed VAT Statement FileRole" in new Setup {
      config.deleteNotificationUrl(PostponedVATStatement, "GB123456789000") mustBe
        "http://localhost:9878/customs-financials-api/eori/GB123456789000/requested-notifications/PostponedVATStatement"
    }

    "return correct url for Security statement FileRole" in new Setup {
      config.deleteNotificationUrl(SecurityStatement, "GB123456789000") mustBe
        "http://localhost:9878/customs-financials-api/eori/GB123456789000/requested-notifications/SecurityStatement"
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()
    val config: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  }
}
