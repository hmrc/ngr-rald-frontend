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


import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.Aliases.{Label, Text}
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRCharacterCount
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, simpleNgrRadio}
import uk.gov.hmrc.ngrraldfrontend.models.forms.HasAnythingElseAffectedTheRentForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.HasAnythingElseAffectedTheRentForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{HasAnythingElseAffectedTheRent, Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.HasAnythingElseAffectedTheRentPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.HasAnythingElseAffectedTheRentView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HasAnythingElseAffectedTheRentController @Inject()(hasAnythingElseAffectedTheRentView: HasAnythingElseAffectedTheRentView,
                                                        authenticate : AuthRetrievals,
                                                        ngrCharacterCountComponent: NGRCharacterCountComponent,
                                                        getData: DataRetrievalAction,
                                                        navigator: Navigator,
                                                        sessionRepository: SessionRepository,
                                                        mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {


  private def reasonConditionalHtml(form: Form[HasAnythingElseAffectedTheRentForm])(implicit messages: Messages): Option[Html] =
    Some(ngrCharacterCountComponent(form,
      NGRCharacterCount(
        id = HasAnythingElseAffectedTheRentForm.reasonInput,
        name = HasAnythingElseAffectedTheRentForm.reasonInput,
        maxLength = Some(250),
        label = Label(
          classes = "govuk-label govuk-label--s",
          content = Text(Messages("hasAnythingElseAffectedTheRent.reason.label"))
        )
      )))

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(HasAnythingElseAffectedTheRentPage) match {
        case None => form
        case Some(value) => form.fill(HasAnythingElseAffectedTheRentForm(value.radio.toString, value.reason))
      }
      Future.successful(Ok(hasAnythingElseAffectedTheRentView(
        form = preparedForm,
        radios = buildRadios(preparedForm, simpleNgrRadio(HasAnythingElseAffectedTheRentForm.hasAnythingElseAffectedTheRentRadio,  yesConditionalHtml = reasonConditionalHtml(preparedForm))),
        propertyAddress = request.property.addressFull,
        mode = mode
      )))
    }
  }
  
  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          Future.successful(BadRequest(hasAnythingElseAffectedTheRentView(
            form = formWithErrors,
            radios = buildRadios(formWithErrors, simpleNgrRadio(HasAnythingElseAffectedTheRentForm.hasAnythingElseAffectedTheRentRadio,  yesConditionalHtml = reasonConditionalHtml(formWithErrors))),
            propertyAddress = request.property.addressFull,
            mode = mode
          )))
        },
        radioValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
              .set(HasAnythingElseAffectedTheRentPage, HasAnythingElseAffectedTheRent(radioValue.radioValue.toBoolean, radioValue.reason)))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(HasAnythingElseAffectedTheRentPage, mode, updatedAnswers))
      )
    }
}
