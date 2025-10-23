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
import uk.gov.hmrc.ngrraldfrontend.models.forms.AboutRepairsAndFittingOutForm
import uk.gov.hmrc.ngrraldfrontend.models.{AboutRepairsAndFittingOut, Mode, NGRMonthYear, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.AboutRepairsAndFittingOutPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.AboutRepairsAndFittingOutView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class AboutRepairsAndFittingOutController @Inject()(
                                                     view: AboutRepairsAndFittingOutView,
                                                     authenticate: AuthRetrievals,
                                                     getData: DataRetrievalAction,
                                                     sessionRepository: SessionRepository,
                                                     navigator: Navigator,
                                                     mcc: MessagesControllerComponents
                                                   )(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  private val form = AboutRepairsAndFittingOutForm.form

  def show(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async { implicit request =>
    val preparedForm = request.userAnswers
      .getOrElse(UserAnswers(request.credId))
      .get(AboutRepairsAndFittingOutPage) match {
      case None => form
      case Some(value) =>
        form.fill(AboutRepairsAndFittingOutForm(
          cost = BigDecimal(value.cost.toString),
          date = NGRMonthYear.fromString(value.date)
        ))
    }

    Future.successful(
      Ok(view(preparedForm, request.property.addressFull, mode))
    )
  }

  def submit(mode: Mode): Action[AnyContent] = (authenticate andThen getData).async { implicit request =>
    form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(view(formWithErrors, request.property.addressFull, mode))),
      validData => {
        val updatedModel = AboutRepairsAndFittingOut(
          cost = validData.cost,
          date = validData.date.makeString
        )

        for {
          updatedAnswers <- Future.fromTry(
            request.userAnswers.getOrElse(UserAnswers(request.credId))
              .set(AboutRepairsAndFittingOutPage, updatedModel)
          )
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(AboutRepairsAndFittingOutPage, mode, updatedAnswers))
      }
    )
  }
}



