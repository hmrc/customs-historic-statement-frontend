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

package controllers.actions

import handlers.ErrorHandler
import models.requests.{IdentifierRequestWithEoriHistory, IdentifierRequestWithEoriHistoryAndSessionId}
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{ActionRefiner, Result}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SessionIdFilterImpl @Inject()(implicit val executionContext: ExecutionContext, errorHandler: ErrorHandler) extends SessionIdFilter {
  override protected def refine[A](request: IdentifierRequestWithEoriHistory[A]): Future[Either[Result, IdentifierRequestWithEoriHistoryAndSessionId[A]]] = {
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
    hc.sessionId match {
      case None => Future.successful(Left(Unauthorized(errorHandler.unauthorized()(request))))
      case Some(sessionId) => Future.successful(Right(IdentifierRequestWithEoriHistoryAndSessionId(
        request,
        request.identifier,
        request.eori,
        request.eoriHistory,
        sessionId.value
      )))
    }
  }
}

trait SessionIdFilter extends ActionRefiner[IdentifierRequestWithEoriHistory, IdentifierRequestWithEoriHistoryAndSessionId]