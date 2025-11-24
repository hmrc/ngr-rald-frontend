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
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm.{answerToForm, form, formToAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Landlord, Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.LandlordPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.LandlordView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class LandlordController @Inject()(view: LandlordView,
                                   authenticate: AuthRetrievals,
                                   ngrCharacterCountComponent: NGRCharacterCountComponent,
                                   mcc: MessagesControllerComponents,
                                   getData: DataRetrievalAction,
                                   checkRequestSentReference: CheckRequestSentReferenceAction,
                                   sessionRepository: SessionRepository,
                                   navigator: Navigator
                                  )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(LandlordPage) match {
        case None => form
        case Some(value) => answerToForm(value)
      }
      Future.successful(Ok(view(selectedPropertyAddress = request.property.addressFull,
        form = preparedForm,
        ngrRadio = buildRadios(preparedForm, LandlordForm.landlordRadio(preparedForm, ngrCharacterCountComponent)),
        mode
      )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(
              selectedPropertyAddress = request.property.addressFull,
              formWithErrors,
              buildRadios(formWithErrors, LandlordForm.landlordRadio(formWithErrors, ngrCharacterCountComponent)),
              mode
            ))),
          landlordForm =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
                .set(LandlordPage, formToAnswers(landlordForm)))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(LandlordPage, mode, updatedAnswers))
        )
    }
  }
}
