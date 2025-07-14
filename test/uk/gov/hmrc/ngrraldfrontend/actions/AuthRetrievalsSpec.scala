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

import org.mockito.ArgumentMatchers.*
import org.mockito.Mockito.{spy, when}
import play.api.Application
import play.api.http.Status.OK
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Result
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import uk.gov.hmrc.auth.core.*
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.{Retrieval, ~}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.models.AuthenticatedUserRequest

import scala.concurrent.{ExecutionContext, Future}
class AuthRetrievalsSpec extends TestSupport{

  override implicit lazy val app: Application = GuiceApplicationBuilder().build()

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]

  private class Stubs {
    def successBlock(request: AuthenticatedUserRequest[_]): Future[Result] = Future.successful(Ok(""))
  }

  private val testRequest = FakeRequest("GET", "/paye/company-car")

  val authAction = new AuthRetrievalsImpl(mockAuthConnector, mcc)

  private implicit class HelperOps[A](a: A) {
    def ~[B](b: B) = new ~(a, b)
  }

  "Auth Action" when {
    "a user navigating to /ngr-login-register-frontend/start" must {
      "the user has a confidence level of 250 with all details" in {

        val retrievalResult: Future[authAction.RetrievalsType] =
          Future.successful(
            Some(testCredId) ~
              Some(testNino) ~
              testConfidenceLevel ~
              Some(testEmail) ~
              Some(testAffinityGroup) ~
              Some(testName)
          )

        when(
          mockAuthConnector
            .authorise[authAction.RetrievalsType](any(), any())(any(), any())
        ).thenReturn(retrievalResult)

        val stubs = spy(new Stubs)

        val result = authAction.invokeBlock(testRequest, stubs.successBlock)

        status(result) mustBe OK
      }

      "is not logged in" must {
        "throw a MissingBearerToken exception" in {

          when(mockAuthConnector.authorise(any(), any())(any(), any()))
            .thenReturn(Future.failed(new MissingBearerToken))

          val stubs = new Stubs()

          val result = authAction.invokeBlock(testRequest, stubs.successBlock)

          whenReady(result.failed) { e =>
            e mustBe a[MissingBearerToken]
          }
        }

        "throw a InvalidBearerToken exception" in {

          when(mockAuthConnector.authorise(any(), any())(any(), any()))
            .thenReturn(Future.failed(new InvalidBearerToken))

          val stubs = new Stubs()

          val result = authAction.invokeBlock(testRequest, stubs.successBlock)

          whenReady(result.failed) { e =>
            e mustBe a[InvalidBearerToken]
          }
        }
      }
      "the user's session has expired" must {
        "redirect the user to log in " in {
          when(mockAuthConnector.authorise(any(), any())(any(), any()))
            .thenReturn(Future.failed(new BearerTokenExpired))

          val stubs = new Stubs()

          val result = authAction.invokeBlock(testRequest, stubs.successBlock)

          whenReady(result.failed) { e =>
            e mustBe a[BearerTokenExpired]
          }
        }
      }
      "the user doesn't have sufficient enrolments" must {
        "redirect the user to the unauthorised page" in {

          when(mockAuthConnector.authorise(any(), any())(any(), any()))
            .thenReturn(Future.failed(new InsufficientEnrolments))

          val stubs = new Stubs()

          val result = authAction.invokeBlock(testRequest, stubs.successBlock)

          whenReady(result.failed) { e =>
            e mustBe a[InsufficientEnrolments]
          }
        }
      }
      "the user used an unaccepted auth provider" must {
        "redirect the user to the unauthorised page" in {

          when(mockAuthConnector.authorise(any(), any())(any(), any()))
            .thenReturn(Future.failed(new UnsupportedAuthProvider))

          val stubs = new Stubs()

          val result = authAction.invokeBlock(testRequest, stubs.successBlock)

          whenReady(result.failed) { e =>
            e mustBe a[UnsupportedAuthProvider]
          }
        }
      }
    }
  }
}

class FakeFailingAuthConnector(exceptionToReturn: Throwable) extends AuthConnector {

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit
                                                                           hc: HeaderCarrier,
                                                                           ec: ExecutionContext
  ): Future[A] =
    Future.failed(exceptionToReturn)
}