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

package connectors

import config.FrontendAppConfig
import models.{EmailUnverifiedResponse, EmailVerifiedResponse, FileRole, HistoricDocumentRequest}
import play.api.http.Status.NO_CONTENT
import play.mvc.Http.Status
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import play.api.libs.ws.JsonBodyWritables.writeableOf_JsValue

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsFinancialsApiConnector @Inject()(appConfig: FrontendAppConfig,
                                              httpClient: HttpClientV2)
                                             (implicit executionContext: ExecutionContext) {

  def postHistoricDocumentRequest(historicDocumentRequest: HistoricDocumentRequest)
                                 (implicit hc: HeaderCarrier): Future[Boolean] = {

    httpClient.post(url"${appConfig.historicDocumentsApiUrl}")
      .withBody[HistoricDocumentRequest](historicDocumentRequest)
      .execute[HttpResponse]
      .map(_.status == NO_CONTENT)
      .recover { case _ => false }
  }

  def deleteNotification(eori: String, fileRole: FileRole)(implicit hc: HeaderCarrier): Future[Boolean] = {
    val deleteNotificationEndpoint = appConfig.deleteNotificationUrl(fileRole, eori)

    httpClient.delete(url"$deleteNotificationEndpoint")
      .execute[HttpResponse]
      .map(_.status == Status.OK)
      .recover { case _ => false }
  }

  def isEmailUnverified(implicit hc: HeaderCarrier): Future[Option[String]] = {
    val unVerifiedEmailEndpoint = s"${appConfig.customsFinancialsApi}/subscriptions/unverified-email-display"

    httpClient.get(url"$unVerifiedEmailEndpoint")
      .execute[EmailUnverifiedResponse]
      .map(res => res.unVerifiedEmail)
  }

  def verifiedEmail(implicit hc: HeaderCarrier): Future[EmailVerifiedResponse] = {
    val emailDisplayEndpoint = s"${appConfig.customsFinancialsApi}/subscriptions/email-display"

    httpClient.get(url"$emailDisplayEndpoint").execute[EmailVerifiedResponse]
  }
}
