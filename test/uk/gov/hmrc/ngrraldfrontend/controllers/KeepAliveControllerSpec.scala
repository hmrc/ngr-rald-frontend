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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.OK
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport

import scala.concurrent.Future


class KeepAliveControllerSpec extends ControllerSpecSupport {

  def controller = new KeepAliveController(
    mcc,
    mockAuthJourney,
    fakeDataProperty(Some(property), None),
    mockCheckRequestSentReference,
    mockSessionRepository
  )

  "KeepAliveController" when  {
    "Calling keep alive" should  {
      "update the createdAt" in {
        when(mockSessionRepository.keepAlive(any())).thenReturn(Future.successful(true))
        val result = controller.keepAlive()(authenticatedFakeRequest)
        status(result) mustBe OK

      }
    }
  }
}
