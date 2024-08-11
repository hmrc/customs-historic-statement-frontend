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
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.select.Elements
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.{dd, div, dl, dt, h2_extraContent, h3}

class CommonComponentSpec extends SpecBaseWithSetup {

  "CommonComponentSpec" must {
    tagElements.foreach { tagElement =>
      s"contain the right content and render $tagElement component correctly" when {

        "id is supplied" in new SpecBaseWithSetup {
          override def id: Option[String] = Some(myId)

          document.select(tagElement).hasAttr(idElement) mustBe true
          document.select(tagElement).attr(idElement) mustBe myId
        }

        "id is not supplied" in new SpecBaseWithSetup {
          override def id: Option[String] = None

          document.select(tagElement).hasAttr(idElement) mustBe false
        }

        "a class is supplied" in new SpecBaseWithSetup {
          override def classes: Option[String] = Some(myClass)

          document.select(tagElement).hasAttr(classElement) mustBe true
          document.select(tagElement).attr(classElement) mustBe myClass
        }

        "a class is not supplied" in new SpecBaseWithSetup {
          override def classes: Option[String] = None

          tagElements.foreach { tag =>
            val hasClassAttr = document.select(tag).hasAttr(classElement)

            if (tag == "h2" || tag == "h3") {
              hasClassAttr mustBe true
            } else {
              hasClassAttr mustBe false
            }
          }
        }

        "contain the correct content" in new SpecBaseWithSetup {
          override def content: Html = Html(myContent)

          document.select(tagElement).html() mustBe myContent
        }
      }
    }

    "render h2_extraContent component correctly" when {
      "extra content is supplied" in new SpecBaseWithSetup {
        override def extraContent: Option[String] = Some(myExtraContent)

        val h2Elements: Elements = document.select("h2")

        h2Elements.get(0).html() must include(myContent)
        h2Elements.get(0).html() must include(myExtraContent)
      }

      "extra content is not supplied" in new SpecBaseWithSetup {
        override def extraContent: Option[String] = None

        val h2Elements: Elements = document.select("h2")

        h2Elements.size() mustBe 1
        h2Elements.get(0).html() mustBe myContent
        h2Elements.get(0).html() must not include myExtraContent
      }
    }
  }
}

trait SpecBaseWithSetup extends SpecBase {

  val app: Application = applicationBuilder().build()
  implicit val messages: Messages = messages(app)

  protected def id: Option[String] = None

  protected def classes: Option[String] = None

  protected def content: Html = Html("some content")

  protected def extraContent: Option[String] = None

  protected val myId = "an-id"
  protected val myClass = "my-class"
  protected val myContent = "expected content"
  protected val myExtraContent = "extra content"

  protected val idElement = "id"
  protected val classElement = "class"

  protected val div: div = app.injector.instanceOf[div]
  protected val dl: dl = app.injector.instanceOf[dl]
  protected val dt: dt = app.injector.instanceOf[dt]
  protected val dd: dd = app.injector.instanceOf[dd]
  protected val h2_extraContent: h2_extraContent = app.injector.instanceOf[h2_extraContent]
  protected val h3: h3 = app.injector.instanceOf[h3]

  val tagElements: Seq[String] = List("div", "dl", "dt", "dd", "h2", "h3")

  protected val html: Html = Html(
    div(content, classes = classes, id = id).toString +
      dl(content, classes = classes, id = id).toString +
      dt(content, classes = classes, id = id) +
      dd(content, classes = classes, id = id) +
      h2_extraContent(myContent, id, classes.getOrElse("govuk-heading-m"), extraContent) +
      h3(myContent, classes = classes.getOrElse("govuk-heading-s"), id = id)
  )

  protected lazy val document: Document = Jsoup.parse(html.toString)
}
