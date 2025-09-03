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
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeStartForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentDatesAgreeStartForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeStartView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class RentDatesAgreeStartController @Inject()(view: RentDatesAgreeStartView,
                                              authenticate: AuthRetrievals,
                                              hasLinkedProperties: PropertyLinkingAction,
                                              raldRepo: RaldRepo,
                                              mcc: MessagesControllerComponents
                                             )(implicit appConfig: AppConfig, ec: ExecutionContext) extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(view(form, property.addressFull)))
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
                case (key, messages) if messages.contains("rentDatesAgreeStart.agreedDate.day.required.error") =>
                  formError.copy(key = "agreedDate.day")
                case (key, messages) if messages.contains("rentDatesAgreeStart.agreedDate.month.required.error") =>
                  formError.copy(key = "agreedDate.month")
                case (key, messages) if messages.contains("rentDatesAgreeStart.agreedDate.year.required.error") =>
                  formError.copy(key = "agreedDate.year")
                case (key, messages) if messages.contains("rentDatesAgreeStart.startPayingDate.day.required.error") =>
                  formError.copy(key = "startPayingDate.day")
                case (key, messages) if messages.contains("rentDatesAgreeStart.startPayingDate.month.required.error") =>
                  formError.copy(key = "startPayingDate.month")
                case (key, messages) if messages.contains("rentDatesAgreeStart.startPayingDate.year.required.error") =>
                  formError.copy(key = "startPayingDate.year")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
            request.propertyLinking.map(property =>
                Future.successful(BadRequest(view(formWithCorrectedErrors, property.addressFull))))
              .getOrElse(throw new NotFoundException("Couldn't find property in mongo")),
          rentDatesAgreeStartForm =>
            raldRepo.insertRentAgreeStartDates(
              CredId(request.credId.getOrElse("")),
              rentDatesAgreeStartForm.agreedDate.makeString,
              rentDatesAgreeStartForm.startPayingDate.makeString
            )
            Future.successful(Redirect(routes.WhatYourRentIncludesController.show.url))
        )
    }
  }
}
