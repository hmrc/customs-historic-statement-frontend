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

import connectors.CustomsSessionCacheConnector
import controllers.actions.{DataRetrievalAction, IdentifierAction, EmailAction}
import models.{DutyDefermentStatement, FileRole, NormalMode, UserAnswers}

import pages.{AccountNumber, IsNiAccount, RequestedLinkId}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class JourneyStartController @Inject()(customsSessionCacheConnector: CustomsSessionCacheConnector,
                                       identify: IdentifierAction,
                                       checkEmailIsVerified: EmailAction,
                                       getData: DataRetrievalAction,
                                       sessionRepository: SessionRepository,
                                       mcc: MessagesControllerComponents)
                                      (implicit executionContext: ExecutionContext) extends FrontendController(mcc) {


  def dutyDeferment(linkId: String): Action[AnyContent] = (
    identify andThen checkEmailIsVerified andThen getData).async { implicit request =>
    hc.sessionId match {

      case Some(sessionId) =>
        customsSessionCacheConnector.getAccountLink(sessionId.value, linkId).flatMap {
          case Some(accountLink) =>
            val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.internalId))
            for {
              userAnswersAccountNumber <- Future.fromTry(
                userAnswers.set(AccountNumber, accountLink.accountNumber))

              userAnswersNiIndicator <- Future.fromTry(
                userAnswersAccountNumber.set(IsNiAccount, accountLink.isNiAccount))

              userAnswersLinkId <- Future.fromTry(
                userAnswersNiIndicator.set(RequestedLinkId, linkId))

              _ <- sessionRepository.set(userAnswersLinkId)
            } yield Redirect(routes.HistoricDateRequestPageController.onPageLoad(NormalMode, DutyDefermentStatement))

          case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
        }
      case None => Future.successful(Redirect(routes.SessionExpiredController.onPageLoad))
    }
  }

  def nonDutyDeferment(fileRole: FileRole): Action[AnyContent] = (
    identify andThen checkEmailIsVerified andThen getData).async { implicit request =>
    fileRole match {
      case DutyDefermentStatement => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad))
      case _ =>
        val userAnswers = request.userAnswers.getOrElse(UserAnswers(request.internalId))
        for {
          _ <- sessionRepository.set(userAnswers)
        } yield Redirect(routes.HistoricDateRequestPageController.onPageLoad(NormalMode, fileRole))
    }
  }
}
