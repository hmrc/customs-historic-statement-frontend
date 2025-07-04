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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import org.scalatest.Assertion
import utils.Utils.emptyStringWithSpace
import utils.TestData.{classes, defaultClasses, href, linkMessage, postLinkMessage, preLinkMessage}
import views.html.components.newTabLink

class NewTabLinkSpec extends SpecBase {

  "component" should {

    "display correct view" when {

      "all the parameters have some value" in new Setup {
        val component: Document =
          newTabLinkComponent(linkMessage, href, Some(preLinkMessage), Some(postLinkMessage), classes)

        elementByParagraph(component).text() mustBe
          s"$preLinkMessage$emptyStringWithSpace$linkMessage$emptyStringWithSpace$postLinkMessage"

        elementByClasses(component, classes).get(0).text() mustBe
          s"$preLinkMessage$emptyStringWithSpace$linkMessage$emptyStringWithSpace$postLinkMessage"

        shouldContainTheMessage(component, preLinkMessage)
        shouldContainTheMessage(component, postLinkMessage)
      }

      "there is no preLinkMessage" in new Setup {
        val component: Document =
          newTabLinkComponent(linkMessage = linkMessage, href = href, postLinkMessage = Some(postLinkMessage))

        elementByParagraph(component).text() mustBe s"$linkMessage$emptyStringWithSpace$postLinkMessage"

        elementByClasses(component, defaultClasses).get(0).text() mustBe
          s"$linkMessage$emptyStringWithSpace$postLinkMessage"

        shouldNotContainTheMessage(component, preLinkMessage)
        shouldContainTheMessage(component, postLinkMessage)
      }

      "there is no postLinkMessage" in new Setup {
        val component: Document =
          newTabLinkComponent(linkMessage = linkMessage, href = href, preLinkMessage = Some(preLinkMessage))

        elementByParagraph(component)
          .text() mustBe s"$preLinkMessage$emptyStringWithSpace$linkMessage"

        elementByClasses(component, defaultClasses).get(0).text() mustBe
          s"$preLinkMessage$emptyStringWithSpace$linkMessage"

        shouldContainTheMessage(component, preLinkMessage)
        shouldNotContainTheMessage(component, postLinkMessage)
      }
    }
  }

  private def elementByParagraph(component: Document): Elements =
    component.getElementsByTag("p")

  private def elementByClasses(component: Document, classes: String): Elements =
    component.getElementsByClass(classes)

  private def shouldContainTheMessage(component: Document, msg: String): Assertion =
    component.text().contains(msg) mustBe true

  private def shouldNotContainTheMessage(component: Document, msg: String): Assertion =
    component.text().contains(msg) mustBe false

  trait Setup {
    def newTabLinkComponent(
      linkMessage: String,
      href: String,
      preLinkMessage: Option[String] = None,
      postLinkMessage: Option[String] = None,
      classes: String = defaultClasses
    ): Document =
      Jsoup.parse(
        application.injector
          .instanceOf[newTabLink]
          .apply(linkMessage, href, preLinkMessage, postLinkMessage, classes)
          .body
      )

  }
}
