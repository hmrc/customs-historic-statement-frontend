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
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomsSessionCacheConnector @Inject() (httpClient: HttpClientV2, appConfig: FrontendAppConfig)(implicit
  executionContext: ExecutionContext
) {

  def getAccountNumber(sessionId: String, linkId: String)(implicit hc: HeaderCarrier): Future[Option[String]] = {

    val cacheUrl = appConfig.sessionCacheUrl(sessionId, linkId)

    httpClient
      .get(url"$cacheUrl")
      .execute[SessionCacheResponse]
      .map(response => Some(response.accountNumber))
      .recover { case _ => None }
  }

  def getAccountLink(sessionId: String, linkId: String)(implicit hc: HeaderCarrier): Future[Option[AccountLink]] = {

    val cacheUrl = appConfig.sessionCacheUrl(sessionId, linkId)

    httpClient
      .get(url"$cacheUrl")
      .execute[AccountLink]
      .map(Some(_))
      .recover { case _ => None }
  }
}

case class SessionCacheResponse(accountNumber: String)

object SessionCacheResponse {
  implicit val format: OFormat[SessionCacheResponse] = Json.format[SessionCacheResponse]
}

case class AccountLink(
  eori: String,
  accountNumber: String,
  linkId: String,
  accountStatus: String,
  accountStatusId: Option[Int],
  isNiAccount: Boolean
)

object AccountLink {
  implicit val format: OFormat[AccountLink] = Json.format[AccountLink]
}
