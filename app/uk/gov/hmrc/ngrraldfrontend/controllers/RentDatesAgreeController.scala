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

import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.viewmodels.dateinput.DateInput
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


@Singleton
class RentDatesAgreeController @Inject()(rentDatesAgreeView: RentDatesAgreeView,
                                         authenticate: AuthRetrievals,
                                         hasLinkedProperties: PropertyLinkingAction,
                                         raldRepo: RaldRepo,
                                         mcc: MessagesControllerComponents
                                        )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def dateInput()(implicit messages: Messages): DateInput = DateInput(
    id = "rentDatesAgreeInput",
    namePrefix = Some("rentDatesAgreeInput"),
    fieldset = Some(Fieldset(
      legend = Some(Legend(
        content = Text(messages("rentDatesAgree.subheading")),
        classes = "govuk-fieldset__legend--s",
        isPageHeading = true
      ))
    )),
    hint = Some(Hint(
      id = Some("rentDatesAgree.hint"),
      content = Text(messages("rentDatesAgree.hint"))
    ))
  )

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(rentDatesAgreeView(
          form = form,
          dateInput = dateInput(),
          propertyAddress = property.addressFull,
        )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          val correctedFormErrors = formWithErrors.errors.map { formError =>
            (formError.key, formError.messages) match
              case (key, messages) if messages.contains("rentDatesAgree.date.month.required.error") =>
                formError.copy(key = "rentDatesAgreeInput.month")
              case (key, messages) if messages.contains("rentDatesAgree.date.month.year.required.error") =>
                formError.copy(key = "rentDatesAgreeInput.month")
              case (key, messages) if messages.contains("rentDatesAgree.date.year.required.error") =>
                formError.copy(key = "rentDatesAgreeInput.year")
              case _ =>
                formError.copy(key = "rentDatesAgreeInput.day")
          }
          val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
          request.propertyLinking.map(property =>
            Future.successful(BadRequest(rentDatesAgreeView(
              form = formWithCorrectedErrors,
              dateInput = dateInput(),
              propertyAddress = property.addressFull
            )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
        },
        dateValue =>
          raldRepo.insertRentDates(
            credId = CredId(request.credId.getOrElse("")),
            rentDates = dateValue.dateInput.makeString
          )
          Future.successful(Redirect(routes.WhatTypeOfLeaseRenewalController.show.url))
      )
    }
}