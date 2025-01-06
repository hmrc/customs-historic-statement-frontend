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

package utils

import base.SpecBase
import config.FrontendAppConfig
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import utils.Utils.{comma, emptyHmrcNewTabLink, emptyString, hmrcNewTabLinkComponent, hyphen, period}
import views.html.components.newTabLink

class UtilsSpec extends SpecBase {
  "emptyString" should {
    "return correct value" in {
      emptyString mustBe ""
    }
  }

  "comma" should {
    "return correct value" in {
      comma mustBe ","
    }
  }

  "hyphen" should {
    "return correct value" in {
      hyphen mustBe "-"
    }
  }

  "period" should {
    "return correct value" in {
      period mustBe "."
    }
  }

  "hmrcNewTabLinkComponent" should {
    "create the component correctly with provided input" in new Setup {
      val result: HtmlFormat.Appendable =
        hmrcNewTabLinkComponent(linkMessage, href, Some(preLinkMessage), Some(postLinkMessage), classes)

      result mustBe new newTabLink(emptyHmrcNewTabLink)
        .apply(linkMessage, href, Some(preLinkMessage), Some(postLinkMessage), classes = classes)
    }
  }

  trait Setup {
    val linkMessage: String = "go to test page"
    val href                = "www.test.com"
    val preLinkMessage      = "test_pre_link_message"
    val postLinkMessage     = "test_post_link_message"
    val classes             = "govuk-!-margin-bottom-7"
  }
}
