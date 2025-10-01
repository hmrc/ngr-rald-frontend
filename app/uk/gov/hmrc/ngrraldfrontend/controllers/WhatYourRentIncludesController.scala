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

import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, UserAnswers, WhatYourRentIncludes}
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatYourRentIncludesForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatYourRentIncludesForm.form
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatYourRentIncludesView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.WhatYourRentIncludesPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatYourRentIncludesController @Inject()(whatYourRentIncludesView: WhatYourRentIncludesView,
                                               authenticate: AuthRetrievals,
                                               inputText: InputText, 
                                               getData: DataRetrievalAction,
                                               sessionRepository: SessionRepository,
                                               navigator: Navigator, 
                                               mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(WhatYourRentIncludesPage) match {
      case Some(value) => form.fill(WhatYourRentIncludesForm(
        livingAccommodationRadio = if (value.livingAccommodation) {
          "livingAccommodationYes"
        } else {
          "livingAccommodationNo"
        },
        rentPartAddressRadio = if (value.rentPartAddress) {
          "rentPartAddressYes"
        } else {
          "rentPartAddressNo"
        },
        rentEmptyShellRadio = if (value.rentEmptyShell) {
          "rentEmptyShellYes"
        } else {
          "rentEmptyShellNo"
        }, 
        rentIncBusinessRatesRadio = if (value.rentIncBusinessRates) {
          "rentIncBusinessRatesYes"
        } else {
          "rentIncBusinessRatesNo"
        }, 
        rentIncWaterChargesRadio = if (value.rentIncWaterCharges) {
          "rentIncWaterChargesYes"
        } else {
          "rentIncWaterChargesNo"
        }, 
        rentIncServiceRadio = if (value.rentIncService) {
          "rentIncServiceYes"
        } else {
          "rentIncServiceNo"
        }, 
        bedroomNumbers = value.bedroomNumbers.map(_.toString)
      ))
      case None => form
    }
        Future.successful(Ok(whatYourRentIncludesView(
          form = preparedForm,
          radios1 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio1(preparedForm, inputText)),
          radios2 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio2),
          radios3 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio3),
          radios4 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio4),
          radios5 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio5),
          radios6 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio6),
          propertyAddress = request.property.addressFull,
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val correctedFormErrors = formWithErrors.errors.map { formError =>
            (formError.key, formError.messages) match
              case ("", messages) =>
                formError.copy(key = "bedroomNumbers")
              case _ => formError
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
            Future.successful(BadRequest(whatYourRentIncludesView(
              form = formWithCorrectedErrors,
              radios1 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio1(formWithCorrectedErrors, inputText)),
              radios2 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio2),
              radios3 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio3),
              radios4 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio4),
              radios5 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio5),
              radios6 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio6),
              propertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        whatYourRentIncludesForm =>
          val answers: WhatYourRentIncludes = WhatYourRentIncludes(
            livingAccommodation = whatYourRentIncludesForm.livingAccommodationRadio match {
              case "livingAccommodationYes" => true
              case _ => false
            }, 
            rentPartAddress = whatYourRentIncludesForm.rentPartAddressRadio match {
              case "Yes" => true
              case _ => false
            }, 
            rentEmptyShell = whatYourRentIncludesForm.rentEmptyShellRadio match {
              case "Yes" => true
              case _ => false
            }, 
            rentIncBusinessRates = whatYourRentIncludesForm.rentIncBusinessRatesRadio match {
              case "Yes" => true
              case _ => false
            }, 
            rentIncWaterCharges = whatYourRentIncludesForm.rentIncWaterChargesRadio match {
              case "Yes" => true
              case _ => false
            }, 
            rentIncService = whatYourRentIncludesForm.rentIncServiceRadio match {
              case "Yes" => true
              case _ => false
            }, 
            bedroomNumbers = whatYourRentIncludesForm.bedroomNumbers match {
              case Some(value) if(whatYourRentIncludesForm.livingAccommodationRadio == "livingAccommodationYes") => Some(value.toInt)
              case _ => None
            }
          )
            for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId))
              .set(WhatYourRentIncludesPage, answers))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatYourRentIncludesPage, mode, updatedAnswers))
      )
    }
}
