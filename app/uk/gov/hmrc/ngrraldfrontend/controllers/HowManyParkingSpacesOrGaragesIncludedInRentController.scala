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

import play.api.data.{Form, FormError, Forms}
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowManyParkingSpacesOrGaragesIncludedInRentForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowManyParkingSpacesOrGaragesIncludedInRentForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.HowManyParkingSpacesOrGaragesIncludedInRentView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowManyParkingSpacesOrGaragesIncludedInRentController @Inject()(howManyParkingSpacesOrGaragesIncludedInRentView: HowManyParkingSpacesOrGaragesIncludedInRentView,
                                                                      authenticate: AuthRetrievals,
                                                                      inputText: InputText,
                                                                      hasLinkedProperties: PropertyLinkingAction,
                                                                      raldRepo: RaldRepo,
                                                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def generateInputText(form: Form[HowManyParkingSpacesOrGaragesIncludedInRentForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    inputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"howManyParkingSpacesOrGaragesIncludedInRent.$inputFieldName.label"),
      headingMessageArgs = Seq("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isPageHeading = true,
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
    )
  }
  
  
  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(howManyParkingSpacesOrGaragesIncludedInRentView(
          form = form,
          propertyAddress = property.addressFull,
          uncoveredSpaces = generateInputText(form, "uncoveredSpaces"),
          coveredSpaces = generateInputText(form, "coveredSpaces"),
          garages = generateInputText(form, "garages"),
        )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {

          val uncoveredSpaces = FormError(key = "uncoveredSpaces", message = "howManyParkingSpacesOrGaragesIncludedInRent.error.required")
          val coveredSpaces = FormError(key = "coveredSpaces", message = "howManyParkingSpacesOrGaragesIncludedInRent.error.required")
          val garages = FormError(key = "garages", message = "howManyParkingSpacesOrGaragesIncludedInRent.error.required")

          val validationCheck = formWithErrors.errors.head match {
            case value if value.key.isEmpty && value.messages.contains("howManyParkingSpacesOrGaragesIncludedInRent.error.required") => formWithErrors.copy(errors = Seq(uncoveredSpaces, coveredSpaces, garages))
            case _ => formWithErrors
          }

          val formWithCorrectedErrors = validationCheck
          request.propertyLinking.map(property =>
            Future.successful(BadRequest(howManyParkingSpacesOrGaragesIncludedInRentView(
              form = formWithCorrectedErrors,
              propertyAddress = property.addressFull,
              uncoveredSpaces = generateInputText(formWithCorrectedErrors, "uncoveredSpaces"),
              coveredSpaces = generateInputText(formWithCorrectedErrors, "coveredSpaces"),
              garages = generateInputText(formWithCorrectedErrors, "garages")
            )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
        },
        rentAmount =>
          raldRepo.insertHowManyParkingSpacesOrGaragesIncludedInRent(
            credId = CredId(request.credId.getOrElse("")),
            uncoveredSpaces = rentAmount.uncoveredSpaces,
            coveredSpaces = rentAmount.coveredSpaces,
            garages = rentAmount.garages
          )
          Future.successful(Redirect(routes.CheckRentFreePeriodController.show.url))
      )
    }
}

