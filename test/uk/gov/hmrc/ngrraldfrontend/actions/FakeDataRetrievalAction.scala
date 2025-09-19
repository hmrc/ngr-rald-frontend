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

import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.TestData
import uk.gov.hmrc.ngrraldfrontend.models.AuthenticatedUserRequest
import uk.gov.hmrc.ngrraldfrontend.models.requests.{IdentifierRequest, OptionalDataRequest}
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty

import scala.concurrent.{ExecutionContext, Future}

class FakeDataRetrievalAction(answers: Option[UserAnswers], propertyOpt: Option[VMVProperty]) extends DataRetrievalAction with TestData {

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global

  override protected def transform[A](request: AuthenticatedUserRequest[A]): Future[OptionalDataRequest[A]] = {
    propertyOpt match {
      case Some(value) =>  Future.successful(
        OptionalDataRequest(request.request, request.credId.getOrElse(""), answers, property)
      )
      case None => throw new NotFoundException("Could not find answers in backend mongo")
    }
     
  }
}
