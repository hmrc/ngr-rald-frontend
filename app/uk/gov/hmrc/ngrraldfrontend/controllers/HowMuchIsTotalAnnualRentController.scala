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
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowMuchIsTotalAnnualRentForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowMuchIsTotalAnnualRentForm.{form}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.repo.RaldRepo
import uk.gov.hmrc.ngrraldfrontend.utils.Constants.{renewedAgreement, surrenderAndRenewal}
import uk.gov.hmrc.ngrraldfrontend.views.html.{HowMuchIsTotalAnnualRentView, WhatTypeOfLeaseRenewalView}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class HowMuchIsTotalAnnualRentController @Inject()(howMuchIsTotalAnnualRentView: HowMuchIsTotalAnnualRentView,
                                                   authenticate: AuthRetrievals,
                                                   hasLinkedProperties: PropertyLinkingAction,
                                                   raldRepo: RaldRepo,
                                                   mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      request.propertyLinking.map(property =>
        Future.successful(Ok(howMuchIsTotalAnnualRentView(
          form = form,
          navigationBarContent = createDefaultNavBar,
          propertyAddress = property.addressFull,
        )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
          request.propertyLinking.map(property =>
            Future.successful(BadRequest(howMuchIsTotalAnnualRentView(
              form = formWithErrors,
              navigationBarContent = createDefaultNavBar,
              propertyAddress = property.addressFull
            )))).getOrElse(throw new NotFoundException("Couldn't find property in mongo"))
        },
        rentAmount =>
          raldRepo.insertAnnualRent(
            credId = CredId(request.credId.getOrElse("")),
            rentAmount = rentAmount.annualRent.toInt
          )
          Future.successful(Redirect(routes.WhatTypeOfLeaseRenewalController.show.url))
      )
    }
}
