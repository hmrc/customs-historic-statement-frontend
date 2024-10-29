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

package models

import base.SpecBase
import play.api.libs.json.{JsResultException, JsString}
import play.api.mvc.PathBindable

class FileRoleSpec extends SpecBase {

  "FileRole" should {
    "return a JsSuccess for DutyDeferment" in {
      JsString("DutyDefermentStatement").as[FileRole] mustBe DutyDefermentStatement
    }

    "return a JsSuccess for C79Certificate" in {
      JsString("C79Certificate").as[FileRole] mustBe C79Certificate
    }

    "return a JsSuccess for SecurityStatement" in {
      JsString("SecurityStatement").as[FileRole] mustBe SecurityStatement
    }

    "return a JsSuccess for PostponedVATStatement" in {
      JsString("PostponedVATStatement").as[FileRole] mustBe PostponedVATStatement
    }

    "return a JsSuccess for CDSCashAccount" in {
      JsString("CDSCashAccount").as[FileRole] mustBe CDSCashAccount
    }

    "return exception for unknown file role" in {
      intercept[JsResultException] {
        JsString("Unknown").as[FileRole]
      }
    }
  }

  "FileRole.pathBinder" should {
    val pathBindable: PathBindable[FileRole] = implicitly[PathBindable[FileRole]]

    "bind 'cash-statement' to Right(CDSCashAccount)" in {
      pathBindable.bind("fileRole", "cash-statement") mustBe Right(CDSCashAccount)
    }

    "unbind CDSCashAccount to 'cash-statement'" in {
      pathBindable.unbind("fileRole", CDSCashAccount) mustBe "cash-statement"
    }

    "return Left with error for unknown file role in bind" in {
      val unknownRole = "unknown-role"
      pathBindable.bind("fileRole", unknownRole) mustBe Left(s"unknown file role: $unknownRole")
    }
  }
}
