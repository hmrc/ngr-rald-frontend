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
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, CheckRequestSentReferenceAction, DataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.connectors.{NGRConnector, NGRNotifyConnector}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{AssessmentIdKey, DeclarationPage}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.utils.UniqueIdGenerator
import uk.gov.hmrc.ngrraldfrontend.views.html.DeclarationView
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DeclarationController @Inject()(declarationView: DeclarationView,
                                      authenticate: AuthRetrievals,
                                      getData: DataRetrievalAction,
                                      checkRequestSentReference: CheckRequestSentReferenceAction,
                                      navigator: Navigator,
                                      sessionRepository: SessionRepository,
                                      ngrConnector: NGRConnector,
                                      notifyConnector: NGRNotifyConnector,
                                      mcc: MessagesControllerComponents)(implicit appConfig: AppConfig, ec: ExecutionContext)
  extends FrontendController(mcc) with I18nSupport {

  def show: Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>
      Future.successful(Ok(declarationView()))
    }
  }

  def submit: Action[AnyContent] = {
    (authenticate andThen checkRequestSentReference andThen getData).async { implicit request =>

      val baseAnswers =
        request.userAnswers.getOrElse(UserAnswers(CredId(request.credId)))

      val assessmentId = baseAnswers.get(AssessmentIdKey).getOrElse(throw new NotFoundException("No Assessment ID found"))

      val updatedAnswersTry =
        baseAnswers.set(DeclarationPage, assessmentId)

      for {
        updatedAnswers <- Future.fromTry(updatedAnswersTry)

        saved <- sessionRepository.set(updatedAnswers)
        _     <- if (saved) Future.unit
        else Future.failed(new Exception(s"Could not save reference for credId: ${request.credId}"))

        upsertResponse <- ngrConnector.upsertRaldUserAnswers(updatedAnswers)
        _ <- upsertResponse.status match {
          case CREATED => Future.unit
          case _       => Future.failed(new Exception(s"Failed upsert to backend for credId: ${request.credId}"))
        }

        notifyStatus <- notifyConnector.postRaldChanges(updatedAnswers, assessmentId)
        _ <- if (notifyStatus == ACCEPTED || notifyStatus == CREATED)
          Future.unit
        else Future.failed(new Exception(s"Failed notify for credId: ${request.credId}, status=$notifyStatus"))

      } yield Redirect(navigator.nextPage(DeclarationPage, NormalMode, updatedAnswers))
    }
  }
}