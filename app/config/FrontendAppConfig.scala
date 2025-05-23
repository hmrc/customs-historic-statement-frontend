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

package config

import com.google.inject.{Inject, Singleton}
import models.{
  C79Certificate, CDSCashAccount, DutyDefermentStatement, FileRole, PostponedVATStatement, SecurityStatement,
  UserAnswers
}
import pages.RequestedLinkId
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, servicesConfig: ServicesConfig) {

  lazy val host: String    = configuration.get[String]("host")
  lazy val context: String = "/customs/historic-statement/"

  lazy val timeout: Int   = configuration.get[Int]("timeout.timeout")
  lazy val countdown: Int = configuration.get[Int]("timeout.countdown")

  val feedbackService: String = configuration.get[String]("microservice.services.feedback.url") +
    configuration.get[String]("microservice.services.feedback.source")

  lazy val customsFinancialsSessionCacheUrl: String = servicesConfig.baseUrl("customs-financials-session-cache") +
    configuration.get[String]("microservice.services.customs-financials-session-cache.context")

  lazy val customsFinancialsApi: String = servicesConfig.baseUrl("customs-financials-api") +
    configuration.get[String]("microservice.services.customs-financials-api.context")

  private lazy val customsDataStoreVerifiedEmail = "/eori/verified-email"
  private lazy val customsDataStoreEoriHistory   = "/eori/eori-history"

  lazy val customsDataStore: String                 = servicesConfig.baseUrl("customs-data-store") +
    configuration.get[String]("microservice.services.customs-data-store.context")
  lazy val customsDataStoreGetVerifiedEmail: String = s"$customsDataStore$customsDataStoreVerifiedEmail"
  lazy val customsDataStoreGetEoriHistory: String   = s"$customsDataStore$customsDataStoreEoriHistory"

  lazy val sdesApi: String = servicesConfig.baseUrl("sdes") +
    configuration.get[String]("microservice.services.sdes.context")

  lazy val sdesCashStatementUrl: String =
    configuration.get[String]("microservice.services.sdes.cashStatementsUrl")

  lazy val xClientIdHeader: String = configuration.get[String]("microservice.services.sdes.x-client-id")

  lazy val loginUrl: String                         = configuration.get[String]("urls.login")
  lazy val loginContinueUrl: String                 = configuration.get[String]("urls.loginContinue")
  lazy val signOutUrl: String                       = configuration.get[String]("urls.signOut")
  lazy val subscribeCdsUrl: String                  = configuration.get[String]("urls.cdsSubscribeUrl")
  lazy val cashAccountForCdsDeclarationsUrl: String = configuration.get[String]("urls.cashAccountForCdsDeclarationsUrl")
  lazy val financialsHomepage: String               = configuration.get[String]("urls.financialsHomepage")
  lazy val helpMakeGovUkBetterUrl: String           = configuration.get[String]("urls.helpMakeGovUkBetterUrl")

  private lazy val c79ReturnLink: String           = configuration.get[String]("urls.c79Return")
  private lazy val adjustmentsReturnLink: String   = configuration.get[String]("urls.adjustmentsReturn")
  private lazy val postponedVatReturnLink: String  = configuration.get[String]("urls.postponedVatReturn")
  private lazy val cashStatementReturnLink: String = configuration.get[String]("urls.cashStatementReturn")

  lazy val sdesDutyDefermentStatementListUrl: String      = sdesApi + "/files-available/list/DutyDefermentStatement"
  lazy val sdesImportVatCertificateListUrl: String        = sdesApi + "/files-available/list/C79Certificate"
  lazy val sdesImportPostponedVatStatementListUrl: String = sdesApi + "/files-available/list/PostponedVATStatement"
  lazy val sdesCashStatementListUrl: String               = s"$sdesApi$sdesCashStatementUrl"

  lazy val sdesSecurityStatementListUrl: String = sdesApi + "/files-available/list/SecurityStatement"
  lazy val historicDocumentsApiUrl: String      = customsFinancialsApi + "/historic-document-request"

  lazy val emailFrontendUrl: String = servicesConfig.baseUrl("customs-email-frontend") +
    configuration.get[String]("microservice.services.customs-email-frontend.context") +
    configuration.get[String]("microservice.services.customs-email-frontend.url")

  lazy val fixedDateTime = configuration.get[Boolean]("features.fixed-systemdate-for-tests")

  def sessionCacheUrl(sessionId: String, linkId: String): String =
    customsFinancialsSessionCacheUrl + s"/account-link/$sessionId/$linkId"

  def deleteNotificationUrl(fileRole: FileRole): String =
    fileRole match {
      case CDSCashAccount => s"$customsFinancialsApi/eori/notifications/$fileRole"
      case _              => s"$customsFinancialsApi/eori/requested-notifications/$fileRole"
    }

  private def dutyDefermentReturnLink(linkId: String): String =
    configuration.get[String]("urls.dutyDefermentReturn") + linkId + "/account"

  def returnLink(fileRole: FileRole, userAnswers: UserAnswers): String =
    fileRole match {
      case DutyDefermentStatement =>
        userAnswers.get(RequestedLinkId) match {
          case Some(value) => dutyDefermentReturnLink(value)
          case None        => throw new RuntimeException("Unable to retrieve linkId")
        }
      case C79Certificate         => c79ReturnLink
      case SecurityStatement      => adjustmentsReturnLink
      case PostponedVATStatement  => postponedVatReturnLink
      case CDSCashAccount         => cashStatementReturnLink
    }

  def returnLink(fileRole: FileRole): String =
    fileRole match {
      case DutyDefermentStatement => throw new RuntimeException("Invalid file role")
      case C79Certificate         => c79ReturnLink
      case SecurityStatement      => adjustmentsReturnLink
      case PostponedVATStatement  => postponedVatReturnLink
      case CDSCashAccount         => cashStatementReturnLink
    }

  def returnLink(linkId: String): String = dutyDefermentReturnLink(linkId)
}
