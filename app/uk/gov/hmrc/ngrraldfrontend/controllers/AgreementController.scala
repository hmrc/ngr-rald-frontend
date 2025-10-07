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
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementForm.{answerToForm, form, formToAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.{Agreement, Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.AgreementPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreementView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.{DateTextFields, NGRCharacterCountComponent}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgreementController @Inject()(view: AgreementView,
                                    authenticate: AuthRetrievals,
                                    dateTextFields: DateTextFields,
                                    ngrCharacterCountComponent: NGRCharacterCountComponent,
                                    mcc: MessagesControllerComponents,
                                    getData: DataRetrievalAction,
                                    navigator: Navigator,
                                    sessionRepository: SessionRepository
                                   )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(AgreementPage) match {
        case None => form
        case Some(value) => answerToForm(value)
      }
      Future.successful(Ok(view(
        selectedPropertyAddress = request.property.addressFull,
        preparedForm,
        AgreementForm.dateInput(),
        buildRadios(preparedForm, AgreementForm.openEndedRadio(preparedForm, dateTextFields)),
        buildRadios(preparedForm, AgreementForm.breakClauseRadio(preparedForm, ngrCharacterCountComponent)),
        mode = mode
      )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            val correctedFormErrors = formWithErrors.errors.map { formError =>
              (formError.key, formError.messages) match
                case ("", messages) if messages.contains("agreement.radio.conditional.breakClause.required.error") =>
                  formError.copy(key = "about-break-clause")
                case ("", messages) if messages.contains("agreement.radio.conditional.breakClause.tooLong.error") =>
                  formError.copy(key = "about-break-clause")
                case ("", messages) =>
                  formError.copy(key = "agreementEndDate")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)

            Future.successful(BadRequest(view(
              selectedPropertyAddress = request.property.addressFull,
              formWithCorrectedErrors,
              AgreementForm.dateInput(),
              buildRadios(formWithErrors, AgreementForm.openEndedRadio(formWithCorrectedErrors, dateTextFields)),
              buildRadios(formWithErrors, AgreementForm.breakClauseRadio(formWithCorrectedErrors, ngrCharacterCountComponent)),
              mode = mode
            ))),
          agreementForm =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId))
                .set(AgreementPage, formToAnswers(agreementForm)))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AgreementPage, mode, updatedAnswers))
        )
    }
  }
}
