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

package uk.gov.hmrc.ngrraldfrontend.controllers

import play.api.i18n.I18nSupport
import play.api.libs.json.*
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfSecondRentPeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfSecondRentPeriodForm.*
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{DetailsOfRentPeriod, Mode, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfSecondRentPeriodPage, RentPeriodsPage}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.utils.RentPeriodsHelper.{shouldGoToRentPeriodsPageCheckMode, setRentPeriodsValueToFalseIf10thPeriodHasBeenAdded, updateRentPeriodsIfEndDateIsChanged}
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfSecondRentPeriodView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AdditionalRentPeriodController @Inject()(view: ProvideDetailsOfSecondRentPeriodView,
                                               authenticate: AuthRetrievals,
                                               mcc: MessagesControllerComponents,
                                               getData: DataRetrievalAction,
                                               checkRequestSentReference: CheckRequestSentReferenceAction,
                                               sessionRepository: SessionRepository,
                                               navigator: Navigator,
                                              )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder:

  private def getPreviousEndDate(userAnswers: UserAnswers, index: Int): Option[LocalDate] =
    userAnswers.get(ProvideDetailsOfSecondRentPeriodPage) match {
      case Some(rentPeriods) if rentPeriods.size >= index => Some(LocalDate.parse(rentPeriods(index - 1).endDate).plusDays(1))
      case _ => None
    }

  def show(mode: Mode, index: Int): Action[AnyContent] =
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val credId = CredId(request.credId)
      val userAnswers = request.userAnswers.getOrElse(UserAnswers(credId))
      val previousRentPeriodEndDate: Option[LocalDate] = getPreviousEndDate(userAnswers, index)

      previousRentPeriodEndDate match {
        case None => Future.successful(Redirect(routes.ProvideDetailsOfSecondRentPeriodController.show(mode)))
        case Some(previousEndDate) =>
          val preparedForm = userAnswers.get(ProvideDetailsOfSecondRentPeriodPage) match {
            case Some(rentPeriods) if rentPeriods.size >= index + 1 => answerToForm(rentPeriods(index), previousEndDate, index)
            case _ => form(previousEndDate, index)
          }
          Future.successful(Ok(view(
            request.property.addressFull,
            preparedForm,
            NGRDate.formatDate(previousEndDate.toString),
            endDateInput(index),
            mode = mode,
            index
          )))
      }
    }

  def submit(mode: Mode, index: Int): Action[AnyContent] =
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val previousEndDate: LocalDate = getPreviousEndDate(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))), index)
        .getOrElse(throw new NotFoundException("Can't find previous end date"))
      form(previousEndDate, index)
        .bindFromRequest()
        .fold(
          formWithErrors =>
            val correctedFormErrors = formWithErrors.errors.map { formError =>
              (formError.key, formError.messages) match
                case (key, messages) if messages.head.contains("provideDetailsOfSecondRentPeriod.endDate") =>
                  setCorrectKey(formError, "provideDetailsOfSecondRentPeriod", "endDate")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
            Future.successful(BadRequest(view(
              request.property.addressFull,
              formWithCorrectedErrors,
              NGRDate.formatDate(previousEndDate.toString),
              endDateInput(index),
              mode,
              index
            ))),
          rentPeriodDetailsForm =>
            for {
              userAnswers <- Future(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))))
              //Checking if end date has been changed. If yes, remove the details of rent periods from this period.
              rentPeriods <- Future(updateRentPeriodsIfEndDateIsChanged(userAnswers, rentPeriodDetailsForm.endDate, index))
              updatedAnswers <- Future.fromTry(userAnswers.set(ProvideDetailsOfSecondRentPeriodPage, formToAnswers(rentPeriodDetailsForm, rentPeriods, index)))
              updateRentPeriodsPageUserAnswers <- Future.fromTry(setRentPeriodsValueToFalseIf10thPeriodHasBeenAdded(updatedAnswers))
              _ <- sessionRepository.set(updateRentPeriodsPageUserAnswers)
            } yield Redirect(navigator.nextPage(ProvideDetailsOfSecondRentPeriodPage, mode, updateRentPeriodsPageUserAnswers, shouldGoToRentPeriodsPageCheckMode(userAnswers, updatedAnswers)))
        )
    }
