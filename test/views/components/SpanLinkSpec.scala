/*
 * Copyright 2024 HM Revenue & Customs
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
import views.ViewTestHelper
import views.html.components.span_link
import utils.TestData.{msgKey, url}

class SpanLinkSpec extends ViewTestHelper {

  "SpanLink component" should {
    "display the span message" when {
      "spanMsg is set and visually hidden" in new Setup {
        implicit val spanView: Document = view(spanMsg = Some(msgKey), spanClass = Some("govuk-visually-hidden"))
        displayVisuallyHiddenMessage(msgKey)
      }

      "spanMsg is set and not visually hidden" in new Setup {
        implicit val spanView: Document = view(spanMsg = Some(msgKey), spanClass = None)
        shouldNotDisplayVisuallyHiddenMessage()
        shouldDisplaySpanWithMessageKey(msgKey)
      }

      "spanMsg is not set" in new Setup {
        implicit val spanView: Document = view(spanMsg = None)
        shouldNotDisplaySpanMessage()
      }
    }

    "render the span link with an id attribute set" in new Setup {
      implicit val spanView: Document = view(
        spanMsg = Some(msgKey),
        spanClass = Some("govuk-visually-hidden"),
        id = Some("govuk-id")
      )

      shouldRenderLinkWithId("govuk-id", msgKey)
    }

    "render the link with the download attribute when download is true" in new Setup {
      implicit val spanView: Document = viewWithDownload(download = true)

      spanView.getElementsByTag("a").hasAttr("download") mustBe true
    }

    "not render the download attribute when download is false" in new Setup {
      implicit val spanView: Document = viewWithDownload(download = false)

      spanView.getElementsByTag("a").hasAttr("download") mustBe false
    }

  }

  private def displayVisuallyHiddenMessage(expectedMsg: String)(implicit view: Document) =
    view.getElementsByClass("govuk-visually-hidden").text() mustBe expectedMsg.trim

  private def shouldNotDisplayVisuallyHiddenMessage()(implicit view: Document) =
    view.getElementsByClass("govuk-visually-hidden").text() mustBe empty

  private def shouldDisplaySpanWithMessageKey(expectedMsg: String)(implicit view: Document) =
    view.getElementsByTag("span").last().text() mustBe expectedMsg.trim

  private def shouldNotDisplaySpanMessage()(implicit view: Document) =
    view.getElementsByTag("span").last().text() mustBe empty

  private def shouldRenderLinkWithId(expectedId: String, expectedMsg: String)(implicit view: Document) = {
    view.getElementsByTag("a").attr("id") mustBe expectedId
    view.getElementsByTag("span").last().text() mustBe expectedMsg.trim
    view.getElementsByClass("govuk-visually-hidden").text() mustBe expectedMsg.trim
  }

  trait Setup {
    def view(
      msgKey: String = msgKey,
      spanMsg: Option[String] = None,
      spanClass: Option[String] = None,
      id: Option[String] = None
    ): Document = {

      val component = application.injector
        .instanceOf[span_link]
        .apply(
          msg = msgKey,
          url = url,
          spanMsg = spanMsg,
          spanClass = spanClass,
          id = id
        )

      Jsoup.parse(component.body)
    }

    def viewWithDownload(
      msgKey: String = msgKey,
      spanMsg: Option[String] = Some(msgKey),
      spanClass: Option[String] = Some("govuk-visually-hidden"),
      id: Option[String] = None,
      download: Boolean
    ): Document = {
      val component = application.injector
        .instanceOf[span_link]
        .render(
          msg = msgKey,
          id = id,
          classes = "govuk-link",
          url = url,
          spanClass = spanClass,
          spanMsg = spanMsg,
          download = download,
          messages = messages
        )

      Jsoup.parse(component.body)
    }
  }
}
