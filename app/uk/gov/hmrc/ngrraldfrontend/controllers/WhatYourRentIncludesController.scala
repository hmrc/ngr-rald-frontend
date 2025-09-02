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
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatYourRentIncludesForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatYourRentIncludesForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.utils.Constants.{renewedAgreement, surrenderAndRenewal}
import uk.gov.hmrc.ngrraldfrontend.views.html. WhatYourRentIncludesView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatYourRentIncludesController @Inject()(whatYourRentIncludesView: WhatYourRentIncludesView,
                                               authenticate: AuthRetrievals,
                                               hasLinkedProperties: PropertyLinkingAction,
                                               raldRepo: RaldRepo,
                                               mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {
  
  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(whatYourRentIncludesView(
          form = form,
          navigationBarContent = createDefaultNavBar,
          radios1 = buildRadios(form, WhatYourRentIncludesForm.ngrRadio1),
          radios2 = buildRadios(form, WhatYourRentIncludesForm.ngrRadio2),
          radios3 = buildRadios(form, WhatYourRentIncludesForm.ngrRadio3),
          radios4 = buildRadios(form, WhatYourRentIncludesForm.ngrRadio4),
          radios5 = buildRadios(form, WhatYourRentIncludesForm.ngrRadio5),
          radios6 = buildRadios(form, WhatYourRentIncludesForm.ngrRadio6),
          propertyAddress = property.addressFull,
        )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          request.propertyLinking.map(property =>
            Future.successful(BadRequest(whatYourRentIncludesView(
              form = formWithErrors,
              navigationBarContent = createDefaultNavBar,
              radios1 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio1),
              radios2 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio2),
              radios3 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio3),
              radios4 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio4),
              radios5 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio5),
              radios6 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio6),
              propertyAddress = property.addressFull
            )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
        },
        radioValue =>
          raldRepo.insertWhatYourRentIncludes(
            credId = CredId(request.credId.getOrElse("")),
            radioValue.livingAccommodationRadio,
            radioValue.rentPartAddressRadio,
            radioValue.rentEmptyShellRadio,
            radioValue.rentIncBusinessRatesRadio,
            radioValue.rentIncWaterChargesRadio,
            radioValue.rentIncServiceRadio
          )
          Future.successful(Redirect(routes.LandlordController.show.url))
      )
    }
}
