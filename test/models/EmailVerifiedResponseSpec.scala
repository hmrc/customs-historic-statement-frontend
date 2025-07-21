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
import play.api.libs.json.{JsValue, Json}

class EmailVerifiedResponseSpec extends SpecBase {

  "EmailVerifiedResponse" should {
    "serialize and deserialize verified email response" in new Setup {
      Json.toJson(verifiedResponse) mustBe verifiedResponseJson
      verifiedResponseJson.as[EmailVerifiedResponse] mustBe verifiedResponse
    }

    "serialize and deserialize unverified email response" in new Setup {
      Json.toJson(unverifiedResponse) mustBe unverifiedResponseJson
      unverifiedResponseJson.as[EmailUnverifiedResponse] mustBe unverifiedResponse
    }

    "deserialize with missing verifiedEmail should return None" in {
      val json = Json.parse("""{}""")
      json.as[EmailVerifiedResponse] mustBe EmailVerifiedResponse(None)
    }

    "deserialize with missing unVerifiedEmail should return None" in {
      val json = Json.parse("""{}""")
      json.as[EmailUnverifiedResponse] mustBe EmailUnverifiedResponse(None)
    }

    "deserialize verifiedEmail as null should return None" in {
      val json = Json.parse("""{"verifiedEmail": null}""")
      json.as[EmailVerifiedResponse] mustBe EmailVerifiedResponse(None)
    }
  }

  "UnverifiedEmail" should {
    "be a subtype of EmailResponses" in {
      val response: EmailResponses = UnverifiedEmail
      response mustBe a[EmailResponses]
    }
  }

  "UndeliverableEmail" should {
    "store email address" in {
      val response = UndeliverableEmail("bounce@example.com")
      response.email mustBe "bounce@example.com"
    }
  }

  trait Setup {
    val verifiedResponse: EmailVerifiedResponse = EmailVerifiedResponse(Some("verified response"))

    val verifiedResponseJson: JsValue = Json.parse(
      """{
        | "verifiedEmail":"verified response"
        |}""".stripMargin
    )

    val unverifiedResponse: EmailUnverifiedResponse = EmailUnverifiedResponse(Some("unverified response"))

    val unverifiedResponseJson: JsValue = Json.parse(
      """{
        | "unVerifiedEmail":"unverified response"
        |}""".stripMargin
    )
  }
}
