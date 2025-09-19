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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, ProvideDetailsOfFirstSecondRentPeriod, RaldUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.ProvideDetailsOfFirstSecondRentPeriodPage
import uk.gov.hmrc.ngrraldfrontend.views.html.RentPeriodView

import scala.concurrent.Future

class RentPeriodsControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Rent periods"
  val view: RentPeriodView = inject[RentPeriodView]
  val controllerNoProperty: RentPeriodsController = new RentPeriodsController(view, fakeAuth, fakeData(None),mcc, mockSessionRepository, mockNavigator)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => RentPeriodsController = answers => new RentPeriodsController(view, fakeAuth, fakeDataProperty(Some(property), answers), mcc, mockSessionRepository, mockNavigator)(mockConfig, ec)

  lazy val firstSecondRentPeriodAnswers: Option[UserAnswers] = UserAnswers("id").set(ProvideDetailsOfFirstSecondRentPeriodPage, firstSecondRentPeriod).toOption

  "method show" must {
    "Return OK and the correct view when the user has said yes to having paid rent for the first period" in {
      val result = controllerProperty(firstSecondRentPeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
      status(result) mustBe OK
      val content = contentAsString(result)
      content must include(pageTitle)
    }
  }
    "method submit" must {
      "Return OK and the correct view after submitting yes" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentPeriodsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-periods-radio" -> "Yes"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DidYouAgreeRentWithLandlordController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting no" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentPeriodsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-periods-radio" -> "No"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ProvideDetailsOfFirstSecondRentPeriodController.show(NormalMode).url)
      }
      "Return Form with Errors when no name is input" in {
        val result = controllerProperty(firstSecondRentPeriodAnswers).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentPeriodsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-periods-radio" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
    }
  }
