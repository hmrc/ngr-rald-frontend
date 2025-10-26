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
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstRentPeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfFirstRentPeriodForm.*
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, NormalMode, ProvideDetailsOfFirstRentPeriod, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.ProvideDetailsOfFirstRentPeriodPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfFirstRentPeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProvideDetailsOfFirstRentPeriodController @Inject()(view: ProvideDetailsOfFirstRentPeriodView,
                                                          authenticate: AuthRetrievals,
                                                          inputText: InputText,
                                                          mcc: MessagesControllerComponents,
                                                          getData: DataRetrievalAction,
                                                          sessionRepository: SessionRepository,
                                                          navigator: Navigator,
                                                         )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder:

  def show(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(ProvideDetailsOfFirstRentPeriodPage)
        .fold(form)(form.fill)
      Future.successful(Ok(view(
        selectedPropertyAddress = request.property.addressFull,
        preparedForm,
        firstDateStartInput,
        firstDateEndInput,
        buildRadios(preparedForm, firstRentPeriodRadio(preparedForm, inputText)),
        mode = mode
      )))
    }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            val correctedFormErrors = formWithErrors.errors.map { formError =>
              (formError.key, formError.messages) match
                case (key, messages) if messages.head.contains("provideDetailsOfFirstRentPeriod.startDate") =>
                  setCorrectKey(formError, "provideDetailsOfFirstRentPeriod", "startDate")
                case (key, messages) if messages.head.contains("provideDetailsOfFirstRentPeriod.endDate") =>
                  setCorrectKey(formError, "provideDetailsOfFirstRentPeriod", "endDate")
                case _ =>
                  formError
            }
            val formWithCorrectedErrors = formWithErrors.copy(errors = correctedFormErrors)
            Future.successful(BadRequest(view(
              selectedPropertyAddress = request.property.addressFull,
              formWithCorrectedErrors,
              firstDateStartInput,
              firstDateEndInput,
              buildRadios(formWithCorrectedErrors, firstRentPeriodRadio(formWithCorrectedErrors, inputText)),
              mode
            ))),
          provideDetailsOfFirstRentPeriod =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(ProvideDetailsOfFirstRentPeriodPage, provideDetailsOfFirstRentPeriod))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ProvideDetailsOfFirstRentPeriodPage, NormalMode, updatedAnswers))
        )
    }
