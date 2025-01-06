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
import utils.Utils.emptyString
import views.ViewTestHelper
import views.html.components.span

class SpanSpec extends ViewTestHelper {

  "component" should {
    "display the correct key" when {
      "visually hidden is set to true" in new Setup {
        implicit val spanView: Document = view()
        displayKey()
      }

      "visually hidden is set to false" in new Setup {
        implicit val spanView: Document = view(visuallyHidden = false)
        shouldNotDisplayVisuallyHiddenKey()
        shouldDisplaySpanWithMessageKey()
      }
    }
  }

  private def displayKey(messageKey: String = "timeout.title")(implicit view: Document) =
    view.getElementsByClass("govuk-visually-hidden").html.contains(messages(messageKey)) mustBe true

  private def shouldNotDisplayVisuallyHiddenKey(messageKey: String = "timeout.title")(implicit view: Document) =
    view.getElementsByClass("govuk-visually-hidden").html.contains(messages(messageKey)) mustBe false

  private def shouldDisplaySpanWithMessageKey(messageKey: String = "timeout.title")(implicit view: Document) =
    view.getElementsByTag("span").text() mustBe messages(messageKey)

  trait Setup {
    val messageKey = "timeout.title"

    def view(msgKey: String = messageKey, visuallyHidden: Boolean = true): Document = Jsoup.parse(
      application.injector
        .instanceOf[span]
        .apply(key = msgKey, classes = Some(emptyString), visuallyHidden = visuallyHidden)
        .body
    )
  }
}
