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


import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{spy, when}
import play.api.Application
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.models.registration.RatepayerRegistrationValuation

import scala.concurrent.Future

class RegistrationActionSpec extends TestSupport {

  override implicit lazy val app: Application = GuiceApplicationBuilder().build()

  private val mockNGRConnector: NGRConnector = mock[NGRConnector]

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockAuthAction = new AuthRetrievalsImpl(mockAuthConnector, mcc)

  private class Stubs {
    def successBlock(request: Request[AnyContent]): Future[Result] = Future.successful(Ok(""))
  }

  private val testRequest = FakeRequest("GET", "/paye/company-car")

  val registrationAction = new RegistrationActionImpl(ngrConnector = mockNGRConnector,authenticate = mockAuthAction, appConfig = mockConfig, mcc)

  private implicit class HelperOps[A](a: A) {
    def ~[B](b: B) = new ~(a, b)
  }

  val retrievalResult: Future[mockAuthAction.RetrievalsType] =
    Future.successful(
      Some(testCredId) ~
        Some(testNino) ~
        testConfidenceLevel ~
        Some(testEmail) ~
        Some(testAffinityGroup) ~
        Some(testName)
    )

  "Registration Action" when {
    "a user navigating to /ngr-login-register-frontend/start" must {
      "must be navigated to requested page if not registered" in {
        when(
          mockAuthConnector
            .authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())
        )
          .thenReturn(retrievalResult)
        when(mockNGRConnector.getRatepayer(any())(any()))
          .thenReturn(Future.successful(Some(RatepayerRegistrationValuation(credId, Some(testRegistrationModel)))))

        val stubs = spy(new Stubs)


        val authResult = mockAuthAction.invokeBlock(testRequest, stubs.successBlock)
        status(authResult) mustBe OK

        val result = registrationAction.invokeBlock(testRequest, stubs.successBlock)
        status(result) mustBe SEE_OTHER
      }
      "must be navigated to property linking if registered" in {
        when(
          mockAuthConnector
            .authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())
        )
          .thenReturn(retrievalResult)

        when(mockNGRConnector.getRatepayer(any())(any()))
          .thenReturn(Future.successful(Some(RatepayerRegistrationValuation(credId, Some(testRegistrationModel.copy(isRegistered = Some(true)))))))


        val stubs = spy(new Stubs)


        val authResult = mockAuthAction.invokeBlock(testRequest, stubs.successBlock)
        status(authResult) mustBe OK

        val result = registrationAction.invokeBlock(testRequest, stubs.successBlock)
        status(result) mustBe OK
      }
    }
  }
}
