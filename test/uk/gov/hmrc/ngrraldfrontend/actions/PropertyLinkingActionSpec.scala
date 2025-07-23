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
import uk.gov.hmrc.ngrraldfrontend.helpers.{TestData, TestSupport}
import uk.gov.hmrc.ngrraldfrontend.models.{PropertyLinkingUserAnswers, RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.*

import scala.concurrent.Future

class PropertyLinkingActionSpec extends TestSupport with TestData {
  val linkedProperty = PropertyLinkingUserAnswers(credId, property)

  override implicit lazy val app: Application = GuiceApplicationBuilder().build()

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  val mockAuthAction = new AuthRetrievalsImpl(mockAuthConnector, mcc)

  private class Stubs {
    def successBlock(request: Request[AnyContent]): Future[Result] = Future.successful(Ok(""))
  }

  private val testRequest = FakeRequest("GET", "/paye/company-car")

  val propertyLinkingAction = new PropertyLinkingActionImpl(
    ngrConnector = mockNGRConnector,
    authenticate = mockAuthAction,
    raldRepo = mockRaldRepo,
    appConfig = mockConfig,
    mcc)

  private implicit class HelperOps[A](a: A) {
    def ~[B](b: B) = new~(a, b)
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

  "Property Linking Action" when {
    "a user navigating to /ngr-dashboard-frontend/select-your-property" must {
      "must be navigated to requested page if not registered" in {
        when(
          mockAuthConnector.authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())
        )
          .thenReturn(retrievalResult)
        when(mockNGRConnector.getRatepayer(any())(any()))
          .thenReturn(Future.successful(Some(RatepayerRegistrationValuation(credId, Some(testRegistrationModel)))))

        val stubs = spy(new Stubs)

        val authResult = mockAuthAction.invokeBlock(testRequest, stubs.successBlock)
        status(authResult) mustBe OK

        val result = propertyLinkingAction.invokeBlock(testRequest, stubs.successBlock)
        status(result) mustBe SEE_OTHER
      }

      "must be navigated to dashboard page if cannot find linked property but it's registered" in {
        when(
          mockAuthConnector
            .authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())
        )
          .thenReturn(retrievalResult)

        when(mockNGRConnector.getRatepayer(any())(any()))
          .thenReturn(Future.successful(Some(RatepayerRegistrationValuation(credId, Some(testRegistrationModel.copy(isRegistered = Some(true)))))))

        when(mockRaldRepo.findByCredId(any())).thenReturn(Future.successful(None))
        when(mockNGRConnector.getPropertyLinkingUserAnswers(any())(any())).thenReturn(Future.successful(None))

        val stubs = spy(new Stubs)

        val authResult = mockAuthAction.invokeBlock(testRequest, stubs.successBlock)
        status(authResult) mustBe OK

        val result = propertyLinkingAction.invokeBlock(testRequest, stubs.successBlock)
        status(result) mustBe SEE_OTHER
      }

      "must return OK if found linked property in the backend and not the frontend and it's registered" in {
        when(
          mockAuthConnector
            .authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())
        )
          .thenReturn(retrievalResult)

        when(mockNGRConnector.getRatepayer(any())(any()))
          .thenReturn(Future.successful(Some(RatepayerRegistrationValuation(credId, Some(testRegistrationModel.copy(isRegistered = Some(true)))))))

        when(mockRaldRepo.findByCredId(any())).thenReturn(Future.successful(None))
        when(mockNGRConnector.getPropertyLinkingUserAnswers(any())(any())).thenReturn(Future.successful(Some(PropertyLinkingUserAnswers(credId, property))))

        val stubs = spy(new Stubs)

        val authResult = mockAuthAction.invokeBlock(testRequest, stubs.successBlock)
        status(authResult) mustBe OK

        val result = propertyLinkingAction.invokeBlock(testRequest, stubs.successBlock)
        status(result) mustBe OK
      }

      "must return OK if found linked property and it's registered" in {
        when(
          mockAuthConnector
            .authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())
        )
          .thenReturn(retrievalResult)

        when(mockNGRConnector.getRatepayer(any())(any()))
          .thenReturn(Future.successful(Some(RatepayerRegistrationValuation(credId, Some(testRegistrationModel.copy(isRegistered = Some(true)))))))

        when(mockRaldRepo.findByCredId(any())).thenReturn(Future.successful(Some(RaldUserAnswers(credId, property))))

        val stubs = spy(new Stubs)

        val authResult = mockAuthAction.invokeBlock(testRequest, stubs.successBlock)
        status(authResult) mustBe OK

        val result = propertyLinkingAction.invokeBlock(testRequest, stubs.successBlock)
        status(result) mustBe OK
      }
    }
  }
}
