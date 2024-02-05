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

package services

import models._

import javax.inject.Singleton
import scala.language.implicitConversions


@Singleton
class SdesGatekeeperService() {

  implicit def convertToVatCertificateFile(sdesResponseFile: FileInformation): VatCertificateFile = {
    val metadata = sdesResponseFile.metadata.asMap

    VatCertificateFile(
      sdesResponseFile.filename,
      sdesResponseFile.downloadURL,
      sdesResponseFile.fileSize,
      VatCertificateFileMetadata(
        metadata("PeriodStartYear").toInt,
        metadata("PeriodStartMonth").toInt,
        FileFormat(metadata("FileType")),
        mapFileRole(metadata("FileRole")),
        metadata.get("statementRequestID"))
    )
  }

  implicit def convertToPostponedVatStatementFile(sdesResponseFile: FileInformation): PostponedVatStatementFile = {
    val metadata = sdesResponseFile.metadata.asMap

    PostponedVatStatementFile(
      sdesResponseFile.filename,
      sdesResponseFile.downloadURL,
      sdesResponseFile.fileSize,
      PostponedVatStatementFileMetadata(
        metadata("PeriodStartYear").toInt,
        metadata("PeriodStartMonth").toInt,
        FileFormat(metadata("FileType")),
        mapFileRole(metadata("FileRole")),
        mapDutyPaymentMethod(metadata("DutyPaymentMethod")),
        metadata.get("statementRequestID"))
      )
  }

  implicit def convertToSecurityStatementFile(sdesResponseFile: FileInformation): SecurityStatementFile = {
    val metadata = sdesResponseFile.metadata.asMap

    SecurityStatementFile(
      sdesResponseFile.filename,
      sdesResponseFile.downloadURL,
      sdesResponseFile.fileSize,
      SecurityStatementFileMetadata(
        metadata("PeriodStartYear").toInt,
        metadata("PeriodStartMonth").toInt,
        metadata("PeriodStartDay").toInt,
        metadata("PeriodEndYear").toInt,
        metadata("PeriodEndMonth").toInt,
        metadata("PeriodEndDay").toInt,
        FileFormat(metadata("FileType")),
        mapFileRole(metadata("FileRole")),
        metadata.getOrElse("eoriNumber", "MISSING EORI NUMBER"),
        metadata.getOrElse("fileSize", sdesResponseFile.fileSize.toString).toLong,
        metadata.getOrElse("checksum", "MISSING CHECKSUM"),
        metadata.get("statementRequestID"))
    )
  }

  implicit def convertToDutyDefermentStatementFile(sdesResponseFile: FileInformation): DutyDefermentStatementFile = {
    val metadata = sdesResponseFile.metadata.asMap

    DutyDefermentStatementFile(
      sdesResponseFile.filename,
      sdesResponseFile.downloadURL,
      sdesResponseFile.fileSize,
      DutyDefermentStatementFileMetadata(
        metadata("PeriodStartYear").toInt,
        metadata("PeriodStartMonth").toInt,
        metadata("PeriodStartDay").toInt,
        metadata("PeriodEndYear").toInt,
        metadata("PeriodEndMonth").toInt,
        metadata("PeriodEndDay").toInt,
        FileFormat(metadata("FileType")),
        mapFileRole(metadata("FileRole")),
        DDStatementType(metadata("DefermentStatementType")),
        Some(mapDutyOverLimit(metadata.getOrElse("DutyOverLimit", "false"))),
        Some(metadata.getOrElse("DutyPaymentType", "Unknown")),
        metadata.getOrElse("DAN", "Unknown"),
        metadata.get("statementRequestID"))
    )
  }

  def convertTo[T <: SdesFile](implicit converter: FileInformation => T): Seq[FileInformation] =>
    Seq[T] = _.map(converter)

  private def mapFileRole(role: String) : FileRole = {
    role match {
      case "C79Certificate" => C79Certificate
      case "SecurityStatement" => SecurityStatement
      case "DutyDefermentStatement" => DutyDefermentStatement
      case "PostponedVATStatement" => PostponedVATStatement
      case _ => throw new Exception(s"Unknown file role: $role")
    }
  }

  private def mapDutyPaymentMethod(dutyPaymentMethod: String): String = {
    dutyPaymentMethod match {
      case "Chief" => "CHIEF"
      case _ => "CDS"
    }
  }
  private def mapDutyOverLimit(MDGDutyOverLimitResponse: String): Boolean = {
    MDGDutyOverLimitResponse match {
      case "Y" => true
      case _   => false
    }
  }
}
