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
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouGetMoneyFromLandlordForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouGetMoneyFromLandlordForm.form
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.DidYouGetMoneyFromLandlordPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouGetMoneyFromLandlordView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DidYouGetMoneyFromLandlordController  @Inject()(didYouGetMoneyFromLandlordView: DidYouGetMoneyFromLandlordView,
                                                      authenticate: AuthRetrievals,
                                                      getData: DataRetrievalAction,
                                                      sessionRepository: SessionRepository,
                                                      navigator: Navigator,
                                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(DidYouGetMoneyFromLandlordPage) match {
        case None => form
        case Some(value) => form.fill(DidYouGetMoneyFromLandlordForm(if(value) {"Yes"} else {"No"}))

      }
        Future.successful(Ok(didYouGetMoneyFromLandlordView(
          selectedPropertyAddress = request.property.addressFull,
          form = preparedForm,
          ngrRadio = buildRadios(preparedForm, DidYouGetMoneyFromLandlordForm.moneyLandlordRadio),
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(didYouGetMoneyFromLandlordView(
              form = formWithErrors,
              ngrRadio = buildRadios(formWithErrors, DidYouGetMoneyFromLandlordForm.moneyLandlordRadio),
              selectedPropertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        radioValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(DidYouGetMoneyFromLandlordPage, radioValue.radio match {
              case "Yes" => true
              case _ => false
            }))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DidYouGetMoneyFromLandlordPage, mode, updatedAnswers))

      )
    }
}
