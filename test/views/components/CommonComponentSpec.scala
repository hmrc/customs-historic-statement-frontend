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
import play.api.Application
import play.api.i18n.Messages
import play.twirl.api.Html
import views.html.components.{dd, div, dl, dt, h2_extraContent}

class CommonComponentSpec extends SpecBaseWithSetup {

  val tagElements: Seq[String] = List("div", "dl", "dt", "dd", "h2")

  tagElements.foreach { tagElement =>
    s"$tagElement component" must {

      "have an id when supplied" in new SpecBaseWithSetup {
        override def id: Option[String] = Some(myId)

        document.select(tagElement).hasAttr(idElement) mustBe true
        document.select(tagElement).attr(idElement) mustBe myId
      }

      "have no id when not supplied" in new SpecBaseWithSetup {
        override def id: Option[String] = None

        document.select(tagElement).hasAttr(idElement) mustBe false
      }

      "have a class when supplied" in new SpecBaseWithSetup {
        override def classes: Option[String] = Some(myClass)

        document.select(tagElement).hasAttr(classElement) mustBe true
        document.select(tagElement).attr(classElement) mustBe myClass
      }

      "have no class when not supplied" in new SpecBaseWithSetup {
        override def classes: Option[String] = None

        tagElements.foreach { tag =>
          val hasClassAttr = document.select(tag).hasAttr(classElement)

          if (tag == "h2") {
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

  "h2_extraContent component" must {
    "contain the correct extra content" in new SpecBaseWithSetup {
      override def extraContent: Option[String] = Some(myExtraContent)

      document.select("h2").html() must include(myExtraContent)
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

  protected val html: Html = Html(
    div(content, classes = classes, id = id).toString +
      dl(content, classes = classes, id = id).toString +
      dt(content, classes = classes, id = id).toString +
      dd(content, classes = classes, id = id).toString +
      h2_extraContent(myContent, id, classes.getOrElse("govuk-heading-m"), extraContent).toString
  )

  protected lazy val document: Document = Jsoup.parse(html.toString)
}
