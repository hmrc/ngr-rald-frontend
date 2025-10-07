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
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.ParkingSpacesOrGaragesNotIncludedInYourRentForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.ParkingSpacesOrGaragesNotIncludedInYourRentForm.*
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, ParkingSpacesOrGaragesNotIncludedInYourRent, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.ParkingSpacesOrGaragesNotIncludedInYourRentPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.ParkingSpacesOrGaragesNotIncludedInYourRentView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.{InputText, NGRCharacterCountComponent}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ParkingSpacesOrGaragesNotIncludedInYourRentController @Inject()(view: ParkingSpacesOrGaragesNotIncludedInYourRentView,
                                                                      authenticate: AuthRetrievals,
                                                                      inputText: InputText,
                                                                      ngrCharacterCountComponent: NGRCharacterCountComponent,
                                                                      mcc: MessagesControllerComponents,
                                                                      getData : DataRetrievalAction,
                                                                      sessionRepository: SessionRepository,
                                                                      navigator: Navigator
                                                                     )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport with DateKeyFinder{

  def generateInputText(form: Form[ParkingSpacesOrGaragesNotIncludedInYourRentForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    inputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"parkingSpacesOrGaragesNotIncludedInYourRent.$inputFieldName.label"),
      headingMessageArgs = Seq("govuk-fieldset__legend--s"),
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
    )
  }

  def agreementDateInput()(implicit messages: Messages): DateInput = DateInput(
    id = "agreementDate",
    namePrefix = Some("parkingSpacesOrGaragesNotIncludedInYourRent"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.label")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = false
      ))
    )),
    hint = Some(Hint(
      id = Some("provideDetailsOfFirstSecondRentPeriod.secondPeriod.start.date.hint"),
      content = Text(messages("provideDetailsOfFirstSecondRentPeriod.secondPeriod.start.date.hint"))
    ))
  )
  
  def show(mode: Mode):Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(ParkingSpacesOrGaragesNotIncludedInYourRentPage) match {
        case None => form
        case Some(value) => form.fill(ParkingSpacesOrGaragesNotIncludedInYourRentForm(value.uncoveredSpaces, value.coveredSpaces, value.garages, value.totalCost, value.agreementDate))
      }
      Future.successful(Ok(view(
        form = preparedForm,
        propertyAddress = request.property.addressFull,
        uncoveredSpaces = generateInputText(preparedForm, "uncoveredSpaces"),
        coveredSpaces = generateInputText(preparedForm, "coveredSpaces"),
        garages = generateInputText(preparedForm, "garages"),
        agreementDate = agreementDateInput(),
        mode = mode
      )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val formWithCorrectedErrors = formWithErrors.errors.headOption match {
            case Some(value) if value.key.isEmpty &&
              value.messages.contains("parkingSpacesOrGaragesNotIncludedInYourRent.error.required") =>
              val uncoveredSpaces = value.copy(key = "uncoveredSpaces")
              val coveredSpaces = value.copy(key = "coveredSpaces")
              val garages = value.copy(key = "garages")
              formWithErrors.copy(errors = Seq(uncoveredSpaces, coveredSpaces, garages))
            case _ =>
              formWithErrors
          }
          Future.successful(BadRequest(view(
            form = formWithCorrectedErrors,
            propertyAddress = request.property.addressFull,
            uncoveredSpaces = generateInputText(formWithCorrectedErrors, "uncoveredSpaces"),
            coveredSpaces = generateInputText(formWithCorrectedErrors, "coveredSpaces"),
            garages = generateInputText(formWithCorrectedErrors, "garages"),
            agreementDate = agreementDateInput(),
            mode = mode
          )))
        },
        parkingSpacesOrGaragesNotIncludedInYourRent =>
          val answers = ParkingSpacesOrGaragesNotIncludedInYourRent(parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces, parkingSpacesOrGaragesNotIncludedInYourRent.coveredSpaces, parkingSpacesOrGaragesNotIncludedInYourRent.garages, parkingSpacesOrGaragesNotIncludedInYourRent.totalCost, parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate)
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, answers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(ParkingSpacesOrGaragesNotIncludedInYourRentPage, mode, updatedAnswers))
      )
    }

}
