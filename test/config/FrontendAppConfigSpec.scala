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

class FrontendAppConfigSpec extends SpecBase {

  "FrontendAppConfig" should {
    "contain the correct context for this service" in {
      appConfig.context mustBe "/customs/historic-statement/"
    }

    "contain correct values for the provided configuration" in {
      appConfig.cashAccountForCdsDeclarationsUrl mustBe
        "https://www.gov.uk/guidance/use-a-cash-account-for-cds-declarations"
    }

    "contain the correct GOV survey banner URL" in {
      appConfig.helpMakeGovUkBetterUrl mustBe
        "https://survey.take-part-in-research.service.gov.uk/jfe/form/SV_74GjifgnGv6GsMC?Source=BannerList_HMRC_CDS_MIDVA"
    }
  }

  "sdesCashStatementListUrl" should {
    "return correct sdes url" in {
      appConfig.sdesCashStatementListUrl mustBe
        "http://localhost:9754/customs-financials-sdes-stub/files-available/list/CDSCashAccount"
    }
  }

  "returnLink" should {
    "return the adjustments link if a SecurityStatement FileRole provided" in {
      appConfig.returnLink(SecurityStatement, emptyUserAnswers) mustBe
        "http://localhost:9398/customs/documents/adjustments"
    }

    "return the adjustments link if a C79Certificate FileRole provided" in {
      appConfig.returnLink(C79Certificate, emptyUserAnswers) mustBe
        "http://localhost:9398/customs/documents/import-vat"
    }

    "return the cash account link if a CDSCashAccount FileRole and User Answers provided" in {
      appConfig.returnLink(CDSCashAccount, emptyUserAnswers) mustBe
        "http://localhost:9394/customs/cash-account"
    }

    "return the DutyDeferment link if a DutyDeferment FileRole provided and linkId in user answers" in {
      appConfig.returnLink(
        DutyDefermentStatement,
        emptyUserAnswers.set(RequestedLinkId, "someLink").success.value
      ) mustBe
        "http://localhost:9397/customs/duty-deferment/someLink/account"
    }

    "return the cash account link if a CDSCashAccount FileRole provided" in {
      appConfig.returnLink(CDSCashAccount) mustBe
        "http://localhost:9394/customs/cash-account"
    }

    "throw an exception if DutyDeferment FileRole and no linkId in user answers" in {
      intercept[Exception] {
        appConfig.returnLink(DutyDefermentStatement, emptyUserAnswers) mustBe
          "http://localhost:9398/customs/documents/import-vat"
      }.getMessage mustBe "Unable to retrieve linkId"
    }

    "throw an exception if DutyDeferment fileRole is passed " in {
      intercept[Exception] {
        appConfig.returnLink(DutyDefermentStatement) mustBe "http://localhost:9398/customs/documents/import-vat"
      }.getMessage mustBe "Invalid file role"
    }
  }

  "feedbackService" should {
    "return correct url" in {
      appConfig.feedbackService mustBe "http://localhost:9514/feedback/CDS-FIN"
    }
  }

  "customsDataStore" should {
    "return correct url" in {
      appConfig.customsDataStore mustBe "http://localhost:9893/customs-data-store"
    }
  }

  "customsDataStoreGetVerifiedEmail" should {
    "return correct url" in {
      appConfig.customsDataStoreGetVerifiedEmail mustBe "http://localhost:9893/customs-data-store/eori/verified-email"
    }
  }

  "customsDataStoreGetEoriHistory" should {
    "return correct url" in {
      appConfig.customsDataStoreGetEoriHistory mustBe "http://localhost:9893/customs-data-store/eori/eori-history"
    }
  }

  "deleteNotificationUrl" should {
    "return correct url for cash account FileRole" in {
      appConfig.deleteNotificationUrl(CDSCashAccount) mustBe
        "http://localhost:9878/customs-financials-api/eori/notifications/CDSCashAccount"
    }

    "return correct url for C79 certificate FileRole" in {
      appConfig.deleteNotificationUrl(C79Certificate) mustBe
        "http://localhost:9878/customs-financials-api/eori/requested-notifications/C79Certificate"
    }

    "return correct url for Duty deferment statement FileRole" in {
      appConfig.deleteNotificationUrl(DutyDefermentStatement) mustBe
        "http://localhost:9878/customs-financials-api/eori/requested-notifications/DutyDefermentStatement"
    }

    "return correct url for Postponed VAT Statement FileRole" in {
      appConfig.deleteNotificationUrl(PostponedVATStatement) mustBe
        "http://localhost:9878/customs-financials-api/eori/requested-notifications/PostponedVATStatement"
    }

    "return correct url for Security statement FileRole" in {
      appConfig.deleteNotificationUrl(SecurityStatement) mustBe
        "http://localhost:9878/customs-financials-api/eori/requested-notifications/SecurityStatement"
    }
  }
}
