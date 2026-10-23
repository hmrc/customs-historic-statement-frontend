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

import config.FrontendAppConfig
import controllers.actions.*
import forms.HistoricDateRequestPageFormProvider
import models.{DateMessages, FileRole, HistoricDates, Mode, PostponedVATStatement}
import navigation.Navigator
import pages.{AccountNumber, HistoricDateRequestPage, IsNiAccount}
import play.api.Logger
import play.api.data.{Form, FormError}
import play.api.i18n.{I18nSupport, Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import uk.gov.hmrc.time.TaxYear
import uk.gov.hmrc.time.TaxYear.taxYearFor
import views.html.HistoricDateRequestPageView
import utils.Utils.{comma, emptyString, hyphen}

import java.time.{Clock, LocalDate, LocalDateTime, Period}
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HistoricDateRequestPageController @Inject() (
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
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val log = Logger(this.getClass)

  private val pvatStartDate: LocalDate = LocalDate.of(2021, 1, 1)

  def onPageLoad(mode: Mode, fileRole: FileRole): Action[AnyContent] = (identify andThen getData andThen requireData) {

    implicit request =>
      val referer = request.headers.get("Referer")

      val preparedForm: Form[HistoricDates] = request.userAnswers.get(HistoricDateRequestPage(fileRole)) match {
        case Some(value) if referer.exists(_.contains(appConfig.context)) =>
          formProvider(fileRole).fill(value)
        case _                                                            => formProvider(fileRole)
      }

      val backLink = appConfig.returnLink(fileRole, request.userAnswers)

      Ok(
        view(
          preparedForm,
          mode,
          fileRole,
          backLink,
          DateMessages(fileRole),
          request.userAnswers.get(AccountNumber),
          request.userAnswers.get(IsNiAccount),
          earliestDateMessage(fileRole),
          hintExampleDate
        )
      )
  }

  def onSubmit(mode: Mode, fileRole: FileRole): Action[AnyContent] =
    (identify andThen getData andThen requireData).async { implicit request =>

      val backLink = appConfig.returnLink(fileRole, request.userAnswers)

      formProvider(fileRole)
        .bindFromRequest()
        .fold(
          formWithErrors => {
            logMessageForAnalytics(fileRole, request.eori, formWithErrors)
            Future.successful(
              BadRequest(
                view(
                  formWithErrors,
                  mode,
                  fileRole,
                  backLink,
                  DateMessages(fileRole),
                  request.userAnswers.get(AccountNumber),
                  request.userAnswers.get(IsNiAccount),
                  earliestDateMessage(fileRole),
                  hintExampleDate
                )
              )
            )
          },
          value =>
            customValidation(value, formProvider(fileRole), fileRole) match {

              case Some(formWithErrors) =>
                logMessageForAnalytics(fileRole, request.eori, formWithErrors)
                Future.successful(
                  BadRequest(
                    view(
                      formWithErrors,
                      mode,
                      fileRole,
                      backLink,
                      DateMessages(fileRole),
                      request.userAnswers.get(AccountNumber),
                      request.userAnswers.get(IsNiAccount),
                      earliestDateMessage(fileRole),
                      hintExampleDate
                    )
                  )
                )

              case None =>
                for {
                  updatedAnswers <- Future.fromTry(request.userAnswers.set(HistoricDateRequestPage(fileRole), value))
                  _              <- sessionRepository.set(updatedAnswers)
                } yield Redirect(navigator.nextPage(HistoricDateRequestPage(fileRole), mode, updatedAnswers, fileRole))
            }
        )
    }

  private val errorKeyPrefix = "cf.historic.document.request.form.error."

  private def customValidation(dates: HistoricDates, form: Form[HistoricDates], fileRole: FileRole)(implicit
    messages: Messages
  ): Option[Form[HistoricDates]] = {
    val HistoricDates(start, end) = dates

    val futureDateErrors   = Seq(futureDateError("start", start), futureDateError("end", end)).flatten
    val tooRecentErrors    = Seq(tooRecentError("start", start), tooRecentError("end", end)).flatten
    val tooFarInPastErrors =
      Seq(tooFarInPastError("start", start, fileRole), tooFarInPastError("end", end, fileRole)).flatten

    val errors: Seq[FormError] =
      if (futureDateErrors.nonEmpty) {
        futureDateErrors
      } else if (end.isBefore(start)) {
        Seq(FormError("end", errorKeyPrefix + "to-date-must-be-later-than-from-date"))
      } else if (tooRecentErrors.nonEmpty) {
        tooRecentErrors
      } else if (isRangeTooWide(start, end)) {
        Seq(FormError("end", errorKeyPrefix + "date-range-too-wide"))
      } else {
        tooFarInPastErrors
      }

    if (errors.isEmpty) None else Some(errors.foldLeft(form)(_.withError(_)).fill(dates))
  }

  private def isRangeTooWide(start: LocalDate, end: LocalDate): Boolean = {
    val maximumNumberOfMonths = 6
    Period.between(start, end).toTotalMonths >= maximumNumberOfMonths
  }

  private def futureDateError(field: String, date: LocalDate): Option[FormError] =
    if (date.isAfter(today)) {
      Some(FormError(field, s"$errorKeyPrefix$field.date-in-future"))
    } else {
      None
    }

  private def tooRecentError(field: String, date: LocalDate): Option[FormError] = {
    val minimumAgeInMonths = 6

    if (Period.between(date, today.minusMonths(1)).toTotalMonths < minimumAgeInMonths) {
      Some(FormError(field, s"$errorKeyPrefix$field.date-too-recent"))
    } else {
      None
    }
  }

  private def tooFarInPastError(field: String, date: LocalDate, fileRole: FileRole)(implicit
    messages: Messages
  ): Option[FormError] =
    if (isBeforeEarliestDate(date, fileRole)) {
      val earliest = earliestDate(fileRole)

      Some(
        FormError(
          field,
          messages(
            s"$errorKeyPrefix$field.date-too-far-in-past",
            messages(s"month.${earliest.getMonthValue}"),
            earliest.getYear.toString
          )
        )
      )
    } else {
      None
    }

  private def today: LocalDate = LocalDateTime.now(clock).toLocalDate

  private def minTaxYear: TaxYear = {
    val maximumNumberOfYears = 6
    taxYearFor(today).back(maximumNumberOfYears)
  }

  private def earliestDate(fileRole: FileRole): LocalDate = {
    val cy6StartDate = minTaxYear.starts.withDayOfMonth(1)

    fileRole match {
      case PostponedVATStatement if pvatStartDate.isAfter(cy6StartDate) => pvatStartDate
      case _                                                            => cy6StartDate
    }
  }

  private def earliestDateMessage(fileRole: FileRole)(implicit messages: Messages): String = {
    val date = earliestDate(fileRole)

    messages(
      s"cf.historic.document.request.inset.earliest-date.$fileRole",
      messages(s"month.${date.getMonthValue}"),
      date.getYear.toString
    )
  }

  private def hintExampleDate: LocalDate = today.minusYears(1)

  private def isBeforeEarliestDate(requestedDate: LocalDate, fileRole: FileRole): Boolean =
    requestedDate.withDayOfMonth(1).isBefore(earliestDate(fileRole))

  private def logMessageForAnalytics(fileRole: FileRole, eori: String, formWithErrors: Form[HistoricDates])(implicit
    messages: Messages
  ): Unit = {
    val errorMessages = formWithErrors.errors.map(e => messages(e.message)).mkString(comma)

    val startDate = formWithErrors.data.getOrElse("start.year", emptyString) + hyphen +
      formWithErrors.data.getOrElse("start.month", emptyString)

    val endDate = formWithErrors.data.getOrElse("end.year", emptyString) + hyphen +
      formWithErrors.data.getOrElse("end.month", emptyString)

    log.warn(
      s"$fileRole, Historic statement request service, eori number: $eori, " +
        s"start date: $startDate, end date: $endDate, error: $errorMessages"
    )
  }
}
