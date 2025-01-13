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

package base

import config.FrontendAppConfig
import controllers.actions.*
import models.{C79Certificate, HistoricDates, UserAnswers}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import pages.{AccountNumber, HistoricDateRequestPage}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import uk.gov.hmrc.http.HeaderCarrier
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper.*
import play.api.test.FakeRequest
import utils.Utils.emptyString
import utils.TestData.*

import java.time.LocalDate

trait SpecBase
    extends AnyWordSpecLike
    with MockitoSugar
    with OptionValues
    with ScalaFutures
    with Matchers
    with IntegrationPatience {

  val userAnswersId = "id"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId, Json.obj())

  def populatedUserAnswers: UserAnswers = emptyUserAnswers
    .set(AccountNumber, "123")
    .success
    .value
    .set(
      HistoricDateRequestPage(C79Certificate),
      HistoricDates(LocalDate.of(year, month, day), LocalDate.of(year, month, day))
    )
    .success
    .value

  def fakeRequest(method: String = emptyString, path: String = emptyString): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("auditing.enabled" -> false, "play.filters.csp.nonce.enabled" -> false)
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  lazy val application: Application = applicationBuilder().build()

  implicit lazy val appConfig: FrontendAppConfig = application.injector.instanceOf[FrontendAppConfig]

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  implicit lazy val messages: Messages =
    application.injector.instanceOf[MessagesApi].preferred(fakeRequest(emptyString, emptyString))

  implicit val hc: HeaderCarrier = HeaderCarrier()

  implicit lazy val request: FakeRequest[AnyContentAsEmpty.type] = fakeRequest()
}
