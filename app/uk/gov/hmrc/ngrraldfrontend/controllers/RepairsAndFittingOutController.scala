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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.components.NGRRadio.{buildRadios, simpleNgrRadio}
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndFittingOutForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.RepairsAndFittingOutForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.RepairsAndFittingOutPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.RepairsAndFittingOutView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class RepairsAndFittingOutController  @Inject()(repairsAndFittingView: RepairsAndFittingOutView,
                                                authenticate: AuthRetrievals,
                                                getData: DataRetrievalAction,
                                                checkRequestSentReference: CheckRequestSentReferenceAction,
                                                sessionRepository: SessionRepository,
                                                navigator: Navigator,
                                                mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(RepairsAndFittingOutPage) match {
        case None => form
        case Some(value) => form.fill(RepairsAndFittingOutForm(value.toString))
      }
        Future.successful(Ok(repairsAndFittingView(
          selectedPropertyAddress = request.property.addressFull,
          form = preparedForm,
          ngrRadio = buildRadios(preparedForm, RepairsAndFittingOutForm.repairsAndFittingOutRadio),
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(repairsAndFittingView(
              form = formWithErrors,
              ngrRadio = buildRadios(formWithErrors, RepairsAndFittingOutForm.repairsAndFittingOutRadio),
              selectedPropertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        radioValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
              .set(RepairsAndFittingOutPage, radioValue.radioValue.toBoolean))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(RepairsAndFittingOutPage, mode, updatedAnswers))

      )
    }
}
