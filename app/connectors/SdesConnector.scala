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
import models.FileFormat.{SdesFileFormats, filterFileFormats}
import models._
import services.SdesGatekeeperService
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SdesConnector @Inject()(http: HttpClientV2,
                              sdesGatekeeperService: SdesGatekeeperService)
                             (implicit appConfig: FrontendAppConfig, ec: ExecutionContext) {

  import sdesGatekeeperService._

  private def addXHeaders(hc: HeaderCarrier, key: String): HeaderCarrier =
    hc.copy(extraHeaders = hc.extraHeaders ++ Seq("x-client-id" -> appConfig.xClientIdHeader, "X-SDES-Key" -> key))

  def getDutyDefermentStatements(eori: String, dan: String)
                                (implicit hc: HeaderCarrier): Future[Seq[DutyDefermentStatementFile]] = {

    val transform = convertTo[DutyDefermentStatementFile] andThen filterFileFormats(SdesFileFormats)
    getSdesFiles(appConfig.sdesDutyDefermentStatementListUrl, s"$eori-$dan", transform)
  }

  def getVatCertificates(eori: String)(implicit hc: HeaderCarrier): Future[Seq[VatCertificateFile]] = {

    val transform = convertTo[VatCertificateFile] andThen filterFileFormats(SdesFileFormats)
    getSdesFiles[FileInformation, VatCertificateFile](appConfig.sdesImportVatCertificateListUrl, eori, transform)
  }

  def getPostponedVatStatements(eori: String)(implicit hc: HeaderCarrier): Future[Seq[PostponedVatStatementFile]] = {

    val transform = convertTo[PostponedVatStatementFile] andThen filterFileFormats(SdesFileFormats)
    getSdesFiles[FileInformation, PostponedVatStatementFile](
      appConfig.sdesImportPostponedVatStatementListUrl, eori, transform)
  }

  def getSecurityStatements(eori: String)(implicit hc: HeaderCarrier): Future[Seq[SecurityStatementFile]] = {

    val transform = convertTo[SecurityStatementFile] andThen filterFileFormats(SdesFileFormats)
    getSdesFiles[FileInformation, SecurityStatementFile](appConfig.sdesSecurityStatementListUrl, eori, transform)
  }

  //TODO
  def getSdesFiles[A, B <: SdesFile](filesUrl: String, key: String, transform: Seq[A] => Seq[B])
                                    (implicit hc: HeaderCarrier,
                                     reads: HttpReads[HttpResponse],
                                     readSeq: HttpReads[Seq[A]]): Future[Seq[B]] = {

    /*
      http.GET[HttpResponse](filesUrl)(reads, addXHeaders(hc, key), ec)
      .map(readSeq.read("GET", filesUrl, _))
      .map(transform)
      */
    http.get(url"$filesUrl")
      .execute[HttpResponse]
      .map(readSeq.read("GET", filesUrl, _))
      .map(transform)


  }
}
