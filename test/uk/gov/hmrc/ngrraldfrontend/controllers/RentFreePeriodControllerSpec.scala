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
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, headers, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentFreePeriodView

import scala.collection.immutable.TreeMap
import scala.concurrent.Future

class RentFreePeriodControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Rent-free period"
  val view: RentFreePeriodView = inject[RentFreePeriodView]
  val controller: RentFreePeriodController = new RentFreePeriodController(view, mockAuthJourney, mockPropertyLinkingAction, mockRaldRepo, mcc)(mockConfig, ec)

  "Rent free period controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.show(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return NotFoundException when property is not found in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.show(authenticatedFakeRequest()))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }

    "method submit" must {
      "Return SEE_OTHER and redirect RentDatesAgreeStart view when everything is provided" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.submit(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "rentFreePeriodMonths" -> "5",
            "reasons" -> "Any reasons"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/rent-dates-agree-start")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentDatesAgreeStartController.show.url)
      }
      "Return Form with Errors when rentFreePeriodMonths is missing" in {
        mockRequest()
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "rentFreePeriodMonths" -> "",
            "reasons" -> "Any reasons"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result).isEmpty mustBe true
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#rentFreePeriodMonths\">Enter how many months the rent-free period is</a>")
      }
      "Return Form with Errors when rentFreePeriodMonths isn't numeric" in {
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "rentFreePeriodMonths" -> "$A,",
            "reasons" -> "Any reasons"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#rentFreePeriodMonths\">Rent-free period must be a number, like 6</a>")
      }
      "Return Form with Errors when rentFreePeriodMonths is over 999" in {
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "rentFreePeriodMonths" -> "1000",
            "reasons" -> "Any reasons"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#rentFreePeriodMonths\">Rent-free period must be 99 months or less</a>")
      }
      "Return Form with Errors when rentFreePeriodMonths is less than 1" in {
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "rentFreePeriodMonths" -> "0",
            "reasons" -> "Any reasons"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#rentFreePeriodMonths\">Rent-free period must be more more than 0</a>")
      }
      "Return Form with Errors when reasons is missing" in {
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "rentFreePeriodMonths" -> "5",
            "reasons" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#reasons\">Tell us why you have a rent-free period</a>")
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
            .withFormUrlEncodedBody(
              "rentFreePeriodMonths" -> "5",
              "reasons" -> ""
            )
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
