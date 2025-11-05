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
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.DeclarationPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.views.html.DeclarationView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.UniqueIdGenerator

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(declarationView: DeclarationView,
                                      authenticate: AuthRetrievals,
                                      getData: DataRetrievalAction,
                                      navigator: Navigator,
                                      sessionRepository: SessionRepository,
                                      ngrConnector: NGRConnector,
                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = {
    (authenticate andThen getData).async { implicit request =>
      Future.successful(Ok(declarationView()))
    }
  }

  def submit: Action[AnyContent] =
    (authenticate andThen getData).async { implicit request =>
      for {
        updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))
          .set(DeclarationPage, UniqueIdGenerator.generateId)
        )
        result <- sessionRepository.set(updatedAnswers).flatMap {
          case true =>
            ngrConnector.upsertRaldUserAnswers(updatedAnswers).flatMap(
              _.status match
                case CREATED => Future.successful(Redirect(navigator.nextPage(DeclarationPage, NormalMode, updatedAnswers)))
                case _ => Future.failed(new Exception(s"Failed upsert to backend for credId: ${request.credId}"))
          )
          case _ => Future.failed(new Exception(s"Could not save reference for credId: ${request.credId}"))
        }
      } yield Redirect(navigator.nextPage(DeclarationPage, NormalMode, updatedAnswers))//result
    }
}