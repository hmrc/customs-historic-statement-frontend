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

import helpers.Formatters
import play.api.Logging
import play.api.libs.json._
import utils.Utils.emptyString

import java.time.LocalDate
import scala.collection.immutable.SortedSet

sealed abstract class FileFormat(val name: String) extends Ordered[FileFormat] {
  val order: Int

  def compare(that: FileFormat): Int = order.compare(that.order)

  override def toString: String = name
}

object FileFormat extends Logging {

  case object Pdf extends FileFormat("PDF") {
    val order = 1
  }

  case object Csv extends FileFormat(name = "CSV") {
    val order = 2
  }

  case object UnknownFileFormat extends FileFormat("UNKNOWN FILE FORMAT") {
    val order = 99
  }

  val SdesFileFormats: SortedSet[FileFormat] = SortedSet(Pdf, Csv)
  val CashStatementFileFormats: Set[FileFormat] = SortedSet(Csv)
  val OtherStatementFileFormats: Set[FileFormat] = SortedSet(Pdf)

  def filterFileFormats[T <: SdesFile](allowedFileFormats: SortedSet[FileFormat])(
    files: Seq[T]): Seq[T] = files.filter(file => allowedFileFormats(file.metadata.fileFormat))

  def apply(name: String): FileFormat = name.toUpperCase match {
    case Pdf.name => Pdf
    case Csv.name => Csv
    case _ =>
      logger.warn(s"Unknown file format: $name")
      UnknownFileFormat
  }

  def unapply(arg: FileFormat): Option[String] = Some(arg.name)

  implicit val fileFormatFormat: Format[FileFormat] = new Format[FileFormat] {
    def reads(json: JsValue): JsSuccess[FileFormat] = JsSuccess(apply(json.as[String]))

    def writes(obj: FileFormat): JsString = JsString(obj.name)
  }
}

sealed abstract class DDStatementType(val name: String) extends Ordered[DDStatementType] {
  val order: Int

  def compare(that: DDStatementType): Int = order.compare(that.order)
}

object DDStatementType extends Logging {
  case object Excise extends DDStatementType("Excise") {
    val order = 1
  }

  case object Supplementary extends DDStatementType(name = "Supplementary") {
    val order = 2
  }

  case object Weekly extends DDStatementType("Weekly") {
    val order = 3
  }

  case object UnknownStatementType extends DDStatementType("UNKNOWN STATEMENT TYPE") {
    val order = 4
  }

  def apply(name: String): DDStatementType = name match {
    case Weekly.name => Weekly
    case Supplementary.name => Supplementary
    case Excise.name => Excise
    case _ =>
      logger.warn(s"Unknown duty deferment statement type: $name")
      UnknownStatementType
  }

  def unapply(arg: DDStatementType): Option[String] = Some(arg.name)
}

trait SdesFileMetadata {
  def fileFormat: FileFormat

  def fileRole: FileRole

  def periodStartYear: Int

  def periodStartMonth: Int
}

trait SdesFile {
  def metadata: SdesFileMetadata

  def downloadURL: String

  val fileFormat: FileFormat = metadata.fileFormat
  val monthAndYear: LocalDate = LocalDate.of(metadata.periodStartYear, metadata.periodStartMonth, 1)
}

case class DutyDefermentStatementFile(filename: String,
                                      downloadURL: String,
                                      size: Long,
                                      metadata: DutyDefermentStatementFileMetadata)
  extends Ordered[DutyDefermentStatementFile] with SdesFile {

  def compare(that: DutyDefermentStatementFile): Int = fileFormat.compare(that.fileFormat)

  val startDate: LocalDate = LocalDate.of(metadata.periodStartYear, metadata.periodStartMonth, metadata.periodStartDay)
  val endDate: LocalDate = LocalDate.of(metadata.periodEndYear, metadata.periodEndMonth, metadata.periodEndDay)
}

