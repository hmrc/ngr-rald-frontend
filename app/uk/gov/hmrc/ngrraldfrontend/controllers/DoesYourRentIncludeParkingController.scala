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
import uk.gov.hmrc.ngrraldfrontend.models.forms.DoesYourRentIncludeParkingForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.DoesYourRentIncludeParkingForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.DoesYourRentIncludeParkingPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.DoesYourRentIncludeParkingView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DoesYourRentIncludeParkingController  @Inject()(doesYourRentIncludeParkingView: DoesYourRentIncludeParkingView,
                                                      authenticate: AuthRetrievals,
                                                      getData: DataRetrievalAction,
                                                      sessionRepository: SessionRepository,
                                                      navigator: Navigator,
                                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show(mode: Mode): Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(CredId(request.credId))).get(DoesYourRentIncludeParkingPage) match {
        case None => form
        case Some(value) => form.fill(DoesYourRentIncludeParkingForm(value.toString))

      }
        Future.successful(Ok(doesYourRentIncludeParkingView(
          selectedPropertyAddress = request.property.addressFull,
          form = preparedForm,
          ngrRadio = buildRadios(preparedForm, DoesYourRentIncludeParkingForm.includeParkingRadio),
          mode = mode
        )))
    }
  }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(doesYourRentIncludeParkingView(
              form = formWithErrors,
              ngrRadio = buildRadios(formWithErrors, DoesYourRentIncludeParkingForm.includeParkingRadio),
              selectedPropertyAddress = request.property.addressFull,
              mode = mode
            )))
        },
        radioValue =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
              .set(DoesYourRentIncludeParkingPage, radioValue.radio.toBoolean))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(DoesYourRentIncludeParkingPage, mode, updatedAnswers))

      )
    }
}
