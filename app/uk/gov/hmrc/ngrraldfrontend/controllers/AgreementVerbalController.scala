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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementVerbalForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreementVerbalView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgreementVerbalController @Inject()(view: AgreementVerbalView,
                                          authenticate: AuthRetrievals,
                                          hasLinkedProperties: PropertyLinkingAction,
                                          raldRepo: RaldRepo,
                                          dateTextFields: DateTextFields,
                                          mcc: MessagesControllerComponents
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

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(view(createDefaultNavBar, form, buildRadios(form, ngrRadio(form)), property.addressFull)))
      ).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
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

            request.propertyLinking.map(property =>
                Future.successful(BadRequest(view(createDefaultNavBar, formWithCorrectedErrors,
                  buildRadios(formWithErrors, ngrRadio(formWithCorrectedErrors)), property.addressFull))))
              .getOrElse(throw new NotFoundException("Couldn't find property in mongo")),
          agreementVerbalForm =>
            val openEnded: Boolean = agreementVerbalForm.radioValue.equals("Yes")
                        raldRepo.insertAgreementVerbal(
                          CredId(request.credId.getOrElse("")),
                          agreementVerbalForm.agreementStartDate.makeString,
                          openEnded,
                          if (openEnded) None else agreementVerbalForm.agreementEndDate.map(_.makeString)
                        )
            Future.successful(Redirect(routes.HowMuchIsTotalAnnualRentController.show.url))
        )
    }
  }
}
