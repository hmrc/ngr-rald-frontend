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

package uk.gov.hmrc.ngrraldfrontend.actions

import com.google.inject.ImplementedBy
import play.api.mvc.Results.Redirect
import play.api.mvc.*
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.models.AuthenticatedUserRequest
import uk.gov.hmrc.ngrraldfrontend.pages.DeclarationPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CheckRequestSentReferenceActionImpl @Inject()(
                                                     val sessionRepository: SessionRepository,
                                                     authenticate: AuthRetrievals,
                                                     mcc: MessagesControllerComponents,
                                                     appConfig: AppConfig
                                                   )(implicit val ec: ExecutionContext) extends CheckRequestSentReferenceAction {

  override def invokeBlock[A](request: Request[A], block: AuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
    authenticate.invokeBlock(request, { implicit authRequest: AuthenticatedUserRequest[A] =>
      implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(authRequest, authRequest.session)
      val credId = authRequest.credId.getOrElse("")

      sessionRepository.get(credId).flatMap {
        case Some(userAnswers) => redirectIfFoundRequestSentReference(userAnswers.get(DeclarationPage), block)
        case None => block(authRequest)
      }
    })
  }

  private def redirectIfFoundRequestSentReference[A](sentReferenceOpt: Option[String], block: AuthenticatedUserRequest[A] => Future[Result])(implicit authRequest: AuthenticatedUserRequest[A]): Future[Result] = {
    sentReferenceOpt match {
      case Some(sentReference) => Future.successful(Redirect(appConfig.ngrCheckYourDetailsUrl))
      case None => block(authRequest)
    }
  }

  // $COVERAGE-OFF$
  override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser

  override protected def executionContext: ExecutionContext = ec
  // $COVERAGE-ON$
}

@ImplementedBy(classOf[CheckRequestSentReferenceActionImpl])
trait CheckRequestSentReferenceAction extends ActionBuilder[AuthenticatedUserRequest, AnyContent] with ActionFunction[Request, AuthenticatedUserRequest]