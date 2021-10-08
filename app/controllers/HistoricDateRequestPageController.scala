/*
 * Copyright 2021 HM Revenue & Customs
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

import config.FrontendAppConfig
import controllers.actions._
import forms.HistoricDateRequestPageFormProvider
import models.{FileRole, HistoricDates, Mode, PostponedVATStatement}
import navigation.Navigator
import pages.{AccountNumber, HistoricDateRequestPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.time.TaxYear
import uk.gov.hmrc.time.TaxYear.taxYearFor
import views.html.HistoricDateRequestPageView

import java.time.{Clock, LocalDate, LocalDateTime, Period}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HistoricDateRequestPageController @Inject()(
                                                   override val messagesApi: MessagesApi,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Navigator,
                                                   appConfig: FrontendAppConfig,
                                                   identify: IdentifierAction,
                                                   getData: DataRetrievalAction,
                                                   requireData: DataRequiredAction,
                                                   clock: Clock,
                                                   formProvider: HistoricDateRequestPageFormProvider,
                                                   val controllerComponents: MessagesControllerComponents,
                                                   view: HistoricDateRequestPageView
                                                 )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData) {
    implicit request =>

      val preparedForm: Form[HistoricDates] = request.userAnswers.get(HistoricDateRequestPage) match {
        case None => formProvider(request.fileRole)
        case Some(value) => formProvider(request.fileRole).fill(value)
      }

      val backLink = appConfig.returnLink(request.fileRole, request.userAnswers)

      Ok(view(preparedForm, mode, request.fileRole, backLink, request.userAnswers.get(AccountNumber)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData andThen requireData).async {
    implicit request =>

      val backLink = appConfig.returnLink(request.fileRole, request.userAnswers)

      formProvider(request.fileRole).bindFromRequest().fold(
        formWithErrors =>
          Future.successful(BadRequest(view(formWithErrors, mode, request.fileRole, backLink, request.userAnswers.get(AccountNumber)))),
        value =>
          customValidation(value, formProvider(request.fileRole), request.fileRole) match {
            case Some(formWithErrors) =>
              Future.successful(BadRequest(view(formWithErrors, mode, request.fileRole, backLink, request.userAnswers.get(AccountNumber))))
            case None =>
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(HistoricDateRequestPage, value))
                _ <- sessionRepository.set(updatedAnswers)
              } yield Redirect(navigator.nextPage(HistoricDateRequestPage, mode, updatedAnswers))
          }
      )
  }

  def customValidation(dates: HistoricDates, form: Form[HistoricDates], fileRole: FileRole)(implicit messages: Messages): Option[Form[HistoricDates]] = {
    val maximumNumberOfMonths = 6

    def formWithError(message: String): Form[HistoricDates] = {
      form.withError("start", message)
        .withError("end", message)
        .fill(dates)
    }

    (dates, fileRole) match {
      case (HistoricDates(start, end), PostponedVATStatement) if (isBeforePVatStartDate(start) || isBeforePVatStartDate(end))  =>
          Some(formWithError(messages("cf.historic.document.request.form.error.date-earlier-than-pvat-start-date"
        )))
      case (HistoricDates(start, end), _) if Period.between(start, end).toTotalMonths < 0 =>
        Some(formWithError("cf.historic.document.request.form.error.to-date-must-be-later-than-from-date"))
      case (HistoricDates(start, end), _) if Period.between(start, end).toTotalMonths >= maximumNumberOfMonths =>
        Some(formWithError("cf.historic.document.request.form.error.date-range-too-wide"))
      case (HistoricDates(start, end), _) if isDateMoreThanSixTaxYearsOld(start) || isDateMoreThanSixTaxYearsOld(end) =>
        Some(formWithError(messages(
          "cf.historic.document.request.form.error.date-too-far-in-past",
          minTaxYear.startYear.toString,
          minTaxYear.finishYear.toString
    )))
      case _ => None
    }
  }

  def minTaxYear: TaxYear = {
    lazy val currentDate: LocalDate = LocalDateTime.now(clock).toLocalDate
    val maximumNumberOfYears = 6
    taxYearFor(currentDate).back(maximumNumberOfYears)
  }

  private def isDateMoreThanSixTaxYearsOld(requestedDate: LocalDate): Boolean = {
    val dayOfMonthThatTaxYearStartsOn = 6
    minTaxYear.starts.isAfter(requestedDate.withDayOfMonth(dayOfMonthThatTaxYearStartsOn))
  }

  private def isBeforePVatStartDate(requestedDate: LocalDate): Boolean = {
    requestedDate.isBefore(LocalDate.of(2021,1,1))
  }
}
