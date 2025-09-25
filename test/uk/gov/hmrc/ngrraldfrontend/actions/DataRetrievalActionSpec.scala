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
import org.mockito.Mockito.when
import play.api.test.FakeRequest
import play.api.test.Helpers.await
import uk.gov.hmrc.http.{HeaderCarrier, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.helpers.{ControllerSpecSupport, TestSupport}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, PropertyLinkingUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.requests.{IdentifierRequest, OptionalDataRequest}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty
import play.api.test.Helpers.defaultAwaitTimeout
import uk.gov.hmrc.ngrraldfrontend.mocks.MockHttpV2

import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionSpec extends ControllerSpecSupport with MockHttpV2{

  class StubNGRConnector(property: VMVProperty)(implicit ec: ExecutionContext) extends NGRConnector(mockHttpClientV2, mockConfig) {
    override def getLinkedProperty(credId: CredId)(implicit hc: HeaderCarrier): Future[Option[VMVProperty]] =
      Future.successful(Some(property))
  }

  class StubNGRConnectorReturnsNone(implicit ec: ExecutionContext) extends NGRConnector(mockHttpClientV2, mockConfig) {
    override def getLinkedProperty(credId: CredId)(implicit hc: HeaderCarrier): Future[Option[VMVProperty]] =
      Future.successful(None)
  }

  class Harness(sessionRepository: SessionRepository, ngrConnector: NGRConnector, appConfig: AppConfig)
    extends DataRetrievalActionImpl(sessionRepository, ngrConnector, appConfig) {
    def callTransform[A](request: AuthenticatedUserRequest[A]): Future[OptionalDataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" when {

    "there is no data in the cache" should {

      "must set userAnswers to 'None' in the request" in {
        when(mockSessionRepository.get(any())) thenReturn Future(None)

        val stubConnector = new StubNGRConnector(property)
        val action = new Harness(mockSessionRepository, stubConnector, mockConfig)
        val result = action.callTransform(authenticatedFakeRequest).futureValue

        result.userAnswers must not be defined
      }

      "when property is not found" should {

        "throw NotFoundException" in {
          when(mockSessionRepository.get(any())) thenReturn Future(Some(UserAnswers("id")))

          val stubConnector = new StubNGRConnectorReturnsNone
          val action = new Harness(mockSessionRepository, stubConnector, mockConfig)

          val thrown = intercept[NotFoundException] {
            await(action.callTransform(authenticatedFakeRequest))
          }

          thrown.getMessage mustBe "Property not found"
        }
      }


    }

    "when there is data in the cache" should {

      "must build a userAnswers object and add it to the request" in {
        when(mockSessionRepository.get(any())) thenReturn Future(Some(UserAnswers("id")))

        val stubConnector = new StubNGRConnector(property)
        val action = new Harness(mockSessionRepository, stubConnector, mockConfig)
        val result = action.callTransform(authenticatedFakeRequest).futureValue

        result.userAnswers mustBe defined
      }
    }
  }
}
