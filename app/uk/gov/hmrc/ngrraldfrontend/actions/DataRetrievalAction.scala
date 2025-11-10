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

import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionTransformer, Result}
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.models.AuthenticatedUserRequest
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.requests.{IdentifierRequest, OptionalDataRequest}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(
                                         val sessionRepository: SessionRepository,
                                         ngrConnector: NGRConnector,
                                         appConfig: AppConfig
                                       )(implicit val executionContext: ExecutionContext) extends DataRetrievalAction  {

  override protected def transform[A](request: AuthenticatedUserRequest[A]): Future[OptionalDataRequest[A]] = {

    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    sessionRepository.get(request.credId.getOrElse("")).flatMap { userAnswersOpt =>
      ngrConnector.getLinkedProperty(CredId(request.credId.getOrElse(""))).map {
        case Some(property) =>
          OptionalDataRequest(request.request, request.credId.getOrElse(""), userAnswersOpt, property, request.email)
        case None => throw new NotFoundException("Property not found")
      }
    }
  }
}

trait DataRetrievalAction extends ActionTransformer[AuthenticatedUserRequest, OptionalDataRequest]