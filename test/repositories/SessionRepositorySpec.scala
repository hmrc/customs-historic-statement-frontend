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

package repositories

import base.SpecBase
import models.UserAnswers
import play.api.{Application, Configuration}
import uk.gov.hmrc.mongo.play.PlayMongoComponent

import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends SpecBase {

  "DefaultSessionRepository" should {

    "get the document for the given id" in new Setup {
      for {
        _                           <- defaultSessionRepository.set(userAnswers)
        result: Option[UserAnswers] <- defaultSessionRepository.get("id")
      } yield result mustBe Some(userAnswers)
    }

    "delete the document for the given id" in new Setup {
      for {
        _               <- defaultSessionRepository.set(userAnswers)
        result: Boolean <- defaultSessionRepository.clear("id")
      } yield result mustBe true
    }
  }

  trait Setup {
    val app: Application = applicationBuilder().build()

    val appConfig: Configuration           = app.injector.instanceOf[Configuration]
    val mongoComponent: PlayMongoComponent = app.injector.instanceOf[PlayMongoComponent]

    val defaultSessionRepository = new DefaultSessionRepository(mongoComponent, appConfig)

    val userAnswers: UserAnswers = populatedUserAnswers
  }
}
