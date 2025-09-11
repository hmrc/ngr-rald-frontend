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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.{Mode,NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouAgreeRentWithLandlordForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouAgreeRentWithLandlordForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{DidYouAgreeRentWithLandlordPage, WhatTypeOfLeaseRenewalPage}
import uk.gov.hmrc.ngrraldfrontend.repo.{RaldRepo, SessionRepository}
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouAgreeRentWithLandlordView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DidYouAgreeRentWithLandlordController @Inject()(didYouAgreeRentWithLandlordView: DidYouAgreeRentWithLandlordView,
                                                      authenticate: AuthRetrievals,
                                                      getData: DataRetrievalAction,
                                                      sessionRepository: SessionRepository,
                                                      navigator: Navigator,
                                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(DidYouAgreeRentWithLandlordPage) match {
        case None => form
        case Some(value) => form.fill(DidYouAgreeRentWithLandlordForm(value))
      }
        Future.successful(Ok(didYouAgreeRentWithLandlordView(
          selectedPropertyAddress = request.property.addressFull,
          navigationBarContent = createDefaultNavBar,
          form = preparedForm,
          ngrRadio = buildRadios(preparedForm, DidYouAgreeRentWithLandlordForm.ngrRadio(preparedForm)),
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(didYouAgreeRentWithLandlordView(
              form = formWithErrors,
              ngrRadio = buildRadios(formWithErrors, DidYouAgreeRentWithLandlordForm.ngrRadio(formWithErrors)),
              selectedPropertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        radioValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(DidYouAgreeRentWithLandlordPage, radioValue.radioValue))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DidYouAgreeRentWithLandlordPage, mode, updatedAnswers))

      )
    }
}
