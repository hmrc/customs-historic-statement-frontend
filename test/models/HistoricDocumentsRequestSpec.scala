/*
 * Copyright 2025 HM Revenue & Customs
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

package models

import base.SpecBase
import models.requests.DataRequest
import pages.{AccountNumber, HistoricDateRequestPage}
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import org.scalatest.TryValues.*
import utils.TestData.*

import java.time.LocalDate

class HistoricDocumentsRequestSpec extends SpecBase {

  "HistoricStatementRequest" should {
    "serialize and deserialize correctly" in new Setup {
      Json.toJson(historicDocumentRequest) mustBe historicDocumentRequestJson
      historicDocumentRequestJson.as[HistoricDocumentRequest] mustBe historicDocumentRequest
    }

    "build from DataRequest when both date and Dan are present" in new Setup {
      val result: Option[HistoricDocumentRequest] = HistoricDocumentRequest.fromRequest(fileRole)(dataRequest)

      result mustBe Some(HistoricDocumentRequest(fileRole, fromDate, untilDate, dan))
    }

    "return None from DataRequest when HistoricDateRequestPage is empty" in new Setup {
      val emptyUserAnswers: UserAnswers = UserAnswers("user-123")
        .set(AccountNumber, "DAN1234")
        .success
        .value

      val emptyDataRequest: DataRequest[AnyContent] = DataRequest(
        fakeRequest,
        internalId = "user-123",
        userAnswers = emptyUserAnswers,
        eori = "GB12345"
      )

      val result = HistoricDocumentRequest.fromRequest(fileRole)(emptyDataRequest)
      result mustBe None
    }

  }

  trait Setup {
    val mockRequest: DataRequest[AnyContent] = mock[DataRequest[AnyContent]]

    val fileRole: FileRole   = DutyDefermentStatement
    val fromDate: LocalDate  = LocalDate.of(year, month, day)
    val untilDate: LocalDate = LocalDate.of(year2, month, day)
    val dan: Option[String]  = Some("DAN1234")

    val fakeRequest: FakeRequest[AnyContent] = FakeRequest("GET", "/some-path")

    val populateUserAnswers: UserAnswers = UserAnswers("user-123")
      .set(HistoricDateRequestPage(fileRole), HistoricDates(fromDate, untilDate))
      .success
      .value
      .set(AccountNumber, "DAN1234")
      .success
      .value

    val dataRequest: DataRequest[AnyContent] =
      DataRequest(fakeRequest, internalId = "user-123", userAnswers = populateUserAnswers, eori = "GB12345")

    val historicDocumentRequest: HistoricDocumentRequest = HistoricDocumentRequest(
      documentType = fileRole,
      from = fromDate,
      until = untilDate,
      dan = dan
    )

    val historicDocumentRequestJson: JsValue = Json.parse(
      s"""{
         |   "documentType":"DutyDefermentStatement",
         |   "from":"2018-03-14",
         |   "until":"2019-03-14",
         |   "dan":"DAN1234"
         |}""".stripMargin
    )
  }
}
