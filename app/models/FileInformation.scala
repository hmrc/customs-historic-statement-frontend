/*
 * Copyright 2022 HM Revenue & Customs
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

import play.api.libs.functional.syntax._
import play.api.libs.json._


case class FileInformation(filename: String,
                           downloadURL: String,
                           fileSize: Long,
                           metadata: Metadata)

case class MetadataItem(key: String, value: String)

case class Metadata(items: Seq[MetadataItem]) {
  val asMap: Map[String, String] = items.map(item => (item.key, item.value)).toMap
}

object FileInformation {
  implicit val metadataItemReads: Reads[MetadataItem] =
    ((JsPath \ "metadata").read[String] and (JsPath \ "value").read[String]) (MetadataItem.apply _)
  implicit val metadataReads: Reads[Metadata] = __.read[List[MetadataItem]].map(Metadata.apply)
  implicit val metadataItemWrites: Writes[MetadataItem] =  Json.writes[MetadataItem]
  implicit val metadataWrites: Writes[Metadata] = new Writes[Metadata] {
    override def writes(o: Metadata): JsValue = JsArray(o.items.map(
      item => Json.obj(("metadata", item.key), ("value", item.value))))
  }
  implicit val fileInformationFormats: Format[FileInformation] = Json.format[FileInformation]
}
