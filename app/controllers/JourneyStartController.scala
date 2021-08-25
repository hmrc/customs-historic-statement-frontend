/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.CustomsSessionCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.{DutyDefermentStatement, FileRole, NormalMode, UserAnswers}
import pages.{AccountNumber, RequestedFileRole, RequestedLinkId}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyStartController @Inject()(customsSessionCacheConnector: CustomsSessionCacheConnector,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       sessionRepository: SessionRepository,
                                       mcc: MessagesControllerComponents)(implicit executionContext: ExecutionContext) extends FrontendController(mcc) {


  def dutyDeferment(linkId: String): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    hc.sessionId match {
      case Some(sessionId) =>
        customsSessionCacheConnector.getAccountNumber(sessionId.value, linkId).flatMap {
          case Some(accountNumber) =>
            val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.internalId))
            for {
              userAnswersAccountNumber <- Future.fromTry(userAnswers.set(AccountNumber, accountNumber))
              userAnswersLinkId <- Future.fromTry(userAnswersAccountNumber.set(RequestedLinkId, linkId))
              updatedUserAnswers <- Future.fromTry(userAnswersLinkId.set(RequestedFileRole, DutyDefermentStatement))
              _ <- sessionRepository.set(updatedUserAnswers)
            } yield Redirect(routes.HistoricDateRequestPageController.onPageLoad(NormalMode))
          case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
        }
      case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad()))
    }
  }

  def nonDutyDeferment(fileRole: FileRole): Action[AnyContent] = (identify andThen getData).async { implicit request =>
    fileRole match {
      case DutyDefermentStatement => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
      case _ =>
        val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.internalId))
        for {
          updatedUserAnswers <- Future.fromTry(userAnswers.set(RequestedFileRole, fileRole))
          _ <- sessionRepository.set(updatedUserAnswers)
        } yield Redirect(routes.HistoricDateRequestPageController.onPageLoad(NormalMode))
    }
  }
}
