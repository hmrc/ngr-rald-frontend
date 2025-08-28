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

package uk.gov.hmrc.ngrraldfrontend.models.requests

import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty

case class OptionalDataRequest[A] (request: Request[A], userId: String, userAnswers: Option[UserAnswers], property: VMVProperty) extends WrappedRequest[A](request)

case class DataRequest[A] (request: Request[A], credId: String, userAnswers: UserAnswers) extends WrappedRequest[A](request)
