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
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.DoesYourRentIncludeParkingForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.DoesYourRentIncludeParkingForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.views.html.DoesYourRentIncludeParkingView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DoesYourRentIncludeParkingController  @Inject()(doesYourRentIncludeParkingView: DoesYourRentIncludeParkingView,
                                                      authenticate: AuthRetrievals,
                                                      hasLinkedProperties: PropertyLinkingAction,
                                                      raldRepo: RaldRepo,
                                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(doesYourRentIncludeParkingView(
          selectedPropertyAddress = property.addressFull,
          form = form,
          ngrRadio = buildRadios(form, DoesYourRentIncludeParkingForm.ngrRadio(form)),
        )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          request.propertyLinking.map(property =>
            Future.successful(BadRequest(doesYourRentIncludeParkingView(
              form = formWithErrors,
              ngrRadio = buildRadios(formWithErrors, DoesYourRentIncludeParkingForm.ngrRadio(formWithErrors)),
              selectedPropertyAddress = property.addressFull
            )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
        },
        radioValue =>
          raldRepo.insertDoesYourRentIncludeParking(
            credId = CredId(request.credId.getOrElse("")),
            radioValue = radioValue.radio
          )
          if (radioValue.radio == "Yes") {
            Future.successful(Redirect(routes.CheckRentFreePeriodController.show.url))
          } else {
            //TODO
            Future.successful(Redirect(routes.CheckRentFreePeriodController.show.url))
          }

      )
    }
}
