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
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, simpleNgrRadio}
import uk.gov.hmrc.ngrraldfrontend.models.forms.CheckRentFreePeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.CheckRentFreePeriodForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.CheckRentFreePeriodPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.CheckRentFreePeriodView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CheckRentFreePeriodController @Inject()(checkRentFreePeriodView: CheckRentFreePeriodView,
                                              authenticate : AuthRetrievals,
                                              getData: DataRetrievalAction,
                                              navigator: Navigator,
                                              sessionRepository: SessionRepository,
                                              mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(CheckRentFreePeriodPage) match {
        case None => form
        case Some(value) => form.fill(CheckRentFreePeriodForm(value.toString))
      }
        Future.successful(Ok(checkRentFreePeriodView(
          form = preparedForm,
          radios = buildRadios(preparedForm, simpleNgrRadio(CheckRentFreePeriodForm.checkRentPeriodRadio)),
          propertyAddress = request.property.addressFull,
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(checkRentFreePeriodView(
              form = formWithErrors,
              radios = buildRadios(formWithErrors, simpleNgrRadio(CheckRentFreePeriodForm.checkRentPeriodRadio)),
              propertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        radioValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
              .set(CheckRentFreePeriodPage, radioValue.radioValue.toBoolean))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(CheckRentFreePeriodPage, mode, updatedAnswers))
      )
    }

}
