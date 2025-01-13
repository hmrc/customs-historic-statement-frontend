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
import forms.HistoricDateRequestPageFormProvider
import models.DutyDefermentStatement
import utils.TestData.id
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.test.Helpers._
import play.twirl.api.HtmlFormat
import views.html.components.inputDate

class InputDateSpec extends SpecBase {

  "InpuDate component" should {
    "render correctly with no errors" in new Setup {
      val formWithValues = form.bind(
        Map(
          s"$id.month" -> "01",
          s"$id.year"  -> "2021"
        )
      )

      running(application) {
        val inputDateView                 = application.injector.instanceOf[inputDate]
        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)
        html.getElementsByTag("legend").text()     must include(headline)
        html.getElementById(s"$id.month").attr(id) must include("01")
        html.getElementById(s"$id.year").attr(id)  must include("2021")
        html.getElementsByTag("input").attr("class") mustNot include(
          "govuk-input--error"
        )
      }

    }

    "render correctly with month error" in new Setup {
      val formWithValues = form.bind(
        Map(
          s"$id.month" -> "",
          s"$id.year"  -> "2021"
        )
      )

      running(application) {
        val inputDateView                 = application.injector.instanceOf[inputDate]
        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)
        html.getElementsByTag("legend").text()          must include(headline)
        html.getElementById(s"$id.month").attr(id) mustNot include("01")
        html.getElementById(s"$id.year").attr(id)       must include("2021")
        html.getElementById(s"$id.month").attr("class") must include(
          "govuk-input--error"
        )

      }
    }

    "render correctly with year error" in new Setup {
      val formWithValues = form.bind(
        Map(
          s"$id.month" -> "01",
          s"$id.year"  -> ""
        )
      )

      running(application) {
        val inputDateView                 = application.injector.instanceOf[inputDate]
        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)
        html.getElementsByTag("legend").text()         must include(headline)
        html.getElementById(s"$id.month").attr(id)     must include("01")
        html.getElementById(s"$id.year").attr(id) mustNot include("2021")
        html.getElementById(s"$id.year").attr("class") must include(
          "govuk-input--error"
        )
      }
    }

    "render correctly with both month and year errors" in new Setup {
      val formWithValues = form.bind(
        Map(
          s"$id.month" -> "",
          s"$id.year"  -> ""
        )
      )

      running(application) {
        val inputDateView                 = application.injector.instanceOf[inputDate]
        val output: HtmlFormat.Appendable = inputDateView(
          formWithValues,
          headline,
          id = id,
          hintText = None,
          legendAsPageHeading = false
        )(messages)

        val html: Document = Jsoup.parse(output.toString)
        html.getElementsByTag("legend").text()       must include(headline)
        html.getElementById(s"$id.month").attr(id) mustNot include("01")
        html.getElementById(s"$id.year").attr(id) mustNot include("2021")
        html.getElementsByTag("input").attr("class") must include(
          "govuk-input--error"
        )
      }
    }
  }

  trait Setup {
    val form     = new HistoricDateRequestPageFormProvider().apply(DutyDefermentStatement)
    val headline = "Date of birth"
  }
}