case class DutyDefermentStatementFileMetadata(periodStartYear: Int,
                                              periodStartMonth: Int,
                                              periodStartDay: Int,
                                              periodEndYear: Int,
                                              periodEndMonth: Int,
                                              periodEndDay: Int,
                                              fileFormat: FileFormat,
                                              fileRole: FileRole,
                                              defermentStatementType: DDStatementType,
                                              dutyOverLimit: Option[Boolean],
                                              dutyPaymentType: Option[String],
                                              dan: String,
                                              statementRequestId: Option[String] = None) extends SdesFileMetadata

case class SecurityStatementFile(filename: String,
                                 downloadURL: String,
                                 size: Long,
                                 metadata: SecurityStatementFileMetadata
                                ) extends Ordered[SecurityStatementFile] with SdesFile {

  val startDate: LocalDate = LocalDate.of(metadata.periodStartYear, metadata.periodStartMonth, metadata.periodStartDay)
  val endDate: LocalDate = LocalDate.of(metadata.periodEndYear, metadata.periodEndMonth, metadata.periodEndDay)
  val formattedSize: String = Formatters.fileSize(metadata.fileSize)

  def compare(that: SecurityStatementFile): Int = startDate.compareTo(that.startDate)
}

case class SecurityStatementFileMetadata(periodStartYear: Int,
                                         periodStartMonth: Int,
                                         periodStartDay: Int,
                                         periodEndYear: Int,
                                         periodEndMonth: Int,
                                         periodEndDay: Int,
                                         fileFormat: FileFormat,
                                         fileRole: FileRole,
                                         eoriNumber: String,
                                         fileSize: Long,
                                         checksum: String,
                                         statementRequestId: Option[String] = None) extends SdesFileMetadata

case class VatCertificateFile(filename: String,
                              downloadURL: String,
                              size: Long,
                              metadata: VatCertificateFileMetadata,
                              eori: String = emptyString)
  extends Ordered[VatCertificateFile] with SdesFile {

  val formattedSize: String = Formatters.fileSize(size)

  def compare(that: VatCertificateFile): Int = that.metadata.fileFormat.compare(metadata.fileFormat)
}

case class VatCertificateFileMetadata(periodStartYear: Int,
                                      periodStartMonth: Int,
                                      fileFormat: FileFormat,
                                      fileRole: FileRole,
                                      statementRequestId: Option[String]) extends SdesFileMetadata

case class PostponedVatStatementFile(filename: String,
                                     downloadURL: String,
                                     size: Long,
                                     metadata: PostponedVatStatementFileMetadata,
                                     eori: String = emptyString)
  extends Ordered[PostponedVatStatementFile] with SdesFile {

  val formattedSize: String = Formatters.fileSize(size)

  def compare(that: PostponedVatStatementFile): Int = that.metadata.fileFormat.compare(metadata.fileFormat)
}

case class PostponedVatStatementFileMetadata(periodStartYear: Int,
                                             periodStartMonth: Int,
                                             fileFormat: FileFormat,
                                             fileRole: FileRole,
                                             source: String,
                                             statementRequestId: Option[String]) extends SdesFileMetadata

case class CashStatementFile(filename: String,
                             downloadURL: String,
                             size: Long,
                             metadata: CashStatementFileMetadata,
                             eori: String = emptyString)
  extends Ordered[CashStatementFile] with SdesFile {

  val formattedSize: String = Formatters.fileSize(size)

  def compare(that: CashStatementFile): Int = that.metadata.fileFormat.compare(metadata.fileFormat)
}

case class CashStatementFileMetadata(periodStartYear: Int,
                                     periodStartMonth: Int,
                                     periodStartDay: Int,
                                     periodEndYear: Int,
                                     periodEndMonth: Int,
                                     periodEndDay: Int,
                                     fileFormat: FileFormat,
                                     fileRole: FileRole,
                                     cashAccountNumber: Option[String],
                                     statementRequestId: Option[String] = None) extends SdesFileMetadata
