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

package controllers

import config.FrontendAppConfig
import connectors.{CustomsFinancialsApiConnector, CustomsSessionCacheConnector, SdesConnector}
import controllers.actions.{EoriHistoryAction, IdentifierAction, SessionIdFilter}
import models._
import models.requests.{IdentifierRequestWithEoriHistory, IdentifierRequestWithEoriHistoryAndSessionId}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SortStatementsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.{DutyDefermentAccountViewModel, PostponedVatViewModel, SecuritiesRequestedStatementsViewModel, VatViewModel}
import views.html._

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HistoricStatementsController @Inject()(identify: IdentifierAction,
                                             getEoriHistory: EoriHistoryAction,
                                             getSessionId: SessionIdFilter,
                                             mcc: MessagesControllerComponents,
                                             customsFinancialsApiConnector: CustomsFinancialsApiConnector,
                                             sessionCacheConnector: CustomsSessionCacheConnector,
                                             sdesConnector: SdesConnector,
                                             importVatView: ImportVatRequestedStatements,
                                             importPostponedVatView: ImportPostponedVatRequestedStatements,
                                             securitiesView: SecuritiesRequestedStatements,
                                             sortStatementsService: SortStatementsService,
                                             dutyDefermentView: DutyDefermentRequestedStatements)
                                            (implicit executionContext: ExecutionContext,
                                             appConfig: FrontendAppConfig)
  extends FrontendController(mcc) with I18nSupport {

  def historicStatements(fileRole: FileRole): Action[AnyContent] = (
    identify andThen getEoriHistory).async { implicit request =>
    customsFinancialsApiConnector.deleteNotification(request.eori, fileRole)

    fileRole match {
      case SecurityStatement => showHistoricSecurityStatements()
      case C79Certificate => showHistoricC79Statements()
      case PostponedVATStatement => showHistoricPostponedVatStatements()
      case _ => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad))
    }
  }

  def historicStatementsDutyDeferment(linkId: String): Action[AnyContent] = (
    identify andThen getEoriHistory andThen getSessionId).async { implicit request =>

    customsFinancialsApiConnector.deleteNotification(request.eori, DutyDefermentStatement)
    sessionCacheConnector.getAccountNumber(request.sessionId, linkId).flatMap {
      case Some(dan) => showHistoricDutyDefermentStatements(dan, linkId)
      case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
    }
  }

  private def showHistoricC79Statements()(
    implicit request: IdentifierRequestWithEoriHistory[AnyContent]): Future[Result] = {

    for {
      allCertificates <- Future.sequence(request.eoriHistory.map { historicEori =>
        sdesConnector
          .getVatCertificates(historicEori.eori)
          .map(sortStatementsService.sortVatCertificatesForEori(historicEori, _))
      })
      viewModel = VatViewModel(allCertificates.sorted)
    } yield Ok(importVatView(viewModel, appConfig.returnLink(C79Certificate)))
  }

  private def showHistoricPostponedVatStatements()(
    implicit request: IdentifierRequestWithEoriHistory[AnyContent]): Future[Result] = {

    for {
      allCertificates <- Future.sequence(request.eoriHistory.map { historicEori =>
        sdesConnector
          .getPostponedVatStatements(historicEori.eori)
          .map(sortStatementsService.sortPostponedVatStatementsForEori(historicEori, _))
      })
      viewModel = PostponedVatViewModel(allCertificates.sorted)
    } yield Ok(importPostponedVatView(viewModel, appConfig.returnLink(PostponedVATStatement)))
  }

  private def showHistoricSecurityStatements()(
    implicit request: IdentifierRequestWithEoriHistory[AnyContent]): Future[Result] = {

    for {
      allCertificates <- Future.sequence(request.eoriHistory.map {
        historicEori =>
          sdesConnector
            .getSecurityStatements(historicEori.eori)
            .map(sortStatementsService.sortSecurityCertificatesForEori(historicEori, _))
      })
    } yield {
      val model = SecuritiesRequestedStatementsViewModel(allCertificates)
      Ok(securitiesView(model, appConfig.returnLink(SecurityStatement)))
    }
  }

  private def showHistoricDutyDefermentStatements(dan: String, linkId: String)(
    implicit request: IdentifierRequestWithEoriHistoryAndSessionId[AnyContent]): Future[Result] = {

    for {
      allStatements <- Future.sequence(request.eoriHistory.map {
        historicEori =>
          sdesConnector
            .getDutyDefermentStatements(historicEori.eori, dan)
            .map(sortStatementsService.sortDutyDefermentStatementsForEori(historicEori, _))
      })
      viewModel = DutyDefermentAccountViewModel(dan, allStatements, isNiAccount = false)
    } yield Ok(dutyDefermentView(viewModel, appConfig.returnLink(linkId)))
  }
}
