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
import controllers.actions.IdentifierAction
import play.api.mvc._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.{AuthConnector, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class LogoutController @Inject()(override val authConnector: AuthConnector,
                                 authenticate: IdentifierAction,
                                 sessionRepository: SessionRepository,
                                 mcc: MessagesControllerComponents)
                                (implicit val appConfig: FrontendAppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with AuthorisedFunctions {

  def logout: Action[AnyContent] = authenticate.async {
    implicit request =>

      for {
        _ <- sessionRepository.clear(request.identifier).recover { case _ => true }
      } yield {
        Redirect(appConfig.signOutUrl, Map("continue" -> Seq(appConfig.feedbackService)))
      }

  }

  def logoutNoSurvey: Action[AnyContent] = authenticate.async {
    implicit request =>

      for {
        _ <- sessionRepository.clear(request.identifier).recover { case _ => true }
      } yield {
        Results.Redirect(appConfig.signOutUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))
      }
  }
}
