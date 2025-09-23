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
import uk.gov.hmrc.govukfrontend.views.Aliases.{Legend, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.Fieldset
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, AgreementVerbal, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm.form
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.AgreementVerbalPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreementVerbalView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AgreementVerbalController @Inject()(view: AgreementVerbalView,
                                          authenticate: AuthRetrievals,
                                          dateTextFields: DateTextFields,
                                          mcc: MessagesControllerComponents,
                                          getData: DataRetrievalAction,
                                          sessionRepository: SessionRepository,
                                          navigator: Navigator
                                         )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  val yesButton: NGRRadioButtons = NGRRadioButtons("agreementVerbal.yes", Yes)

  private def noButton(form: Form[AgreementVerbalForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "agreementVerbal.no",
    radioValue = No,
    conditionalHtml = Some(dateTextFields(form, DateInput(id = "agreementEndDate",
      fieldset = Some(Fieldset(legend = Some(Legend(content = Text(messages("agreementVerbal.endDate.title")), classes = "govuk-fieldset__legend--s")))),
      hint = Some(Hint(content = Text(messages("agreementVerbal.endDate.hint")))))))
  )

  def ngrRadio(form: Form[AgreementVerbalForm])(implicit messages: Messages): NGRRadio =
    NGRRadio(NGRRadioName("agreement-verbal-radio"), Seq(yesButton, noButton(form)),
      Some(Legend(content = Text(messages("agreementVerbal.radio.title")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      Some("agreementVerbal.radio.hint"))

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(AgreementVerbalPage) match {
        case None => form
        case Some(value) => form.fill(AgreementVerbalForm(if (value.openEnded) {
          "Yes"
        } else {
          "No"
        },NGRDate.fromString(value.startDate), value.endDate match {
          case Some(value) => Some(NGRDate.fromString(value))
          case None => None
        }))
      }
        Future.successful(Ok(view(preparedForm, buildRadios(preparedForm, ngrRadio(preparedForm)), request.property.addressFull, mode)))
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
                case (key, messages) if messages.contains("agreementVerbal.startDate.day.required.error") =>
                  formError.copy(key = "agreementStartDate.day")
                case (key, messages) if messages.contains("agreementVerbal.startDate.month.required.error") =>
                  formError.copy(key = "agreementStartDate.month")
                case (key, messages) if messages.contains("agreementVerbal.startDate.year.required.error") =>
                  formError.copy(key = "agreementStartDate.year")
                case ("", messages) if messages.contains("agreementVerbal.endDate.day.required.error") =>
                  formError.copy(key = "agreementEndDate.day")
                case ("", messages) if messages.contains("agreementVerbal.endDate.month.required.error") =>
                  formError.copy(key = "agreementEndDate.month")
                case ("", messages) if messages.contains("agreementVerbal.endDate.year.required.error") =>
                  formError.copy(key = "agreementEndDate.year")
                case ("", messages) =>
                  formError.copy(key = "agreementEndDate")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
                Future.successful(BadRequest(view(formWithCorrectedErrors,
                  buildRadios(formWithErrors, ngrRadio(formWithCorrectedErrors)), request.property.addressFull, mode))),
          agreementVerbalForm =>
            val openEnded: Boolean = agreementVerbalForm.radioValue.equals("Yes")
            val answers: AgreementVerbal = AgreementVerbal(
              agreementVerbalForm.agreementStartDate.makeString,
              openEnded,
              if (openEnded) None else agreementVerbalForm.agreementEndDate.map(_.makeString))

            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(AgreementVerbalPage, answers))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(AgreementVerbalPage, mode, updatedAnswers))
        )
    }
  }
}
