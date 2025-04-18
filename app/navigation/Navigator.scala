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

package navigation

import controllers.routes
import models._
import pages._
import play.api.mvc.Call

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject() () {

  private def normalRoutes(fileRole: FileRole): Page => UserAnswers => Call = {
    case HistoricDateRequestPage(fileRole) => _ => routes.CheckYourAnswersController.onPageLoad(fileRole)
    case _                                 => _ => routes.HistoricDateRequestPageController.onPageLoad(NormalMode, fileRole)
  }

  private def checkRouteMap(fileRole: FileRole): Page => UserAnswers => Call = { case _ =>
    _ => routes.CheckYourAnswersController.onPageLoad(fileRole)
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, fileRole: FileRole): Call = mode match {
    case NormalMode =>
      normalRoutes(fileRole)(page)(userAnswers)
    case CheckMode  =>
      checkRouteMap(fileRole)(page)(userAnswers)
  }
}
