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
import org.scalacheck.Gen
import play.api.libs.json._

class RichJsValueSpec extends SpecBase {

  val min                           = 2
  val max                           = 10
  val nonEmptyAlphaStr: Gen[String] = Gen.alphaStr.suchThat(_.nonEmpty)

  def buildJsObj[B](keys: Seq[String], values: Seq[B])(implicit writes: Writes[B]): JsObject =
    keys.zip(values).foldLeft(JsObject.empty) { case (acc, (key, value)) =>
      acc + (key -> Json.toJson[B](value))
    }

  "set" should {

    "must return an error if the path is empty" in {

      val value = Json.obj()

      value.set(JsPath, Json.obj()) mustEqual JsError("path cannot be empty")
    }

    "must set a nested value on a JsObject" in {

      val value = Json.obj(
        "foo" -> Json.obj()
      )

      val path = JsPath \ "foo" \ "bar"

      value.set(path, JsString("baz")).asOpt.value mustEqual Json.obj(
        "foo" -> Json.obj(
          "bar" -> "baz"
        )
      )
    }

    "must set a nested value on a JsArray" in {

      val value = Json.arr(Json.arr("foo"))

      val path = JsPath \ 0 \ 0

      value.set(path, JsString("bar")).asOpt.value mustEqual Json.arr(Json.arr("bar"))
    }

    "must return an error when trying to set a key on a non-JsObject" in {

      val value = Json.arr()

      val path = JsPath \ "foo"

      value.set(path, JsString("bar")) mustEqual JsError(s"cannot set a key on $value")
    }

    "must return an error when trying to set an index on a non-JsArray" in {

      val value = Json.obj()

      val path = JsPath \ 0

      value.set(path, JsString("bar")) mustEqual JsError(s"cannot set an index on $value")
    }

    "must return an error when trying to set an index other than zero on an empty array" in {

      val value = Json.arr()

      val path = JsPath \ 1

      value.set(path, JsString("bar")) mustEqual JsError("array index out of bounds: 1, []")
    }

    "must return an error when trying to set an index out of bounds" in {

      val value = Json.arr("bar", "baz")

      val path = JsPath \ 3

      value.set(path, JsString("fork")) mustEqual JsError("array index out of bounds: 3, [\"bar\",\"baz\"]")
    }

    "must set into an array which does not exist" in {

      val value = Json.obj()

      val path = JsPath \ "foo" \ 0

      value.set(path, JsString("bar")) mustEqual JsSuccess(
        Json.obj(
          "foo" -> Json.arr("bar")
        )
      )
    }

    "must set into an object which does not exist" in {

      val value = Json.obj()

      val path = JsPath \ "foo" \ "bar"

      value.set(path, JsString("baz")) mustEqual JsSuccess(
        Json.obj(
          "foo" -> Json.obj(
            "bar" -> "baz"
          )
        )
      )
    }

    "must set nested objects and arrays" in {

      val value = Json.obj()

      val path = JsPath \ "foo" \ 0 \ "bar" \ 0

      value.set(path, JsString("baz")) mustEqual JsSuccess(
        Json.obj(
          "foo" -> Json.arr(
            Json.obj(
              "bar" -> Json.arr(
                "baz"
              )
            )
          )
        )
      )
    }
  }

  "remove" should {
    "must return an error if the path is empty" in {

      val value = Json.obj()

      value.set(JsPath, Json.obj()) mustEqual JsError("path cannot be empty")
    }

    "remove a value from one of many arrays" in {

      val input = Json.obj(
        "key"  -> JsArray(Seq(Json.toJson(1), Json.toJson(2))),
        "key2" -> JsArray(Seq(Json.toJson(1), Json.toJson(2)))
      )

      val path = JsPath \ "key" \ 0

      input.remove(path) mustBe JsSuccess(
        Json.obj("key" -> JsArray(Seq(Json.toJson(2))), "key2" -> JsArray(Seq(Json.toJson(1), Json.toJson(2))))
      )
    }
  }

  "remove a value when there are nested arrays" in {

    val input = Json.obj(
      "key"  -> JsArray(Seq(JsArray(Seq(Json.toJson(1), Json.toJson(2))), Json.toJson(2))),
      "key2" -> JsArray(Seq(Json.toJson(1), Json.toJson(2)))
    )

    val path = JsPath \ "key" \ 0 \ 0

    input.remove(path) mustBe JsSuccess(
      Json.obj(
        "key"  -> JsArray(Seq(JsArray(Seq(Json.toJson(2))), Json.toJson(2))),
        "key2" -> JsArray(Seq(Json.toJson(1), Json.toJson(2)))
      )
    )
  }

  "remove the value if the last value is deleted from an array" in {
    val input = Json.obj(
      "key"  -> JsArray(Seq(Json.toJson(1))),
      "key2" -> JsArray(Seq(Json.toJson(1), Json.toJson(2)))
    )

    val path = JsPath \ "key" \ 0

    input.remove(path) mustBe JsSuccess(
      Json.obj(
        "key"  -> JsArray(),
        "key2" -> JsArray(Seq(Json.toJson(1), Json.toJson(2)))
      )
    )
  }
}
