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

package base

import controllers.actions._
import models.{HistoricDates, UserAnswers}
import org.mockito.scalatest.MockitoSugar
import org.scalatest.OptionValues
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import pages.{AccountNumber, HistoricDateRequestPage}
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.CSRFTokenHelper._
import play.api.test.FakeRequest

import java.time.LocalDate


trait SpecBase extends AnyWordSpecLike with MockitoSugar with OptionValues with ScalaFutures with Matchers with IntegrationPatience {

  val userAnswersId = "id"

  def emptyUserAnswers = UserAnswers(userAnswersId, Json.obj())

  def populatedUserAnswers =
    emptyUserAnswers
      .set(AccountNumber, "123").success.value
      .set(HistoricDateRequestPage, HistoricDates(LocalDate.of(2019, 10, 1), LocalDate.of(2019, 10, 1))).success.value

  def fakeRequest(method: String = "", path: String = ""): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(method, path).withCSRFToken.asInstanceOf[FakeRequest[AnyContentAsEmpty.type]]

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(fakeRequest("", ""))

  protected def applicationBuilder(userAnswers: Option[UserAnswers] = None): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure("auditing.enabled" -> false,
        "play.filters.csp.nonce.enabled" -> false)
      .overrides(
        bind[DataRequiredAction].to[DataRequiredActionImpl],
        bind[IdentifierAction].to[FakeIdentifierAction],
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )
}
