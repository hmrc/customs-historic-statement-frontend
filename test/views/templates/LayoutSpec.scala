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

package views.templates

import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.templates.Layout
import base.SpecBase
import utils.Utils.emptyString
import utils.TestData.{test_title, url}

class LayoutSpec extends SpecBase {

  "layout" should {

    "display correct guidance" when {

      "title and back link are provided" in new Setup {
        val layoutView: Document = Jsoup.parse(
          application.injector
            .instanceOf[Layout]
            .apply(
              pageTitle = Some(test_title),
              backLinkUrl = Some(url)
            )(content)
            .body
        )

        shouldContainCorrectTitle(layoutView, test_title)
        shouldContainCorrectServiceUrls(layoutView)
        shouldContainCorrectBackLink(layoutView, Some(url))
        shouldContainCorrectBanners(layoutView)
      }

      "there is no value for title and back link" in new Setup {
        val layoutView: Document = Jsoup.parse(instanceOf[Layout].apply()(content).body)

        shouldContainCorrectTitle(layoutView)
        shouldContainCorrectServiceUrls(layoutView)
        shouldContainCorrectBackLink(layoutView)
        shouldContainCorrectBanners(layoutView)
      }
    }

  }

  private def shouldContainCorrectTitle(viewDoc: Document, title: String = emptyString)(implicit msgs: Messages) =
    if (title.nonEmpty) {
      viewDoc.title() mustBe s"$title - ${msgs("service.name")} - GOV.UK"
    } else {
      viewDoc.title() mustBe s"${msgs("service.name")} - GOV.UK"
    }

  private def shouldContainCorrectServiceUrls(viewDoc: Document)(implicit appConfig: FrontendAppConfig) = {
    viewDoc.html().contains(appConfig.financialsHomepage) mustBe true
    viewDoc.html().contains(controllers.routes.LogoutController.logout().url) mustBe true
    viewDoc.html().contains("/accessibility-statement/customs-financials") mustBe true
  }

  private def shouldContainCorrectBackLink(viewDoc: Document, backLinkUrl: Option[String] = None) =
    if (backLinkUrl.isDefined) {
      viewDoc.getElementsByClass("govuk-back-link").text() mustBe "Back"
      viewDoc
        .getElementsByClass("govuk-back-link")
        .attr("href")
        .contains(backLinkUrl.get) mustBe true
    } else {
      viewDoc.getElementsByClass("govuk-back-link").size() mustBe 0
    }

  private def shouldContainCorrectBanners(viewDoc: Document) =
    viewDoc
      .getElementsByClass("govuk-phase-banner")
      .text() mustBe "BETA This is a new service - your feedback will help us to improve it."

  trait Setup {
    val content: Html = Html("test")
  }
}
