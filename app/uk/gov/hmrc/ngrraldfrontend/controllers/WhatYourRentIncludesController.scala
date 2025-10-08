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
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.buildRadios
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatYourRentIncludesForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatYourRentIncludesForm.{answerToForm, form, formToAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, UserAnswers, WhatYourRentIncludes}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.WhatYourRentIncludesPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatYourRentIncludesView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

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
      case Some(value) => answerToForm(value, isOTCLease = false)
      case None => form(isOTCLease = false)
    }
        Future.successful(Ok(whatYourRentIncludesView(
          form = preparedForm,
          radios1 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio1(preparedForm, inputText)),
          radios2 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio2),
          radios3 = buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio3),
          radios4 = Some(buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio4)),
          radios5 = Some(buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio5)),
          radios6 = Some(buildRadios(preparedForm, WhatYourRentIncludesForm.ngrRadio6)),
          propertyAddress = request.property.addressFull,
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form(isOTCLease = false).bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(whatYourRentIncludesView(
              form = formWithErrors,
              radios1 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio1(formWithErrors, inputText)),
              radios2 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio2),
              radios3 = buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio3),
              radios4 = Some(buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio4)),
              radios5 = Some(buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio5)),
              radios6 = Some(buildRadios(formWithErrors, WhatYourRentIncludesForm.ngrRadio6)),
              propertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        whatYourRentIncludesForm =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId))
              .set(WhatYourRentIncludesPage, formToAnswers(whatYourRentIncludesForm, isOTCLease = false)))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(WhatYourRentIncludesPage, mode, updatedAnswers))
      )
    }
}
