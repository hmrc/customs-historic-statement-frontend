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

import base.SpecBase
import models.{C79Certificate, DutyDefermentStatement, NormalMode, PostponedVATStatement, SecurityStatement, UserAnswers}
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import pages.{HistoricDateRequestPage, RequestedLinkId}
import play.api.test.Helpers._
import play.api.{Application, inject}
import repositories.SessionRepository

import java.time._
import scala.concurrent.Future

class HistoricDateRequestPageControllerSpec extends SpecBase {

  "onPageLoad" should {
    "return OK when there is no pre-populated data in the user answers" in {
      val app = applicationBuilder(Some(populatedUserAnswers.remove(HistoricDateRequestPage(C79Certificate)).success.value)).build()
      val request = fakeRequest(GET, routes.HistoricDateRequestPageController.onPageLoad(NormalMode, C79Certificate).url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return OK when there is populated data in the user answers" in {
      val app = applicationBuilder(Some(populatedUserAnswers)).build()
      val request = fakeRequest(GET, routes.HistoricDateRequestPageController.onPageLoad(NormalMode, C79Certificate).url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return session expired when no user answers provided" in {
      val app = applicationBuilder().build()
      val request = fakeRequest(GET, routes.HistoricDateRequestPageController.onPageLoad(NormalMode, C79Certificate).url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad.url
      }
    }
  }

  "onSubmit" should {
    "redirect to the check your answers page when valid data has been submitted" in new Setup {
      when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.CheckYourAnswersController.onPageLoad(C79Certificate).url
      }
    }

    "return ok when the dates are later than earliest possible date for DutyDeferment statement" in new Setup {
      val userAnswers: UserAnswers = populatedUserAnswers.set(RequestedLinkId, "someId").success.value

      override val app: Application = applicationBuilder(Some(userAnswers)).build()

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, DutyDefermentStatement).url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "11", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
      }
    }

    "return BAD_REQUEST when the start date is too recent for C79" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody(
          "start.month" -> earliestMonthInCurrentPeriod.getMonthValue.toString,
          "start.year" -> earliestMonthInCurrentPeriod.getYear.toString,
          "end.month" -> earliestMonthInCurrentPeriod.getMonthValue.toString,
          "end.year" -> earliestMonthInCurrentPeriod.getYear.toString)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is too recent for PVAT" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, PostponedVATStatement).url)
        .withFormUrlEncodedBody(
          "start.month" -> earliestMonthInCurrentPeriod.getMonthValue.toString,
          "start.year" -> earliestMonthInCurrentPeriod.getYear.toString,
          "end.month" -> earliestMonthInCurrentPeriod.getMonthValue.toString,
          "end.year" -> earliestMonthInCurrentPeriod.getYear.toString)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is less than 4 digits for PVAT" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, PostponedVATStatement).url)
        .withFormUrlEncodedBody(
          "start.month" -> earliestMonthInCurrentPeriod.getMonthValue.toString,
          "start.year" -> "201",
          "end.month" -> earliestMonthInCurrentPeriod.getMonthValue.toString,
          "end.year" -> earliestMonthInCurrentPeriod.getYear.toString)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return OK when the end date is the earliest possible" in new Setup {
      when(mockSessionRepository.set(any)).thenReturn(Future.successful(true))

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody(
          "start.month" -> lastestMonthInLastPeriod.getMonthValue.toString,
          "start.year" -> lastestMonthInLastPeriod.getYear.toString,
          "end.month" -> lastestMonthInLastPeriod.getMonthValue.toString,
          "end.year" -> lastestMonthInLastPeriod.getYear.toString)

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
      }
    }

    "return BAD_REQUEST when the start date is earlier than system start date for C79" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is earlier than system start date for securities statement" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, SecurityStatement).url)
        .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the year is not equals to 4 for C79" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "201", "end.month" -> "10", "end.year" -> "2020")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the year is not equals to 4 for DutyDeferment statement" in new Setup {
      val userAnswers: UserAnswers = populatedUserAnswers.set(RequestedLinkId, "someId").success.value

      override val app: Application = applicationBuilder(Some(userAnswers)).build()

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, DutyDefermentStatement).url)
        .withFormUrlEncodedBody("start.month" -> "7", "start.year" -> "200", "end.month" -> "1", "end.year" -> "20201")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the year is not equals to 4 for Security statement" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, SecurityStatement).url)
        .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "19", "end.month" -> "10", "end.year" -> "2020")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the year is not equals to 4 for PVAT statement" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, PostponedVATStatement).url)
        .withFormUrlEncodedBody("start.month" -> "9", "start.year" -> "2021", "end.month" -> "10", "end.year" -> "20211")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is earlier than postponed VAT date" in new Setup {
      override val app: Application = applicationBuilder(Some(populatedUserAnswers)).build()

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, PostponedVATStatement).url)
        .withFormUrlEncodedBody("start.month" -> "12", "start.year" -> "2020", "end.month" -> "1", "end.year" -> "2021")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return Ok when the start date is later than the earliest possible postponed VAT date" in new Setup {
      override val app: Application = applicationBuilder(Some(populatedUserAnswers)).build()

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.month" -> "1", "start.year" -> "2021", "end.month" -> "1", "end.year" -> "2021")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
      }
    }

    "return BAD_REQUEST when the start date is earlier than duty deferment date" in new Setup {

      val userAnswers: UserAnswers = populatedUserAnswers.set(RequestedLinkId, "someId").success.value

      override val app: Application = applicationBuilder(Some(userAnswers)).build()

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, DutyDefermentStatement).url)
        .withFormUrlEncodedBody("start.month" -> "7", "start.year" -> "2018", "end.month" -> "1", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when invalid data submitted" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.invalid" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the start date is after the end date" in new Setup {
      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.month" -> "11", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the requested data exceeds 6 months" in new Setup {

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "5", "end.year" -> "2020")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

    "return BAD_REQUEST when the requested data exceeds 6 years in the past" in {
      val mockSessionRepository: SessionRepository = mock[SessionRepository]
      val fixedInstant: Instant = LocalDateTime.parse("2060-01-01T00:00:00.000").toInstant(ZoneOffset.UTC)
      val stubClock: Clock = Clock.fixed(fixedInstant, ZoneId.systemDefault)

      val app: Application = applicationBuilder(Some(populatedUserAnswers)).overrides(
        inject.bind[SessionRepository].toInstance(mockSessionRepository),
        inject.bind[Clock].toInstance(stubClock)
      ).build()

      val request = fakeRequest(POST, routes.HistoricDateRequestPageController.onSubmit(NormalMode, C79Certificate).url)
        .withFormUrlEncodedBody("start.month" -> "10", "start.year" -> "2019", "end.month" -> "10", "end.year" -> "2019")

      running(app) {
        val result = route(app, request).value
        status(result) mustBe BAD_REQUEST
      }
    }

  }

  trait Setup {
    val earliestMonthInCurrentPeriod = LocalDateTime.now().minusMonths(6)
    val lastestMonthInLastPeriod = earliestMonthInCurrentPeriod.minusMonths(1)

    val mockSessionRepository: SessionRepository = mock[SessionRepository]

    val app: Application = applicationBuilder(Some(populatedUserAnswers)).overrides(
      inject.bind[SessionRepository].toInstance(mockSessionRepository)
    ).build()

  }

}