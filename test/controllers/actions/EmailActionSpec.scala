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

package controllers.actions

import base.SpecBase
import connectors.CustomsDataStoreConnector
import models.requests.IdentifierRequest
import models.{UndeliverableEmail, UnverifiedEmail}
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.{Application, inject}
import uk.gov.hmrc.auth.core.retrieve.Email
import uk.gov.hmrc.http.ServiceUnavailableException
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class EmailActionSpec extends SpecBase {

  "filter" should {
    "Let requests with validated email through" in new Setup {
      running(app) {
        when(mockDataStoreService.getEmail(any)(any)).thenReturn(
          Future.successful(Right(Email("last.man@standing.co.uk"))))

        val response = await(emailAction.filter(authenticatedRequest))
        response mustBe None
      }
    }


    "Let request through, when getEmail throws service unavailable exception" in new Setup {
      running(app) {
        when(mockDataStoreService.getEmail(any)(any)).thenReturn(Future.failed(new ServiceUnavailableException("")))

        val response = await(emailAction.filter(authenticatedRequest))
        response mustBe None
      }
    }

    "Redirect users with unvalidated emails" in new Setup {
      running(app) {
        when(mockDataStoreService.getEmail(any)(any)).thenReturn(Future.successful(Left(UnverifiedEmail)))

        val response = await(emailAction.filter(authenticatedRequest))

        response.get.header.status mustBe SEE_OTHER
        response.get.header.headers(LOCATION) must include("/verify-your-email")
      }
    }

    "redirect the requests to undeliverable email page when dataStoreService returns undeliverable email" in new Setup {
      when(mockDataStoreService.getEmail(any)(any)).thenReturn(Future.successful(Left(UndeliverableEmail(emailId))))

      running(app) {
        val result = emailAction.filter(authenticatedRequest).map(res => res.get)

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.EmailController.showUndeliverable().url)
      }
    }
  }

  trait Setup {
    val emailId = "test@test.com"

    val mockDataStoreService: CustomsDataStoreConnector = org.mockito.Mockito.mock(classOf[CustomsDataStoreConnector])

    val app: Application = applicationBuilder().overrides(
      inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreService)
    ).build()

    val emailAction: EmailAction = app.injector.instanceOf[EmailAction]

    val authenticatedRequest: IdentifierRequest[AnyContentAsEmpty.type] = IdentifierRequest(FakeRequest("GET", "/"), "EORINumber", "GB123456789012")
  }
}
