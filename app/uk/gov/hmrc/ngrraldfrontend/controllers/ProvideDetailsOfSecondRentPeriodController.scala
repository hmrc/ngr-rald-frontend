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
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfSecondRentPeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfSecondRentPeriodForm.*
import uk.gov.hmrc.ngrraldfrontend.models.{Mode, NormalMode, ProvideDetailsOfSecondRentPeriod, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstRentPeriodPage, ProvideDetailsOfSecondRentPeriodPage}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.DateKeyFinder
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfSecondRentPeriodView

import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.libs.json.*

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ProvideDetailsOfSecondRentPeriodController @Inject()(view: ProvideDetailsOfSecondRentPeriodView,
                                                           authenticate: AuthRetrievals,
                                                           mcc: MessagesControllerComponents,
                                                           getData: DataRetrievalAction,
                                                           sessionRepository: SessionRepository,
                                                           navigator: Navigator,
                                                         )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport with DateKeyFinder:



  def show(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>

      def formatDate = {
        val firstRentPeriodEnd = request.userAnswers.getOrElse(UserAnswers(request.credId))
          .get(ProvideDetailsOfFirstRentPeriodPage).map(_.endDate).getOrElse("").toString
        val date = LocalDate.parse(firstRentPeriodEnd)
        val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
        date.format(outputFormatter)
      }

      formatDate match {
        case "" => Future.successful(Redirect(routes.ProvideDetailsOfFirstRentPeriodController.show(NormalMode)))
        case _ =>
          val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(ProvideDetailsOfSecondRentPeriodPage).fold(form)(form.fill)
          Future.successful(Ok(view(
            request.property.addressFull,
            preparedForm,
            formatDate,
            endDateInput,
            mode = mode
            )))
      }
    }

  def submit(mode: Mode): Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            def formatDate = {
              val firstRentPeriodEnd = request.userAnswers.getOrElse(UserAnswers(request.credId))
                .get(ProvideDetailsOfFirstRentPeriodPage).map(_.endDate).getOrElse(ProvideDetailsOfSecondRentPeriodPage).toString
              val date = LocalDate.parse(firstRentPeriodEnd)
              val outputFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.UK)
              date.format(outputFormatter)
            }

            Future.successful(BadRequest(view(
              request.property.addressFull,
              formWithErrors,
              formatDate,
              endDateInput,
              mode
            ))),
          provideDetailsOfSecondRentPeriod =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(ProvideDetailsOfSecondRentPeriodPage, provideDetailsOfSecondRentPeriod))
              _ <- sessionRepository.set(updatedAnswers)
            } yield Redirect(navigator.nextPage(ProvideDetailsOfSecondRentPeriodPage, NormalMode, updatedAnswers))
        )
    }
