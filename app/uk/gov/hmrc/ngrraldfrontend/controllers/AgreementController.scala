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
import uk.gov.hmrc.govukfrontend.views.html.components.ErrorMessage
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreementForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreementView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.{DateTextFields, NGRCharacterCountComponent}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class AgreementController @Inject()(view: AgreementView,
                                    authenticate: AuthRetrievals,
                                    dateTextFields: DateTextFields,
                                    hasLinkedProperties: PropertyLinkingAction,
                                    raldRepo: RaldRepo,
                                    ngrCharacterCountComponent: NGRCharacterCountComponent,
                                    mcc: MessagesControllerComponents
                                   )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def dateInput()(implicit messages: Messages) : DateInput = DateInput(
    id = "agreementStartDate",
    namePrefix = Some("agreementStartDate"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("agreement.subheading.1")),
        classes = "govuk-fieldset__legend--m",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("agreement.start.date.hint"),
      content = Text(messages("agreement.radio.conditional.hint.1"))
    ))
  )

  def openEndedNoButton(form: Form[AgreementForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "agreement.radio.2",
    radioValue = NoOpenEnded,
    conditionalHtml = Some(dateTextFields(form, DateInput(
      id = "agreementEndDate",
      namePrefix = Some(""),
      fieldset = Some(Fieldset(
        legend = Some(Legend(
          content = Text(messages("agreement.radio.conditional.subheading.1")),
          classes = "govuk-fieldset__legend--s",
          isPageHeading = true
        ))
      )),
      hint = Some(Hint(
        content = Text(messages("agreement.radio.conditional.hint.1"))
      ))
    )))
  )

  def openEndedRadio(form: Form[AgreementForm])(implicit messages: Messages): NGRRadio = {
    val ngrRadioButtons: Seq[NGRRadioButtons] = Seq(
      NGRRadioButtons(radioContent = "agreement.radio.1", radioValue = YesOpenEnded),
      openEndedNoButton(form)
    )
    NGRRadio(
      NGRRadioName("agreement-radio-openEnded"),
      ngrTitle = Some(Legend(content = Text(messages("agreement.subheading.2")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      hint = Some(messages("agreement.hint.2")),
      NGRRadioButtons = ngrRadioButtons
    )
  }

  def breakClauseYesButton(form: Form[AgreementForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "service.yes",
    radioValue = YesBreakClause,
    conditionalHtml = Some(ngrCharacterCountComponent(form,
      NGRCharacterCount(
        id = "about-break-clause",
        name = "about-break-clause",
        maxLength = Some(250),
        label = Label(
          classes = "govuk-label govuk-label--s",
          content = Text(Messages("agreement.radio.conditional.subheading.2"))
        ),
        hint = Some(
          Hint(
            id =  Some("agreement-breakClause-hint"),
            classes = "",
            attributes = Map.empty,
            content = Text(messages("agreement.radio.conditional.hint.2"))
          )
        )
      )))
  )

  def breakClauseRadio(form: Form[AgreementForm])(implicit messages: Messages): NGRRadio = {
    val ngrRadioButtons: Seq[NGRRadioButtons] = Seq(
      breakClauseYesButton(form),
      NGRRadioButtons(radioContent = "service.no", radioValue = NoBreakClause)
    )
    NGRRadio(
      NGRRadioName("agreement-breakClause-radio"),
      ngrTitle = Some(Legend(content = Text(messages("agreement.subheading.3")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      hint = Some(messages("agreement.hint.3")),
      NGRRadioButtons =  ngrRadioButtons
    )
  }


  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(view(
          navigationBarContent = createDefaultNavBar,
          selectedPropertyAddress = property.addressFull,
          form,
          dateInput(),
          buildRadios(form, openEndedRadio(form)),
          buildRadios(form, breakClauseRadio(form))
        )))
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

            request.propertyLinking.map(property =>
              Future.successful(BadRequest(view(
                createDefaultNavBar,
                selectedPropertyAddress = property.addressFull,
                formWithCorrectedErrors,
                dateInput(),
                buildRadios(formWithErrors, openEndedRadio(formWithCorrectedErrors)),
                buildRadios(formWithErrors, breakClauseRadio(formWithCorrectedErrors))
              )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo")),
          agreementForm =>
            raldRepo.insertAgreement(
              CredId(request.credId.getOrElse("")),
              agreementForm.agreementStart.makeString,
              agreementForm.openEndedRadio,
              agreementForm.openEndedDate.map(value => value.makeString),
              agreementForm.breakClauseRadio,
              agreementForm.breakClauseInfo,
            )
            Future.successful(Redirect(routes.WhatIsYourRentBasedOnController.show.url))
        )
    }
  }
}
