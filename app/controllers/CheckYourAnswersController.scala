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

import com.google.inject.Inject
import connectors.CustomsFinancialsApiConnector
import controllers.actions.{DataRequiredAction, DataRetrievalAction, IdentifierAction}
import models.HistoricDocumentRequest
import pages.AccountNumber
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import viewmodels.CheckYourAnswersHelper
import views.html.CheckYourAnswersView

import scala.concurrent.{ExecutionContext, Future}

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            requireData: DataRequiredAction,
                                            controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView,
                                            customsFinancialsApiConnector: CustomsFinancialsApiConnector
                                          )(implicit execution: ExecutionContext) extends FrontendController(controllerComponents) with I18nSupport {

  def onPageLoad(): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>
      val checkYourAnswersHelper = new CheckYourAnswersHelper(request.userAnswers)
      val maybeAccountNumber = request.userAnswers.get(AccountNumber)
      Ok(view(checkYourAnswersHelper, request.fileRole, maybeAccountNumber))
  }

  def onSubmit(): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>
      HistoricDocumentRequest.fromRequest match {
        case Some(value) =>
          customsFinancialsApiConnector.postHistoricDocumentRequest(value).map { successful =>
            if (successful) {
              Redirect(routes.ConfirmationPageController.onPageLoad())
            } else {
              Redirect(routes.TechnicalDifficultiesController.onPageLoad())
            }
          }
        case None => Future.successful(Redirect(routes.TechnicalDifficultiesController.onPageLoad()))
      }
  }
}
