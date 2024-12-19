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

package utils

import config.FrontendAppConfig
import play.api.i18n.Messages
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.hmrcfrontend.views.html.components.HmrcNewTabLink
import views.html.components.*
import views.html.components.description_list.{dd, dl, dt}

object Utils {
  val emptyString          = ""
  val emptyStringWithSpace = " "
  val comma                = ","
  val hyphen               = "-"
  val period               = "."

  val h2Component = new h2()
  val h3Component = new h3()
  val pComponent  = new p()

  val divComponent = new div()
  val dtComponent  = new dt()
  val ddComponent  = new dd()
  val dlComponent  = new dl()

  val spanComponent     = new span()
  val spanLinkComponent = new span_link()

  val missingDocumentsGuidanceComponent   = new missing_documents_guidance(h2Component, pComponent)
  val emptyHmrcNewTabLink: HmrcNewTabLink = new HmrcNewTabLink()

  def hmrcNewTabLinkComponent(
    linkMessage: String,
    href: String,
    preLinkMessage: Option[String] = None,
    postLinkMessage: Option[String] = None,
    classes: String = "govuk-body"
  )(implicit messages: Messages, config: FrontendAppConfig): HtmlFormat.Appendable =
    new newTabLink(emptyHmrcNewTabLink).apply(
      linkMessage = linkMessage,
      href = href,
      preLinkMessage = preLinkMessage,
      postLinkMessage = postLinkMessage,
      classes = classes
    )
}
