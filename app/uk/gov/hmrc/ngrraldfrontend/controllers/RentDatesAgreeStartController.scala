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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeStartForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeStartForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, NGRDate, RentDatesAgreeStart, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.RentDatesAgreeStartPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeStartView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RentDatesAgreeStartController @Inject()(view: RentDatesAgreeStartView,
                                              authenticate: AuthRetrievals,
                                              getData: DataRetrievalAction,
                                              checkRequestSentReference: CheckRequestSentReferenceAction,
                                              sessionRepository: SessionRepository,
                                              navigator: Navigator,
                                              mcc: MessagesControllerComponents
                                             )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val preparedFrom = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(RentDatesAgreeStartPage) match {
        case None => form
        case Some(value) => form.fill(RentDatesAgreeStartForm(NGRDate.fromString(value.agreedDate), NGRDate.fromString(value.startPayingDate)))
      }
      Future.successful(Ok(view(preparedFrom, request.property.addressFull, mode)))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            val correctedFormErrors = formWithErrors.errors.map { formError =>
              (formError.key, formError.messages) match
                case (key, messages) if messages.head.contains("rentDatesAgreeStart.agreedDate") =>
                  setCorrectKey(formError, "rentDatesAgreeStart", "agreedDate")
                case (key, messages) if messages.head.contains("rentDatesAgreeStart.startPayingDate") =>
                  setCorrectKey(formError, "rentDatesAgreeStart", "startPayingDate")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
            Future.successful(BadRequest(view(formWithCorrectedErrors, request.property.addressFull, mode))),
          rentDatesAgreeStartForm =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).set(RentDatesAgreeStartPage, RentDatesAgreeStart(rentDatesAgreeStartForm.agreedDate.makeString, rentDatesAgreeStartForm.startPayingDate.makeString)))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(RentDatesAgreeStartPage, mode, updatedAnswers)))
    }
  }
}
