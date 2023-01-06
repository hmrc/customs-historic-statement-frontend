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

package controllers

import base.SpecBase
import connectors.{CustomsDataStoreConnector, CustomsFinancialsApiConnector, CustomsSessionCacheConnector, SdesConnector}
import models.DDStatementType.{Excise, Supplementary, Weekly}
import models.FileFormat.Pdf
import models.{C79Certificate, DutyDefermentStatement, DutyDefermentStatementFile, DutyDefermentStatementFileMetadata, EoriHistory, PostponedVATStatement, PostponedVatStatementFile, PostponedVatStatementFileMetadata, SecurityStatement, SecurityStatementFile, SecurityStatementFileMetadata, VatCertificateFile, VatCertificateFileMetadata}
import play.api.i18n.Messages
import play.api.test.Helpers
import play.api.test.Helpers._
import play.api.{Application, inject}

import java.time._
import scala.concurrent.Future

class HistoricStatementsControllerSpec extends SpecBase {

  "historicStatements" should {
    "return Security Statements" in new Setup {
      when(mockSdesConnector.getSecurityStatements(any)(any)).thenReturn(Future.successful(securityStatementFiles))
      val request = fakeRequest(GET, controllers.routes.HistoricStatementsController.historicStatements(SecurityStatement).url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return C79 Statements" in new Setup {
      when(mockSdesConnector.getVatCertificates(any)(any)).thenReturn(Future.successful(c79CertificateFiles))
      val request = fakeRequest(GET, controllers.routes.HistoricStatementsController.historicStatements(C79Certificate).url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return PVAT Statements" in new Setup {
      when(mockSdesConnector.getPostponedVatStatements(any)(any)).thenReturn(Future.successful(postponedVatStatementFiles))
      val request = fakeRequest(GET, controllers.routes.HistoricStatementsController.historicStatements(PostponedVATStatement).url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe OK
      }
    }

    "return Technical difficulties page when the file role does not match" in new Setup {
      val request = fakeRequest(GET, controllers.routes.HistoricStatementsController.historicStatements(DutyDefermentStatement).url)
      running(app) {
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustBe routes.TechnicalDifficultiesController.onPageLoad.url
      }
    }
  }

  "historicStatementsDutyDeferment" should {
    "return dutyDeferment statement" in new Setup{
      when(mockSessionCacheConnector.getAccountNumber(any, any)(any)).thenReturn(Future.successful(Some(someDan)))
      when(mockSdesConnector.getDutyDefermentStatements(any, any)(any)).thenReturn(Future.successful(dutyDefermentFiles))
      val request = fakeRequest(GET, routes.HistoricStatementsController.historicStatementsDutyDeferment("linkId").url).withHeaders("X-Session-Id" -> "sessionId")
      val result = route(app, request).value
      status(result) mustBe OK
    }

    "return Session Expired when Dan is not returned" in new Setup{
      when(mockSessionCacheConnector.getAccountNumber(any, any)(any)).thenReturn(Future.successful(None))
      val request = fakeRequest(GET, routes.HistoricStatementsController.historicStatementsDutyDeferment("linkId").url).withHeaders("X-Session-Id" -> "sessionId")
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
      redirectLocation(result).value mustBe routes.SessionExpiredController.onPageLoad.url
    }

    "return Unauthorised when sessionId is not present" in {
      val mockDataStoreConnector = mock[CustomsDataStoreConnector]
      when(mockDataStoreConnector.getAllEoriHistory(any)(any)).thenReturn(Future.successful(Seq.empty))
      val request = fakeRequest(GET, routes.HistoricStatementsController.historicStatementsDutyDeferment("linkId").url)
      val app = applicationBuilder().overrides(
        inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
      ).build()
      running(app) {
        val result = route(app, request).value
        status(result) mustBe UNAUTHORIZED
      }
    }
  }

  trait Setup {
    implicit val messages: Messages = Helpers.stubMessages()

    val someEori = "12345678"
    val someDan = "12345"
    val someRequestId = Some("Ab1234")
    val securityStatementFile = SecurityStatementFile("statementfile_00", "download_url_00", 99L,
      SecurityStatementFileMetadata(2017, 12, 28, 2018, 1, 1, Pdf, SecurityStatement, someEori, 500L, "0000000"))
    val securityStatementFile_2 = SecurityStatementFile("statementfile_00", "download_url_00", 99L,
      SecurityStatementFileMetadata(2017, 11, 28, 2018, 2, 2, Pdf, SecurityStatement, someEori, 500L, "0000000"))
    val securityStatementFiles = List(securityStatementFile, securityStatementFile_2)

    val c79Certificates = VatCertificateFile("statementfile_00", "download_url_00", 99L,
      VatCertificateFileMetadata(2017, 12, Pdf, C79Certificate,None))
    val c79Certificates_2 = VatCertificateFile("statementfile_00", "download_url_00", 99L,
      VatCertificateFileMetadata(2017, 11, Pdf, C79Certificate,None))
    val c79CertificateFiles = Seq(c79Certificates, c79Certificates_2)

    val postponedVatStatement = PostponedVatStatementFile("statementfile_00", "download_url_00", 99L,
      PostponedVatStatementFileMetadata(2017, 12, Pdf, PostponedVATStatement, "CDS", None))
    val postponedVatStatement_2 = PostponedVatStatementFile("statementfile_00", "download_url_00", 99L,
      PostponedVatStatementFileMetadata(2017, 11, Pdf, PostponedVATStatement, "Chief", Some("a request id")))
    val postponedVatStatementFiles = Seq(postponedVatStatement, postponedVatStatement_2)

    val dutyDeferementFile: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_03_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 3, 1, 2018, 3, 8, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), someDan, someRequestId))
    val dutyDeferementFile_2: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 2, 1, 2018, 2, 8, Pdf, DutyDefermentStatement, Supplementary, Some(true), Some("BACS"), someDan, someRequestId))
    val dutyDeferementFile_3: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 1, 1, 2018, 1, 8, Pdf, DutyDefermentStatement, Excise, Some(true), Some("BACS"), someDan, someRequestId))
    val dutyDeferementFile_4: DutyDefermentStatementFile = DutyDefermentStatementFile("2018_02_01-08.pdf", "url.pdf", 1024L,
      DutyDefermentStatementFileMetadata(2018, 1, 1, 2018, 1, 8, Pdf, DutyDefermentStatement, Weekly, Some(true), Some("BACS"), someDan, someRequestId))
    val dutyDefermentFiles = Seq(dutyDeferementFile, dutyDeferementFile_2, dutyDeferementFile_3, dutyDeferementFile_4)

    val eoriHistories = Seq(EoriHistory("eori1", Some(LocalDate.now()), Some(LocalDate.now())),
                            EoriHistory("eori2", Some(LocalDate.now().minusDays(10)), Some(LocalDate.now().minusDays(10))))

    val mockCustomsFinancialsApiConnector = mock[CustomsFinancialsApiConnector]
    val mockSdesConnector = mock[SdesConnector]
    val mockSessionCacheConnector = mock[CustomsSessionCacheConnector]
    val mockDataStoreConnector = mock[CustomsDataStoreConnector]

    val app: Application = applicationBuilder().overrides(
      inject.bind[CustomsFinancialsApiConnector].toInstance(mockCustomsFinancialsApiConnector),
      inject.bind[SdesConnector].toInstance(mockSdesConnector),
      inject.bind[CustomsSessionCacheConnector].toInstance(mockSessionCacheConnector),
      inject.bind[CustomsDataStoreConnector].toInstance(mockDataStoreConnector)
    ).build()

    when(mockCustomsFinancialsApiConnector.deleteNotification(any, any)(any)).thenReturn(Future.successful(true))
    when(mockDataStoreConnector.getAllEoriHistory(any)(any)).thenReturn(Future.successful(eoriHistories))
  }

}
