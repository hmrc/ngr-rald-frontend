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
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatTypeOfAgreementForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatTypeOfAgreementForm.form
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.WhatTypeOfAgreementPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatTypeOfAgreementView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class WhatTypeOfAgreementController @Inject()(view: WhatTypeOfAgreementView,
                                              authenticate: AuthRetrievals,
                                              mcc: MessagesControllerComponents,
                                              getData: DataRetrievalAction,
                                              navigator: Navigator,
                                              sessionRepository: SessionRepository)
                                             (implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(WhatTypeOfAgreementPage) match {
        case None => form
        case Some(value) => form.fill(WhatTypeOfAgreementForm(value))
      }
        Future.successful(Ok(
          view(
            selectedPropertyAddress = request.property.addressFull,
            form = preparedForm,
            ngrRadio = buildRadios(preparedForm, WhatTypeOfAgreementForm.ngrRadio(preparedForm)),
            mode
          )
        ))
    }
  }

  def submit(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
              Future.successful(BadRequest(view(
                selectedPropertyAddress = request.property.addressFull,
                formWithErrors,
                buildRadios(formWithErrors, WhatTypeOfAgreementForm.ngrRadio(formWithErrors)),
                mode
              ))),
          whatTypeOfAgreementForm =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId))
                .set(WhatTypeOfAgreementPage, whatTypeOfAgreementForm.radioValue))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatTypeOfAgreementPage,mode,updatedAnswers))
        )
    }
  }
}
