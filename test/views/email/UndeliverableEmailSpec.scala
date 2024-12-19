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

package views.email

import base.SpecBase
import config.FrontendAppConfig
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.email.undeliverable_email

class UndeliverableEmailSpec extends SpecBase {

  "view" should {
    "display correct guidance and text" when {

      "registered email is available" in new Setup {

        view.title() mustBe
          s"${messages(app)("cf.undeliverable.email.title")} - ${messages(app)("service.name")} - GOV.UK"

        view.getElementById("email-heading-h1").text() mustBe messages(app)("cf.undeliverable.email.heading")

        view.text().contains(messages(app)("cf.undeliverable.email.p1")) mustBe true
        view.html.contains(messages(app)("cf.undeliverable.email.p2", email))

        view.getElementById("email-verify-heading-h2").text() mustBe
          messages(app)("cf.undeliverable.email.verify.heading")

        view.text().contains(messages(app)("cf.undeliverable.email.verify.text.p1")) mustBe true

        view.getElementById("email-change-heading-h2").text() mustBe
          messages(app)("cf.undeliverable.email.change.heading")

        view.text().contains(messages(app)("cf.undeliverable.email.change.text.p1")) mustBe true
        view.text().contains(messages(app)("cf.undeliverable.email.change.text.p2")) mustBe true

        view.text().contains(messages(app)("cf.undeliverable.email.link-text")) mustBe true

        view.toString must include(nextPageUrl)
        view.text().contains(email.get) mustBe true
      }

      "registered email in unavailable" in new Setup {
        viewWithNoEmail.text().contains(email.get) mustBe false
      }

    }

  }

  trait Setup {
    val app: Application                                      = applicationBuilder().build()
    val nextPageUrl                                           = "test_url"
    val email: Option[String]                                 = Some("test@test.com")
    implicit val msges: Messages                              = messages(app)
    implicit val appConfig: FrontendAppConfig                 = app.injector.instanceOf[FrontendAppConfig]
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val view: Document = Jsoup.parse(
      app.injector.instanceOf[undeliverable_email].apply(nextPageUrl, email)(request, msges, appConfig).body
    )

    val viewWithNoEmail: Document = Jsoup.parse(app.injector.instanceOf[undeliverable_email].apply(nextPageUrl).body)
  }
}
