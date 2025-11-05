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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.html.components.GovukRadios
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewDetailsForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewDetailsForm.{answerToForm, form, formToAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, RentReviewDetails, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.RentReviewDetailsPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.RentReviewDetailsView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RentReviewDetailsController @Inject()(rentReviewDetailsView: RentReviewDetailsView,
                                            authenticate: AuthRetrievals,
                                            getData: DataRetrievalAction,
                                            govukRadios: GovukRadios,
                                            navigator: Navigator,
                                            sessionRepository: SessionRepository,
                                            mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(RentReviewDetailsPage) match {
        case None => form
        case Some(value) => answerToForm(value)
      }
      Future.successful(Ok(rentReviewDetailsView(
        form = preparedForm,
        whatHappensAtRentReviewRadios = buildRadios(preparedForm, RentReviewDetailsForm.createWhatHappensAtRentReviewRadio),
        hasAgreedNewRentRadios = buildRadios(preparedForm, RentReviewDetailsForm.createHasAgreedNewRentRadio(preparedForm, govukRadios)),
        propertyAddress = request.property.addressFull,
        mode = mode
      )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val correctedFormErrors = formWithErrors.errors.map { formError =>
            (formError.key, formError.messages) match
              case (key, messages) if messages.head.contains("rentReviewDetails.startDate") =>
                setCorrectKey(formError, "rentReviewDetails", "startDate")
              case _ =>
                formError
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
          Future.successful(BadRequest(rentReviewDetailsView(
            form = formWithCorrectedErrors,
            whatHappensAtRentReviewRadios = buildRadios(formWithCorrectedErrors, RentReviewDetailsForm.createWhatHappensAtRentReviewRadio),
            hasAgreedNewRentRadios = buildRadios(formWithCorrectedErrors, RentReviewDetailsForm.createHasAgreedNewRentRadio(formWithCorrectedErrors, govukRadios)),
            propertyAddress = request.property.addressFull,
            mode = mode
          )))
        },
        rentReviewDetailsForm =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
              .set(RentReviewDetailsPage, formToAnswers(rentReviewDetailsForm))
            )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RentReviewDetailsPage, mode, updatedAnswers))
      )
    }
}