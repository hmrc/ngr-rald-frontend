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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewForm.{answerToForm, form, formToAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, RentReview, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.RentReviewPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.RentReviewView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputDateForMonthYear
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RentReviewController @Inject()(rentReviewView: RentReviewView,
                                     authenticate : AuthRetrievals,
                                     getData: DataRetrievalAction,
                                     navigator: Navigator,
                                     sessionRepository: SessionRepository,
                                     inputDateForMonthYear: InputDateForMonthYear,
                                     mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(RentReviewPage) match {
        case None => form
        case Some(value) => answerToForm(value)
      }
        Future.successful(Ok(rentReviewView(
          form = preparedForm,
          hasIncludedRentReviewRadios = buildRadios(preparedForm, RentReviewForm.createHasIncludeRentReviewRadio(preparedForm, inputDateForMonthYear)),
          canRentGoDownRadios = buildRadios(preparedForm, RentReviewForm.createCanRentGoDownRadio),
          propertyAddress = request.property.addressFull,
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(rentReviewView(
            form = formWithErrors,
            hasIncludedRentReviewRadios = buildRadios(formWithErrors, RentReviewForm.createHasIncludeRentReviewRadio(formWithErrors, inputDateForMonthYear)),
            canRentGoDownRadios = buildRadios(formWithErrors, RentReviewForm.createCanRentGoDownRadio),
            propertyAddress = request.property.addressFull,
            mode = mode
          )))
        },
        rentReviewForm =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
              .set(RentReviewPage, formToAnswers(rentReviewForm))
            )
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RentReviewPage, mode, updatedAnswers))
      )
    }

}