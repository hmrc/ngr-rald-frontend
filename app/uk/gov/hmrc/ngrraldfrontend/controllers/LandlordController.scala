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
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.{Landlord, Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.LandlordForm.form
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
                                   getData : DataRetrievalAction,
                                   sessionRepository: SessionRepository,
                                   navigator: Navigator
                                  )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def landlordRelationship(form: Form[LandlordForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "service.yes",
    radioValue = LandlordRelationshipYes,
    conditionalHtml = Some(ngrCharacterCountComponent(form,
      NGRCharacterCount(
        id = "landlord-relationship",
        name = "landlord-relationship",
        maxLength = Some(250),
        label = Label(
          classes = "govuk-label govuk-label--s",
          content = Text(Messages("landlord.radio.yes"))
        ),
        hint = Some(
          Hint(
            id = Some("landlord-relationship-hint"),
            classes = "",
            attributes = Map.empty,
            content = Text(messages("landlord.radio.yes.hint"))
          )
      )))
  )
  )

  def ngrRadio(form: Form[LandlordForm])(implicit messages: Messages): NGRRadio =
    val ngrRadioButtons: Seq[NGRRadioButtons] = Seq(
      landlordRelationship(form),
      NGRRadioButtons(radioContent = "service.no", radioValue = LandlordRelationshipNo)
    )
    NGRRadio(
      NGRRadioName("landlord-radio"),
      ngrTitle = Some(Legend(content = Text(messages("landlord.p2")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      NGRRadioButtons = ngrRadioButtons
    )

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(LandlordPage) match {
        case None => form
        case Some(value) => form.fill(LandlordForm(
          value.landlordName,
          if(value.hasRelationship) {
            "LandlordRelationshipYes"
          } else {
            "LandlordRelationshipNo"
          },
          value.landlordRelationship))
      }
      Future.successful(Ok(view(selectedPropertyAddress = request.property.addressFull, form = preparedForm, ngrRadio =  buildRadios(preparedForm, ngrRadio(preparedForm)), mode))
      )

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
                case ("", messages) if messages.contains("landlord.radio.empty.error") =>
                  formError.copy(key = "landlord-relationship")
                case ("", messages) if messages.contains("landlord.radio.tooLong.error") =>
                  formError.copy(key = "landlord-relationship")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
              Future.successful(BadRequest(view(
                selectedPropertyAddress = request.property.addressFull,
                formWithCorrectedErrors,
                buildRadios(formWithErrors, ngrRadio(formWithCorrectedErrors)),
                mode
              ))),
          landlordForm =>
            val answers: Landlord = Landlord(
              landlordForm.landlordName,
              landlordForm.hasRelationship match {
                case "LandlordRelationshipYes" => true
                case _ => false
              },
              landlordForm.landlordRelationship
            )
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId))
                .set(LandlordPage, answers))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(LandlordPage, mode, updatedAnswers))
        )
    }
  }
}
