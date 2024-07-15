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

package utils

import models.{FileFormat, PostponedVatStatementFile}
import play.api.i18n.Messages
import play.twirl.api.Html

object PostponedVatViewUtil {

  def downloadLink(file: Option[PostponedVatStatementFile],
                   fileFormat: FileFormat,
                   source: String,
                   id: String,
                   period: String)
                  (implicit messages: Messages): Html = {

    file match {
      case Some(f) =>
        Html(s"""
          <a id="$id" class="file-link govuk-link" href="${f.downloadURL}" download>
            <span>$source statement - $fileFormat (${f.formattedSize})</span>
            <span class="govuk-visually-hidden">${voiceOver(fileFormat, period, f.formattedSize, source)}</span>
          </a>
        """)

      case None =>
        Html(s"""
          <div id="missing-file-$id">
            <span class="govuk-visually-hidden">${hiddenText(fileFormat, period)}</span>
            <span aria-hidden="true">${messages("cf.account.postponed-vat.missing-file")}</span>
          </div>
        """)
    }
  }

  private def hiddenText(fileFormat: FileFormat, period: String)(implicit messages: Messages): String = {
    messages("cf.account.postponed-vat.missing-file-hidden-text", fileFormat, period)
  }

  private def voiceOver(fileFormat: FileFormat, period: String, fileSize: String, source: String)
                       (implicit messages: Messages): String = {
    messages("cf.account.postponed-vat.download-link", source, fileFormat, period, fileSize)
  }
}
