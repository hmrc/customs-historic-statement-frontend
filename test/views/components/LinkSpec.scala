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

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.scalatest.Assertion
import views.ViewTestHelper
import views.html.components.link

class LinkSpec extends ViewTestHelper {

  "view" should {

    "display correct contents" when {

      "it contains linkId, preLinkMessage and postLinkMessage" in new Setup {
        implicit val linkView: Document =
          viewDoc(linkId = Some(id), preLinkMessage = Some(preLinkMsgKey), postLinkMessage = Some(postLinkMsgKey))

        shouldContainLocation(Some(id), location)
        shouldContainPreAndPostLinkMessages(preLinkMsgKey, postLinkMsgKey)
        shouldContainCorrectStyleClasses(linkMessage = linkMessage)
      }

      "it contains linkId but no preLinkMessage and postLinkMessage" in new Setup {
        implicit val linkView: Document = viewDoc(linkId = Some(id), pClass = testClass)

        shouldContainLocation(Some(id), location)
        shouldContainCorrectStyleClasses(pClass = testClass, linkMessage = linkMessage)
      }

      "it contains pId and pClass" in new Setup {
        implicit val linkView: Document = viewDoc(pId = Some(pId), pClass = testClass)

        shouldContainCorrectStyleClasses(pClass = testClass, linkMessage = linkMessage)
      }

      "it contains linkSentence" in new Setup {
        implicit val linkView: Document = viewDoc(linkId = Some(id), pClass = testClass, linkSentence = true)

        shouldContainLocation(Some(id), location)
        shouldContainCorrectStyleClasses(pClass = testClass, linkMessage = linkMessage)
        shouldContainLinkSentence(linkMessage = linkMessage)
      }

      "pWrapped is false" in new Setup {
        implicit val linkView: Document = viewDoc(
          linkId = Some(id),
          pWrapped = false,
          preLinkMessage = Some(preLinkMsgKey),
          postLinkMessage = Some(postLinkMsgKey),
          pClass = testClass
        )

        shouldNotContainPreAndPostLinkMessages(preLinkMsgKey, postLinkMsgKey)
      }
    }
  }

  private def shouldContainLocation(id: Option[String], location: String)(implicit view: Document): Assertion =
    if (id.nonEmpty) {
      view.getElementById(id.get).getElementsByAttribute("href").attr("href") mustBe location
    } else {
      view.html().contains(location) mustBe true
    }

  private def shouldContainPreAndPostLinkMessages(preLinkMessage: String, postLinkMessage: String)(implicit
    view: Document
  ): Assertion = {
    view.text().contains(messages(preLinkMessage)) mustBe true
    view.text().contains(messages(postLinkMessage)) mustBe true
  }

  private def shouldNotContainPreAndPostLinkMessages(preLinkMessage: String, postLinkMessage: String)(implicit
    view: Document
  ): Assertion = {
    view.text().contains(messages(preLinkMessage)) mustBe false
    view.text().contains(messages(postLinkMessage)) mustBe false
  }

  private def shouldContainCorrectStyleClasses(
    linkClass: String = "govuk-link",
    pClass: String = "govuk-body",
    linkMessage: String
  )(implicit view: Document): Assertion = {
    view.getElementsByClass(linkClass).text() mustBe messages(linkMessage)
    view.getElementsByClass(pClass).text().contains(messages(linkMessage)) mustBe true
  }

  private def shouldContainLinkSentence(linkMessage: String)(implicit view: Document): Assertion =
    view.text().contains(s"${messages(linkMessage)}.") mustBe true

  trait Setup {
    val linkMessage    = "cf.undeliverable.email.change.text.p1"
    val location       = "test_location"
    val id             = "test_id"
    val pId            = "test_pid"
    val preLinkMsgKey  = "cf.undeliverable.email.link-text"
    val postLinkMsgKey = "cf.undeliverable.email.change.text.p2"
    val testClass      = "test_link_class"

    def viewDoc(
      location: String = location,
      linkId: Option[String] = None,
      pWrapped: Boolean = true,
      linkSentence: Boolean = false,
      preLinkMessage: Option[String] = None,
      postLinkMessage: Option[String] = None,
      pId: Option[String] = None,
      pClass: String = "govuk-body"
    ): Document =
      Jsoup.parse(
        application.injector
          .instanceOf[link]
          .apply(
            linkMessage = linkMessage,
            location = location,
            linkId = linkId,
            pWrapped = pWrapped,
            linkSentence = linkSentence,
            preLinkMessage = preLinkMessage,
            postLinkMessage = postLinkMessage,
            pId = pId,
            pClass = pClass
          )
          .body
      )
  }
}
