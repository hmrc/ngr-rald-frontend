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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.RaldUserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, RaldUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.components.NavBarPageContents.createDefaultNavBar
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.TellUsAboutYourNewAgreementPage
import uk.gov.hmrc.ngrraldfrontend.repo.{RaldRepo, SessionRepository}
import uk.gov.hmrc.ngrraldfrontend.views.html.TellUsAboutYourAgreementView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class TellUsAboutYourNewAgreementController @Inject()(view: TellUsAboutYourAgreementView,
                                                      authenticate: AuthRetrievals,
                                                      mcc: MessagesControllerComponents,
                                                      getData: DataRetrievalAction,
                                                      sessionRepository: SessionRepository,
                                                      navigator: Navigator
                                                     )(implicit appConfig: AppConfig, ec:ExecutionContext)  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
          Future.successful(Ok(view(selectedPropertyAddress = request.property.addressFull, agreement = NewAgreement)))
    }
  }

    def submit: Action[AnyContent] = {
      (authenticate andThen getData).async { implicit request =>
        for {
          updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.credId)).set(TellUsAboutYourNewAgreementPage, NewAgreement))
          _ <- sessionRepository.set(updatedAnswers)
        } yield Redirect(navigator.nextPage(TellUsAboutYourNewAgreementPage, NormalMode, updatedAnswers))
      }
  }
}
