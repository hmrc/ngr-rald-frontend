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
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeStartView

import scala.collection.immutable.TreeMap
import scala.concurrent.Future

class RentDatesAgreeStartControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Rent dates"
  val view: RentDatesAgreeStartView = inject[RentDatesAgreeStartView]
  val mockDateTextFields: DateTextFields = inject[DateTextFields]
  val controller: RentDatesAgreeStartController = new RentDatesAgreeStartController(view, mockAuthJourney, mockPropertyLinkingAction, mockRaldRepo, mcc)(mockConfig, ec)

  "Rent Dates Agree Start controller" must {
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
      "Return SEE_OTHER and redirect WhatRentIncludes view when dates are provided" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.submit(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "agreedDate.day" -> "30",
            "agreedDate.month" -> "4",
            "agreedDate.year" -> "2025",
            "startPayingDate.day" -> "1",
            "startPayingDate.month" -> "6",
            "startPayingDate.year" -> "2025"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/what-rent-includes")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatYourRentIncludesController.show.url)
      }
      "Return Form with Errors when dates are missing" in {
        mockRequest()
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "agreedDate.day" -> "",
            "agreedDate.month" -> "",
            "agreedDate.year" -> "",
            "startPayingDate.day" -> "",
            "startPayingDate.month" -> "",
            "startPayingDate.year" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result).isEmpty mustBe true
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreedDate\">Enter the date you agreed your rent</a>")
        content must include("<a href=\"#startPayingDate\">Enter the date you will start paying rent</a>")
      }
      "Return Form with Errors when agreed and start paying dates are missing day" in {
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "agreedDate.day" -> "",
            "agreedDate.month" -> "4",
            "agreedDate.year" -> "2025",
            "startPayingDate.day" -> "",
            "startPayingDate.month" -> "6",
            "startPayingDate.year" -> "2025"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreedDate.day\">Date you agreed your rent must include a day</a>")
        content must include("<a href=\"#startPayingDate.day\">Date you will start paying rent must include a day</a>")
      }
      "Return Form with Errors when agreed and start paying dates are missing month" in {
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "agreedDate.day" -> "30",
            "agreedDate.month" -> "",
            "agreedDate.year" -> "2025",
            "startPayingDate.day" -> "30",
            "startPayingDate.month" -> "",
            "startPayingDate.year" -> "2025"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreedDate.month\">Date you agreed your rent must include a month</a>")
        content must include("<a href=\"#startPayingDate.month\">Date you will start paying rent must include a month</a>")
      }
      "Return Form with Errors when agreed and start paying dates are missing year" in {
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
          .withFormUrlEncodedBody(
            "agreedDate.day" -> "30",
            "agreedDate.month" -> "4",
            "agreedDate.year" -> "",
            "startPayingDate.day" -> "30",
            "startPayingDate.month" -> "6",
            "startPayingDate.year" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreedDate.year\">Date you agreed your rent must include a year</a>")
        content must include("<a href=\"#startPayingDate.year\">Date you will start paying rent must include a year</a>")
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeStartController.submit)
            .withFormUrlEncodedBody(
              "agreedDate.day" -> "30",
              "agreedDate.month" -> "4",
              "agreedDate.year" -> "2025",
              "startPayingDate.day" -> "1",
              "startPayingDate.month" -> "6",
              "startPayingDate.year" -> ""
            )
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
