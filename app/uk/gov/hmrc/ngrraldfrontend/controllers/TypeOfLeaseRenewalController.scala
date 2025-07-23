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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, PropertyLinkingAction, RegistrationAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.NGRRadio
import uk.gov.hmrc.ngrraldfrontend.models.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.TypeOfLeaseRenewalForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.TypeOfLeaseRenewalForm.form
import uk.gov.hmrc.ngrraldfrontend.views.html.TypeOfLeaseRenewalView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TypeOfLeaseRenewalController @Inject()(typeOfLeaseRenewalView: TypeOfLeaseRenewalView,
                                             authenticate: AuthRetrievals,
                                             hasLinkedProperties: PropertyLinkingAction,
                                             mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show: Action[AnyContent] = {
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      Future.successful(Ok(typeOfLeaseRenewalView(
        form = form,
        navigationBarContent = createDefaultNavBar,
        radios = buildRadios(form, TypeOfLeaseRenewalForm.ngrRadio),
        propertyAddress = request.propertyLinking.get.addressFull,
      )))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen hasLinkedProperties).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
              Future.successful(BadRequest(typeOfLeaseRenewalView(
                form = formWithErrors,
                navigationBarContent = createDefaultNavBar,
                radios = buildRadios(formWithErrors, TypeOfLeaseRenewalForm.ngrRadio),
                propertyAddress = request.propertyLinking.get.addressFull
              )))
        },
         answers =>
           Future.successful(NotImplemented)
      )
    }


}

