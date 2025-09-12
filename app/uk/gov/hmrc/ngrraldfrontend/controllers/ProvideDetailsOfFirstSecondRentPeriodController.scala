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
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstSecondRentPeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstSecondRentPeriodForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfFirstSecondRentPeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProvideDetailsOfFirstSecondRentPeriodController @Inject()(view: ProvideDetailsOfFirstSecondRentPeriodView,
                                                                authenticate: AuthRetrievals,
                                                                inputText: InputText,
                                                                hasLinkedProperties: PropertyLinkingAction,
                                                                raldRepo: RaldRepo,
                                                                mcc: MessagesControllerComponents
                                                               )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder {

  def firstDateStartInput()(implicit messages: Messages): DateInput = DateInput(
    id = "first.startDate",
    namePrefix = Some("first.startDate"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.start.date.label")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("provideDetailsOfFirstSecondRentPeriod.firstPeriod.start.date.hint"),
      content = Text(messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.start.date.hint"))
    ))
  )

  def firstDateEndInput()(implicit messages: Messages): DateInput = DateInput(
    id = "first.endDate",
    namePrefix = Some("first.endDate"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.end.date.label")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("provideDetailsOfFirstSecondRentPeriod.firstPeriod.end.date.hint"),
      content = Text(messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.end.date.hint"))
    ))
  )

  private def firstRentPeriodYesButton(form: Form[ProvideDetailsOfFirstSecondRentPeriodForm])(implicit messages: Messages): NGRRadioButtons = NGRRadioButtons(
    radioContent = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.yes",
    radioValue = yesPayedRent,
    conditionalHtml = Some(inputText(
      form = form,
      id = "RentPeriodAmount",
      name = "RentPeriodAmount",
      label = messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.conditional.hint.bold"),
      isVisible = true,
      hint = Some(Hint(
        content = Text(messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.conditional.hint"))
      )),
      classes = Some("govuk-input--width-10"),
      prefix = Some(PrefixOrSuffix(content = Text("£")))
    ))
  )

  private def firstRentPeriodRadio(form: Form[ProvideDetailsOfFirstSecondRentPeriodForm])(implicit messages: Messages): NGRRadio = {
    val ngrRadioButtons: Seq[NGRRadioButtons] = Seq(
      firstRentPeriodYesButton(form),
      NGRRadioButtons(radioContent = "provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.no", radioValue = noRentPayed),
    )
    NGRRadio(
      NGRRadioName("provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio"),
      ngrTitle = Some(Legend(content = Text(messages("provideDetailsOfFirstSecondRentPeriod.firstPeriod.radio.label")), classes = "govuk-fieldset__legend--m", isPageHeading = true)),
      NGRRadioButtons = ngrRadioButtons
    )
  }

  def secondDateStartInput()(implicit messages: Messages): DateInput = DateInput(
    id = "second.startDate",
    namePrefix = Some("second.startDate"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("provideDetailsOfFirstSecondRentPeriod.secondPeriod.start.date.label")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("provideDetailsOfFirstSecondRentPeriod.secondPeriod.start.date.hint"),
      content = Text(messages("provideDetailsOfFirstSecondRentPeriod.secondPeriod.start.date.hint"))
    ))
  )

  def secondDateEndInput()(implicit messages: Messages): DateInput = DateInput(
    id = "second.endDate",
    namePrefix = Some("second.endDate"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("provideDetailsOfFirstSecondRentPeriod.secondPeriod.end.date.label")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("provideDetailsOfFirstSecondRentPeriod.secondPeriod.end.date.hint"),
      content = Text(messages("provideDetailsOfFirstSecondRentPeriod.secondPeriod.end.date.hint"))
    ))
  )

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(view(
          selectedPropertyAddress = property.addressFull,
          form,
          firstDateStartInput(),
          firstDateEndInput(),
          buildRadios(form, firstRentPeriodRadio(form)),
          secondDateStartInput(),
          secondDateEndInput()
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
              println(Console.RED + "************** " + formWithErrors.errors + Console.RESET)
              (formError.key, formError.messages) match
                case (key, messages) if messages.head.contains("provideDetailsOfFirstSecondRentPeriod.first.startDate") =>
                  setCorrectKey(formError, "provideDetailsOfFirstSecondRentPeriod", "first.startDate")
                case (key, messages) if messages.head.contains("provideDetailsOfFirstSecondRentPeriod.first.endDate") =>
                  setCorrectKey(formError, "provideDetailsOfFirstSecondRentPeriod", "first.endDate")
                case (key, messages) if messages.head.contains("provideDetailsOfFirstSecondRentPeriod.second.startDate") =>
                  setCorrectKey(formError, "provideDetailsOfFirstSecondRentPeriod", "second.startDate")
                case (key, messages) if messages.head.contains("provideDetailsOfFirstSecondRentPeriod.second.endDate") =>
                  setCorrectKey(formError, "provideDetailsOfFirstSecondRentPeriod", "second.endDate")
                case ("", messages) =>
                  formError.copy(key = "RentPeriodAmount")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)

            request.propertyLinking.map(property =>
              Future.successful(BadRequest(view(
                selectedPropertyAddress = property.addressFull,
                formWithCorrectedErrors,
                firstDateStartInput(),
                firstDateEndInput(),
                buildRadios(formWithErrors, firstRentPeriodRadio(formWithCorrectedErrors)),
                secondDateStartInput(),
                secondDateEndInput()
              )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo")),
          provideDetailsOfFirstSecondRentPeriodForm =>
            raldRepo.insertProvideDetailsOfFirstSecondRentPeriod(
              CredId(request.credId.getOrElse("")),
              provideDetailsOfFirstSecondRentPeriodForm.firstDateStartInput.makeString,
              provideDetailsOfFirstSecondRentPeriodForm.firstDateEndInput.makeString,
              provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodRadio,
              provideDetailsOfFirstSecondRentPeriodForm.firstRentPeriodAmount,
              provideDetailsOfFirstSecondRentPeriodForm.secondDateStartInput.makeString,
              provideDetailsOfFirstSecondRentPeriodForm.secondDateEndInput.makeString,
              provideDetailsOfFirstSecondRentPeriodForm.secondHowMuchIsRent,
            )
            Future.successful(Redirect(routes.RentPeriodsController.show.url))
        )
    }
  }
}
