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
import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.routes
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier

import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction(_ => Results.Ok)
  }

  implicit class Ops[A](a: A) {
    def ~[B](b: B): A ~ B = new ~(a, b)
  }

  "Auth Action" when {

    "redirect the user to unauthorised controller when has incorrect enrolments" in {
      val mockAuthConnector = mock[AuthConnector]

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any, any)(any, any))
        .thenReturn(
          Future.successful(
            Some("id") ~
              Enrolments(Set.empty)
          )
        )

      val app         = applicationBuilder().overrides().build()
      val config      = app.injector.instanceOf[FrontendAppConfig]
      val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

      val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, config, bodyParsers)
      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith("/customs/historic-statement/unauthorised")
      }
    }

    "redirect the user to technical difficulties when invalid data is returned from auth" in {
      val mockAuthConnector = mock[AuthConnector]

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any, any)(any, any))
        .thenReturn(Future.successful(Option.empty[String] ~ Enrolments(Set.empty)))

      val app         = applicationBuilder().overrides().build()
      val config      = app.injector.instanceOf[FrontendAppConfig]
      val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

      val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, config, bodyParsers)
      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result) mustBe SEE_OTHER
        redirectLocation(result).get must startWith("/customs/historic-statement/technical-difficulties")
      }
    }

    "continue journey on successful response from auth" in {
      val mockAuthConnector = mock[AuthConnector]

      when(mockAuthConnector.authorise[Option[String] ~ Enrolments](any, any)(any, any))
        .thenReturn(
          Future.successful(
            Some("id") ~
              Enrolments(Set(Enrolment("HMRC-CUS-ORG", Seq(EnrolmentIdentifier("EORINumber", "test")), "Active")))
          )
        )

      val app         = applicationBuilder().overrides().build()
      val config      = app.injector.instanceOf[FrontendAppConfig]
      val bodyParsers = app.injector.instanceOf[BodyParsers.Default]

      val authAction = new AuthenticatedIdentifierAction(mockAuthConnector, config, bodyParsers)
      val controller = new Harness(authAction)

      running(app) {
        val result = controller.onPageLoad()(fakeRequest().withHeaders("X-Session-Id" -> "someSessionId"))
        status(result) mustBe OK
      }
    }

    "the user hasn't logged in" must {

      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers       = application.injector.instanceOf[BodyParsers.Default]
        val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new MissingBearerToken),
          frontendAppConfig,
          bodyParsers
        )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user's session has expired" must {

      "redirect the user to log in " in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers       = application.injector.instanceOf[BodyParsers.Default]
        val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new BearerTokenExpired),
          frontendAppConfig,
          bodyParsers
        )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result).get must startWith(frontendAppConfig.loginUrl)
      }
    }

    "the user doesn't have sufficient enrolments" must {

      "redirect the user to the unauthorised page" in {

        val application = applicationBuilder(userAnswers = None).build()

        val bodyParsers       = application.injector.instanceOf[BodyParsers.Default]
        val frontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

        val authAction = new AuthenticatedIdentifierAction(
          new FakeFailingAuthConnector(new InsufficientEnrolments),
          frontendAppConfig,
          bodyParsers
        )

        val controller = new Harness(authAction)
        val result     = controller.onPageLoad()(fakeRequest())

        status(result) mustBe SEE_OTHER

        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }
  }
}

class FakeFailingAuthConnector @Inject() (exceptionToReturn: Throwable) extends AuthConnector {
  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}
