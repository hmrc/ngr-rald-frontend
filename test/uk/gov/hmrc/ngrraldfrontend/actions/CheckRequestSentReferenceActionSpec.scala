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
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.Ok
import play.api.mvc.{AnyContent, Request, Result}
import play.api.test.Helpers.{OK, SEE_OTHER, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.AuthConnector
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.ngrraldfrontend.controllers.routes
import uk.gov.hmrc.ngrraldfrontend.helpers.{TestData, TestSupport}
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.DeclarationPage
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository

import scala.concurrent.Future

class CheckRequestSentReferenceActionSpec extends TestSupport with TestData {


  override implicit lazy val app: Application = GuiceApplicationBuilder().build()

  private val mockSessionRepository: SessionRepository = mock[SessionRepository]
  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val mockAuthAction = new AuthRetrievalsImpl(mockAuthConnector, mcc)

  private val action = new CheckRequestSentReferenceActionImpl(
    sessionRepository = mockSessionRepository,
    authenticate = mockAuthAction,
    mcc = mcc,
    appConfig = mockConfig
  )
  private class Stubs {
    def successBlock(request: Request[AnyContent]): Future[Result] = Future.successful(Ok(""))
  }

  private implicit class HelperOps[A](a: A) {
    def ~[B](b: B) = new~(a, b)
  }

  private val testRaldUserAnswers: Option[UserAnswers] =
    UserAnswers(credId = credId).set(DeclarationPage, "CTTW-DSWP-H9G2").toOption

  private val retrievalResult: Future[mockAuthAction.RetrievalsType] =
    Future.successful(
      Some(testCredId) ~
        Some(testNino) ~
        testConfidenceLevel ~
        Some(testEmail) ~
        Some(testAffinityGroup) ~
        Some(testName)
    )
   private val stubs = spy(new Stubs)

  "CheckRequestSentReferenceAction" when {

    "Rald user answers found in the mongoDB" must {
      "redirect to check your details page when there is request sent reference" in {
        when(mockAuthConnector.authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())).thenReturn(retrievalResult)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(testRaldUserAnswers))

        val result = action.invokeBlock(fakeRequest, stubs.successBlock)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(mockConfig.ngrCheckYourDetailsUrl)
      }

      "Return OK when there is no request sent reference" in {
        when(mockAuthConnector.authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())).thenReturn(retrievalResult)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(Some(UserAnswers(credId))))

        val result = action.invokeBlock(fakeRequest, stubs.successBlock)
        status(result) mustBe OK
      }
    }
    "rald user answers is not found in the mongoDB" must {
      "Return OK" in {
        when(mockAuthConnector.authorise[mockAuthAction.RetrievalsType](any(), any())(any(), any())).thenReturn(retrievalResult)
        when(mockSessionRepository.get(any())).thenReturn(Future.successful(None))

        val result = action.invokeBlock(fakeRequest, stubs.successBlock)
        status(result) mustBe OK
      }
    }
  }
}




