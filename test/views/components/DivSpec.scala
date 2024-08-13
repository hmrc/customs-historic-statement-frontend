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

import base.SpecBase
import play.twirl.api.{Html, HtmlFormat}
import utils.Utils.divComponent

class DivSpec extends SpecBase {

  "div component" when {
    "rendering div element" should {
      "render content correctly" in new Setup {
        divComponent(content) mustBe divExpectedContent
      }

      "include class when provided" in new Setup {
        divComponent(content, classes = testClass) mustBe divWithClass
      }

      "include id when provided" in new Setup {
        divComponent(content, id = testId) mustBe divWithId
      }

      "include both class and id when provided" in new Setup {
        divComponent(content, testClass, testId) mustBe divWithClassAndId
      }

      "work correctly when calling view.render method" in new Setup {
        divComponent.render(content, testClass, testId) mustBe divWithClassAndId
      }

      "work correctly when calling view.f method" in new Setup {
        divComponent.f(content, testClass, testId) mustBe divWithClassAndId
      }
    }
  }

  trait Setup {

    protected val content: Html = Html("Test content")
    protected val testClass: Option[String] = Some("test-class")
    protected val testId: Option[String] = Some("test-id")

    protected val divExpectedContent: HtmlFormat.Appendable = divComponent(content)
    protected val divWithClass: HtmlFormat.Appendable = divComponent(content, classes = testClass)
    protected val divWithId: HtmlFormat.Appendable = divComponent(content, id = testId)
    protected val divWithClassAndId: HtmlFormat.Appendable = divComponent(content, classes = testClass, id = testId)
  }
}
