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
import connectors.CustomsDataStoreConnector
import controllers.actions._
import models.{FileRole, UserAnswers}
import models.requests.DataRequest
import pages.HistoricDateRequestPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Utils.emptyString
import viewmodels.CheckYourAnswersHelper
import views.html.ConfirmationPageView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmationPageController @Inject()(override val messagesApi: MessagesApi,
                                           identify: IdentifierAction,
                                           getData: DataRetrievalAction,
                                           requireData: DataRequiredAction,
                                           sessionRepository: SessionRepository,
                                           customsDataStoreConnector: CustomsDataStoreConnector,
                                           val controllerComponents: MessagesControllerComponents,
                                           view: ConfirmationPageView)(
  implicit ec: ExecutionContext, appConfig: FrontendAppConfig)
  extends FrontendBaseController with I18nSupport {

  def onPageLoad(fileRole: FileRole): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val dates = new CheckYourAnswersHelper(request.userAnswers)
      val returnLink = routes.ConfirmationPageController.returnToStatementsPage(fileRole).url

      for {
        _ <- sessionRepository.set(userAnswersWithNoHistoricDates(fileRole, request))
        email <- retrieveEmail(request)
      } yield Ok(
        view(email,
          fileRole,
          returnLink,
          dates.dateRows(fileRole).getOrElse(emptyString))
      )
  }

  def returnToStatementsPage(fileRole: FileRole): Action[AnyContent] = {
    (identify andThen getData andThen requireData).async {
      implicit request =>

        for {
          _ <- sessionRepository.clear(request.internalId).recover { case _ => true }
        } yield {
          Redirect(appConfig.returnLink(fileRole, request.userAnswers))
        }
    }
  }

  private def userAnswersWithNoHistoricDates(fileRole: FileRole,
                                         request: DataRequest[AnyContent]): UserAnswers =
    request.userAnswers.remove(HistoricDateRequestPage(fileRole)) match {
      case scala.util.Success(value) => value
      case _ => request.userAnswers
    }

  private def retrieveEmail(request: DataRequest[AnyContent])(implicit hc: HeaderCarrier): Future[Option[Email]] =
    customsDataStoreConnector.getEmail(request.eori).map {
      case Right(email) => Some(email)
      case Left(_) => None
    }.recover { case _ => None }

}
