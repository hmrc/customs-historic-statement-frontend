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

package views.components.description_list

import base.SpecBase
import play.twirl.api.{Html, HtmlFormat}
import utils.Utils.{ddComponent, dlComponent, dtComponent}

class DescriptionListSpec extends SpecBase {

  "Description List components" when {
    "rendering dd element" should {
      "render content correctly" in new Setup {
        ddComponent(content) mustBe ddExpectedContent
      }

      "include class when provided" in new Setup {
        ddComponent(content, classes = testClass) mustBe ddWithClass
      }

      "include id when provided" in new Setup {
        ddComponent(content, id = testId) mustBe ddWithId
      }

      "include both class and id when provided" in new Setup {
        ddComponent(content, testClass, testId) mustBe ddWithClassAndId
      }

      "work correctly with render method" in new Setup {
        ddComponent.render(content, testClass, testId) mustBe ddWithClassAndId
      }

      "work correctly with f method" in new Setup {
        ddComponent.f(content, testClass, testId) mustBe ddWithClassAndId
      }
    }

    "rendering dl element" should {
      "render content correctly" in new Setup {
        dlComponent(content) mustBe dlExpectedContent
      }

      "include class when provided" in new Setup {
        dlComponent(content, classes = testClass) mustBe dlWithClass
      }

      "include id when provided" in new Setup {
        dlComponent(content, id = testId) mustBe dlWithId
      }

      "include both class and id when provided" in new Setup {
        dlComponent(content, testClass, testId) mustBe dlWithClassAndId
      }

      "work correctly with render method" in new Setup {
        dlComponent.render(content, testClass, testId) mustBe dlWithClassAndId
      }

      "work correctly with f method" in new Setup {
        dlComponent.f(content, testClass, testId) mustBe dlWithClassAndId
      }
    }

    "rendering dt element" should {
      "render content correctly" in new Setup {
        dtComponent(content) mustBe dtExpectedContent
      }

      "include class when provided" in new Setup {
        dtComponent(content, classes = testClass) mustBe dtWithClass
      }

      "include id when provided" in new Setup {
        dtComponent(content, id = testId) mustBe dtWithId
      }

      "include both class and id when provided" in new Setup {
        dtComponent(content, classes = testClass, id = testId) mustBe dtWithClassAndId
      }

      "work correctly when calling view.render method" in new Setup {
        dtComponent.render(content, testClass, testId) mustBe dtWithClassAndId
      }

      "work correctly when calling view.f method" in new Setup {
        dtComponent.f(content, testClass, testId) mustBe dtWithClassAndId
      }
    }
  }

  trait Setup {

    protected val content: Html = Html("Test content")
    protected val testClass: Option[String] = Some("test-class")
    protected val testId: Option[String] = Some("test-id")

    protected val ddExpectedContent: HtmlFormat.Appendable = ddComponent(content)
    protected val ddWithClass: HtmlFormat.Appendable = ddComponent(content, classes = testClass)
    protected val ddWithId: HtmlFormat.Appendable = ddComponent(content, id = testId)
    protected val ddWithClassAndId: HtmlFormat.Appendable = ddComponent(content, classes = testClass, id = testId)

    protected val dlExpectedContent: HtmlFormat.Appendable = dlComponent(content)
    protected val dlWithClass: HtmlFormat.Appendable = dlComponent(content, classes = testClass)
    protected val dlWithId: HtmlFormat.Appendable = dlComponent(content, id = testId)
    protected val dlWithClassAndId: HtmlFormat.Appendable = dlComponent(content, classes = testClass, id = testId)

    protected val dtExpectedContent: HtmlFormat.Appendable = dtComponent(content)
    protected val dtWithClass: HtmlFormat.Appendable = dtComponent(content, classes = testClass)
    protected val dtWithId: HtmlFormat.Appendable = dtComponent(content, id = testId)
    protected val dtWithClassAndId: HtmlFormat.Appendable = dtComponent(content, classes = testClass, id = testId)
  }
}
