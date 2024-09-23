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

import play.api.libs.json._
import play.api.mvc.PathBindable

sealed abstract class FileRole(val name: String)

case object DutyDefermentStatement extends FileRole("DutyDefermentStatement")
case object C79Certificate extends FileRole("C79Certificate")
case object SecurityStatement extends FileRole("SecurityStatement")
case object PostponedVATStatement extends FileRole("PostponedVATStatement")
case object CashStatement extends FileRole("CashStatement")

object FileRole {
  implicit val format: Format[FileRole] = new Format[FileRole] {
    override def reads(json: JsValue): JsResult[FileRole] = {
      json match {
        case JsString("DutyDefermentStatement") => JsSuccess(DutyDefermentStatement)
        case JsString("C79Certificate") => JsSuccess(C79Certificate)
        case JsString("SecurityStatement") => JsSuccess(SecurityStatement)
        case JsString("PostponedVATStatement") => JsSuccess(PostponedVATStatement)
        case JsString("CashStatement") => JsSuccess(CashStatement)
        case e => JsError(s"Invalid file role: $e")
      }
    }
    override def writes(o: FileRole): JsValue = JsString(o.name)
  }

  implicit val pathBinder: PathBindable[FileRole] = new PathBindable[FileRole] {
    override def bind(key: String, value: String): Either[String, FileRole] = {
      value match {
        case "import-vat" => Right(C79Certificate)
        case "duty-deferment" => Right(DutyDefermentStatement)
        case "adjustments" => Right(SecurityStatement)
        case "postponed-vat" => Right(PostponedVATStatement)
        case "cash-statement" => Right(CashStatement)
        case fileRole => Left(s"unknown file role: $fileRole")
      }
    }

    override def unbind(key: String, fileRole: FileRole): String = {
      fileRole match {
        case C79Certificate => "import-vat"
        case DutyDefermentStatement => "duty-deferment"
        case SecurityStatement => "adjustments"
        case PostponedVATStatement => "postponed-vat"
        case CashStatement => "cash-statement"
      }
    }
  }
}
