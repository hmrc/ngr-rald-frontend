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

package uk.gov.hmrc.ngrraldfrontend.helpers

import org.mockito.Mockito.when
import play.api.mvc.*
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.ngrraldfrontend.actions.{AuthRetrievals, DataRetrievalActionSpec, FakeAuthenticatedRequest, FakeDataRetrievalAction}
import uk.gov.hmrc.ngrraldfrontend.connectors.NGRConnector
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.{NewAgreement, RenewedAgreement, RentAgreement}
import uk.gov.hmrc.ngrraldfrontend.models.AuthenticatedUserRequest
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.requests.OptionalDataRequest
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty
import uk.gov.hmrc.ngrraldfrontend.navigation.Navigator
import uk.gov.hmrc.ngrraldfrontend.pages.{TellUsAboutRentPage, TellUsAboutYourNewAgreementPage, TellUsAboutYourRenewedAgreementPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import scala.concurrent.{ExecutionContext, Future}

trait ControllerSpecSupport extends TestSupport {
  val mockGetData: DataRetrievalActionSpec = mock[DataRetrievalActionSpec]
  val mockAuthJourney: AuthRetrievals = mock[AuthRetrievals]
  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  def fakeData(answers: Option[UserAnswers]) = new FakeDataRetrievalAction(answers, None)
  def fakeDataProperty(property: Option[VMVProperty], answers: Option[UserAnswers]) = new FakeDataRetrievalAction(answers, property)
  val mockNavigator: Navigator = inject[Navigator]
  val mockInputText: InputText = inject[InputText]
  val mockNgrConnector: NGRConnector = mock[NGRConnector]
  implicit val headerCarrier: HeaderCarrier = HeaderCarrier()
  val fakeAuth = new FakeAuthenticatedRequest(mcc.parsers.defaultBodyParser)
  val renewedAgreementAnswers: Option[UserAnswers] = UserAnswers("id").set(TellUsAboutYourRenewedAgreementPage, RenewedAgreement).toOption
  val newAgreementAnswers: Option[UserAnswers] = UserAnswers("id").set(TellUsAboutYourNewAgreementPage, NewAgreement).toOption
  val rentAgreementAnswers: Option[UserAnswers] = UserAnswers("id").set(TellUsAboutRentPage, RentAgreement).toOption


  //  def mockRequest(hasCredId: Boolean = false, hasNino: Boolean = true): Unit =
//    when(mockAuthJourney andThen mockGetData) thenReturn new ActionBuilder[AuthenticatedUserRequest, AnyContent] {
//      override def invokeBlock[A](request: Request[A], block: AuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
//        val authRequest = AuthenticatedUserRequest(
//          request,
//          None,
//          None,
//          Some("user@email.com"),
//          Some(property),
//          if (hasCredId) Some("1234") else None,
//          None,
//          None,
//          nino = if (hasNino) Nino(hasNino = true, Some("AA000003D")) else Nino(hasNino = false, None)
//        )
//        block(authRequest)
//      }
//
//      override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser
//
//      override protected def executionContext: ExecutionContext = ec
//    }
//
//
//  def mockRequestWithoutProperty(hasCredId: Boolean = false, hasNino: Boolean = true): Unit =
//    when(mockAuthJourney andThen mockGetData) thenReturn new ActionBuilder[AuthenticatedUserRequest, AnyContent] {
//      override def invokeBlock[A](request: Request[A], block: AuthenticatedUserRequest[A] => Future[Result]): Future[Result] = {
//        val authRequest = AuthenticatedUserRequest(
//          request,
//          None,
//          None,
//          Some("user@email.com"),
//          None,
//          if (hasCredId) Some("1234") else None,
//          None,
//          None,
//          nino = if (hasNino) Nino(hasNino = true, Some("AA000003D")) else Nino(hasNino = false, None)
//        )
//        block(authRequest)
//      }
//
//      override def parser: BodyParser[AnyContent] = mcc.parsers.defaultBodyParser
//
//      override protected def executionContext: ExecutionContext = ec
//    }
}
