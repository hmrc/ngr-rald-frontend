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
import play.api.test.FakeRequest
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import scala.concurrent.Future

class RentDatesAgreeControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Have you agreed in advance with the landlord when and by how much rent goes up?"
  val view: RentDatesAgreeView = inject[RentDatesAgreeView]
  val mockInputText: InputText = inject[InputText]
  val controller: RentDatesAgreeController = new RentDatesAgreeController(
    view,
    mockAuthJourney,
    mcc,
    fakeData(None),
    navigator,
    mockSessionRepository
  )(mockConfig, ec)

  "Agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.show(NormalMode)(authenticatedFakeRequest())
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
      "Return OK and the correct view after submitting with first start date, first end date no radio button selected for first rent period" +
        "and second rent date start, end and amount is added" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rentDatesAgreeInput.day" -> "12",
            "rentDatesAgreeInput.month" -> "12",
            "rentDatesAgreeInput.year" -> "2026",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-type-of-lease-renewal-is-it")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfLeaseRenewalController.show(NormalMode).url)
      }
      "Return Form with Errors when no day is added" in {
        mockRequest(hasCredId = true)
        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rentDatesAgreeInput.day" -> "",
            "rentDatesAgreeInput.month" -> "12",
            "rentDatesAgreeInput.year" -> "2026",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when no month is added" in {
        mockRequest(hasCredId = true)
        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rentDatesAgreeInput.day" -> "12",
            "rentDatesAgreeInput.month" -> "",
            "rentDatesAgreeInput.year" -> "2026",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when no year is added" in {
        mockRequest(hasCredId = true)
        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rentDatesAgreeInput.day" -> "12",
            "rentDatesAgreeInput.month" -> "12",
            "rentDatesAgreeInput.year" -> "",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
