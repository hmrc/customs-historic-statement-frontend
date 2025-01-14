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

package handlers

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import views.html.{ErrorTemplate, not_found}
import utils.TestData.{test_heading, test_message, test_title}
import base.SpecBase

class ErrorHandlerSpec extends SpecBase {

  "overridden standardErrorTemplate" should {

    "display template with correct contents" in new Setup {

      val errorTemplateView: ErrorTemplate = instanceOf[ErrorTemplate]

      errorHandler.standardErrorTemplate(test_title, test_heading, test_heading).map { errorTemplate =>
        errorTemplate mustBe errorTemplateView(test_title, test_heading, test_message)

        val docView: Document = Jsoup.parse(errorTemplate.body)
        docView.getElementsByClass("govuk-heading-xl").text mustBe test_heading
        docView.getElementsByClass("govuk-body").text mustBe test_message
      }
    }
  }

  "notFoundTemplate" should {

    "display template with correct contents" in new Setup {

      val notFoundView: not_found = instanceOf[not_found]

      errorHandler.notFoundTemplate.map { notFoundTemplate =>
        notFoundTemplate.toString mustBe notFoundView.apply().body
      }
    }
  }

  "unauthorized" should {

    "display template with correct contents" in new Setup {

      val errorTemplateView: ErrorTemplate = instanceOf[ErrorTemplate]

      errorHandler.unauthorized() mustBe
        errorTemplateView.apply(
          messages("cf.error.unauthorized.title"),
          messages("cf.error.unauthorized.heading"),
          messages("cf.error.unauthorized.message")
        )
    }
  }

  trait Setup {
    implicit val ec: scala.concurrent.ExecutionContext = scala.concurrent.ExecutionContext.global
    val errorHandler: ErrorHandler                     = instanceOf[ErrorHandler]
  }
}
