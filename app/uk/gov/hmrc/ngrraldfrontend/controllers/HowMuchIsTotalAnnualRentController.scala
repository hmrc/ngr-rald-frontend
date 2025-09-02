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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction, PropertyLinkingAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowMuchIsTotalAnnualRentForm
import uk.gov.hmrc.ngrraldfrontend.models.forms.HowMuchIsTotalAnnualRentForm.form
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.HowMuchIsTotalAnnualRentPage
import uk.gov.hmrc.ngrraldfrontend.repo.{RaldRepo, SessionRepository}
import uk.gov.hmrc.ngrraldfrontend.views.html.HowMuchIsTotalAnnualRentView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class HowMuchIsTotalAnnualRentController @Inject()(howMuchIsTotalAnnualRentView: HowMuchIsTotalAnnualRentView,
                                                   authenticate: AuthRetrievals,
                                                   hasLinkedProperties: PropertyLinkingAction,
                                                   raldRepo: RaldRepo,
                                                   getData: DataRetrievalAction,
                                                   sessionRepository: SessionRepository,
                                                   navigator: Navigator,
                                                   mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {


  def show: Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      val preparedForm = request.userAnswers.getOrElse(UserAnswers(request.credId)).get(HowMuchIsTotalAnnualRentPage) match {
        case None => form
        case Some(value) => form.fill(HowMuchIsTotalAnnualRentForm(value))
      }
        Future.successful(Ok(howMuchIsTotalAnnualRentView(
          form = preparedForm,
          propertyAddress = request.property.addressFull,
        )))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors => {
            Future.successful(BadRequest(howMuchIsTotalAnnualRentView(
              form = formWithErrors,
              propertyAddress = request.property.addressFull
            )))
        },
        rentAmount =>
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(HowMuchIsTotalAnnualRentPage, rentAmount.annualRent))
            _ <- sessionRepository.set(updatedAnswers)
          } yield Redirect(navigator.nextPage(HowMuchIsTotalAnnualRentPage, NormalMode, updatedAnswers))
      )
    }
}
