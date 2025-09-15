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

import play.api.data.{Form, FormError}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import play.twirl.api.HtmlFormat
import play.api.i18n.{I18nSupport, Messages}
import uk.gov.hmrc.govukfrontend.views.Aliases.{Fieldset, Hint, PrefixOrSuffix, Text}
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.{DateInput, InputItem}
import uk.gov.hmrc.govukfrontend.views.viewmodels.fieldset.{Fieldset, Legend}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.views.html.InterimRentSetByTheCourtView
import uk.gov.hmrc.ngrraldfrontend.models.forms.InterimRentSetByTheCourtForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.InterimRentSetByTheCourtForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId

import uk.gov.hmrc.ngrraldfrontend.views.html.components.{DateTextFields, InputText}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class InterimRentSetByTheCourtController @Inject()(interimRentSetByTheCourtView: InterimRentSetByTheCourtView,
                                                   authenticate: AuthRetrievals,
                                                   inputText: InputText,
                                                   hasLinkedProperties: PropertyLinkingAction,
                                                   raldRepo: RaldRepo,
                                                   mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {
  
  def generateDateInput(implicit messages: Messages): DateInput = DateInput(
    id = "interimRentSetByTheCourt.date",
    namePrefix = Some("interimRentSetByTheCourt.date"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("interimRentSetByTheCourt.label.2")),
        classes = "govuk-fieldset__legend--m",
        isPageHeading = true
      ))
    )),
    items = Seq(
      InputItem(
        id = "interimRentSetByTheCourt.Month",
        name = "interimRentSetByTheCourt.Month",
        value = form("interimRentSetByTheCourt.month").value,
        label = Some("interimRentSetByTheCourt.inputField.month"),
        classes = s"govuk-input--width-2".trim
      ),
      InputItem(
        id = "interimRentSetByTheCourt.Year",
        name = "interimRentSetByTheCourt.Year",
        label = Some("interimRentSetByTheCourt.inputField.year"),
        value = form("interimRentSetByTheCourt.year").value,
        classes = s"govuk-input--width-2".trim
      )
    ),
    hint = Some(Hint(
      id = Some("interimRentSetByTheCourt.hint.2"),
      content = Text(messages("interimRentSetByTheCourt.hint.2"))
    ))
  )

  def generateInputText(form: Form[InterimRentSetByTheCourtForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    inputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"interimRentSetByTheCourt.$inputFieldName.label"),
      headingMessageArgs = Seq("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isPageHeading = true,
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
      prefix = Some(PrefixOrSuffix(content = Text("£")))
    )
  }

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(interimRentSetByTheCourtView(
          form = form,
          propertyAddress = property.addressFull,
          howMuch = generateInputText(form, "howMuch"),
          dateInput = generateDateInput()
        )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          request.propertyLinking.map(property =>
            Future.successful(BadRequest(interimRentSetByTheCourtView(
              form = formWithErrors,
              propertyAddress = property.addressFull,
              howMuch = generateInputText(formWithErrors, "howMuch"),
              dateInput = generateDateInput()
            )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
        },
        interimRent =>
          raldRepo.insertInterimRentSetByTheCourt(
            credId = CredId(request.credId.getOrElse("")),
            amount = interimRent.amount,
            date = interimRent.date.makeString
          )
          Future.successful(Redirect(routes.CheckRentFreePeriodController.show.url))
      )
    }
  }
