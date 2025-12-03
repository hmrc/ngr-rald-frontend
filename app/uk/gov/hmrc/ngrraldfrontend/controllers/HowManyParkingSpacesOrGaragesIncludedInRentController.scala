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
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.twirl.api.HtmlFormat
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowManyParkingSpacesOrGaragesIncludedInRentForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowManyParkingSpacesOrGaragesIncludedInRentForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{HowManyParkingSpacesOrGarages, Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.HowManyParkingSpacesOrGaragesIncludedInRentPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.HowManyParkingSpacesOrGaragesIncludedInRentView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HowManyParkingSpacesOrGaragesIncludedInRentController @Inject()(howManyParkingSpacesOrGaragesIncludedInRentView: HowManyParkingSpacesOrGaragesIncludedInRentView,
                                                                      authenticate: AuthRetrievals,
                                                                      inputText: InputText,
                                                                      getData: DataRetrievalAction,
                                                                      checkRequestSentReference: CheckRequestSentReferenceAction,
                                                                      sessionRepository: SessionRepository,
                                                                      navigator: Navigator,
                                                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def generateInputText(form: Form[HowManyParkingSpacesOrGaragesIncludedInRentForm], inputFieldName: String)(implicit messages: Messages): HtmlFormat.Appendable = {
    inputText(
      form = form,
      id = inputFieldName,
      name = inputFieldName,
      label = messages(s"howManyParkingSpacesOrGaragesIncludedInRent.$inputFieldName.label"),
      labelClasses = Some("govuk-label govuk-label--s "),
      headingMessageArgs = Seq("govuk-fieldset__legend govuk-fieldset__legend--s"),
      isVisible = true,
      classes = Some("govuk-input govuk-input--width-5"),
    )
  }
  
  
  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(HowManyParkingSpacesOrGaragesIncludedInRentPage) match {
        case None => form
        case Some(value) => form.fill(HowManyParkingSpacesOrGaragesIncludedInRentForm(
          value.uncoveredSpaces,
          value.coveredSpaces,
          value.garages
        ))
      }
        Future.successful(Ok(howManyParkingSpacesOrGaragesIncludedInRentView(
          form = preparedForm,
          propertyAddress = request.property.addressFull,
          uncoveredSpaces = generateInputText(preparedForm, "uncoveredSpaces"),
          coveredSpaces = generateInputText(preparedForm, "coveredSpaces"),
          garages = generateInputText(preparedForm, "garages"),
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val formWithCorrectedErrors = formWithErrors.errors.head match {
            case value if value.key.isEmpty &&
              value.messages.contains("howManyParkingSpacesOrGaragesIncludedInRent.error.required") =>
              val uncoveredSpaces = value.copy(key = "uncoveredSpaces")
              val coveredSpaces = value.copy(key = "coveredSpaces")
              val garages = value.copy(key = "garages")
              formWithErrors.copy(errors = Seq(uncoveredSpaces, coveredSpaces, garages))
            case _ => formWithErrors
          }
            Future.successful(BadRequest(howManyParkingSpacesOrGaragesIncludedInRentView(
              form = formWithCorrectedErrors,
              propertyAddress = request.property.addressFull,
              uncoveredSpaces = generateInputText(formWithCorrectedErrors, "uncoveredSpaces"),
              coveredSpaces = generateInputText(formWithCorrectedErrors, "coveredSpaces"),
              garages = generateInputText(formWithCorrectedErrors, "garages"),
              mode = mode
            )))
        },
        parkingSpacesOrGaragesIncluded =>
          val answers = HowManyParkingSpacesOrGarages(parkingSpacesOrGaragesIncluded.uncoveredSpaces, parkingSpacesOrGaragesIncluded.coveredSpaces, parkingSpacesOrGaragesIncluded.garages)
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).set(HowManyParkingSpacesOrGaragesIncludedInRentPage, answers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(HowManyParkingSpacesOrGaragesIncludedInRentPage, mode, updatedAnswers))
      )
    }
}

