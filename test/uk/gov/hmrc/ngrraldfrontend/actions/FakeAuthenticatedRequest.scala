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

import play.api.mvc.{AnyContent, BodyParser, Request, Result}
import uk.gov.hmrc.auth.core.{ConfidenceLevel, Nino}
import uk.gov.hmrc.ngrraldfrontend.helpers.{TestData, TestSupport}
import uk.gov.hmrc.ngrraldfrontend.models.AuthenticatedUserRequest

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakeAuthenticatedRequest @Inject()(val parser: BodyParser[AnyContent])(implicit val executionContext: ExecutionContext)
  extends AuthRetrievals with TestData {
  override def invokeBlock[A](request: Request[A], block: AuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
    val newRequest = AuthenticatedUserRequest(
      request = request,
      confidenceLevel = Some(ConfidenceLevel.L250),
      authProvider = None,
      email = Some("user@email.com"),
      propertyLinking = Some(property),
      credId = Some("1234"),
      name = None,
      affinityGroup = None,
      nino = Nino(hasNino = true, Some("AA000003D"))
    )
    block(newRequest)
  }
}
