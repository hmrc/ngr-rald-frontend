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
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatYourRentIncludesView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

import scala.concurrent.Future

class WhatYourRentIncludesControllerSpec  extends ControllerSpecSupport {
  val pageTitle = "What your rent includes"
  val view:  WhatYourRentIncludesView = inject[ WhatYourRentIncludesView]
  val mockNGRCharacterCountComponent: NGRCharacterCountComponent = inject[NGRCharacterCountComponent]
  val controller:WhatYourRentIncludesController = new WhatYourRentIncludesController(
    view,
    mockAuthJourney,
    mockInputText,
    mockPropertyLinkingAction,
    mockRaldRepo,
    mcc)(mockConfig, ec)

  "Tell us about what your rent includes controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.show()(authenticatedFakeRequest())
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
      "Return OK and the correct view after submitting with all radio buttons selected" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatYourRentIncludesController.submit)
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "livingAccommodationYes",
            "rentPartAddressRadio" -> "No",
            "rentEmptyShellRadio" -> "Yes",
            "rentIncBusinessRatesRadio" -> "No",
            "rentIncWaterChargesRadio" -> "No",
            "rentIncServiceRadio" -> "Yes",
            "bedroomNumbers" -> "6"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/does-rent-include-parking-spaces-or-garages")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DoesYourRentIncludeParkingController.show.url)
      }
      "Return Form with Errors when no radio button is selected" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatYourRentIncludesController.submit)
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "",
            "rentPartAddressRadio" -> "No",
            "rentEmptyShellRadio" -> "Yes",
            "rentIncBusinessRatesRadio" -> "No",
            "rentIncWaterChargesRadio" -> "No",
            "rentIncServiceRadio" -> "Yes"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#livingAccommodationRadio\">Select yes if your rent includes any living accommodation</a>")
      }
      "Return Form with Errors when bedroom numbers is not provide" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatYourRentIncludesController.submit)
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "livingAccommodationYes",
            "rentPartAddressRadio" -> "No",
            "rentEmptyShellRadio" -> "Yes",
            "rentIncBusinessRatesRadio" -> "No",
            "rentIncWaterChargesRadio" -> "No",
            "rentIncServiceRadio" -> "Yes",
            "bedroomNumbers" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">Enter how many bedrooms the living accommodation has</a>")
      }
      "Return Form with Errors when bedroom numbers is not numeric" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatYourRentIncludesController.submit)
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "livingAccommodationYes",
            "rentPartAddressRadio" -> "No",
            "rentEmptyShellRadio" -> "Yes",
            "rentIncBusinessRatesRadio" -> "No",
            "rentIncWaterChargesRadio" -> "No",
            "rentIncServiceRadio" -> "Yes",
            "bedroomNumbers" -> "AS&"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">How many bedrooms must be a number, like 6</a>")
      }
      "Return Form with Errors when bedroom numbers is less than 1" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatYourRentIncludesController.submit)
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "livingAccommodationYes",
            "rentPartAddressRadio" -> "No",
            "rentEmptyShellRadio" -> "Yes",
            "rentIncBusinessRatesRadio" -> "No",
            "rentIncWaterChargesRadio" -> "No",
            "rentIncServiceRadio" -> "Yes",
            "bedroomNumbers" -> "-1"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">How many bedrooms must be 1 or more</a>")
      }
      "Return Form with Errors when bedroom numbers is greater than 99" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatYourRentIncludesController.submit)
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "livingAccommodationYes",
            "rentPartAddressRadio" -> "No",
            "rentEmptyShellRadio" -> "Yes",
            "rentIncBusinessRatesRadio" -> "No",
            "rentIncWaterChargesRadio" -> "No",
            "rentIncServiceRadio" -> "Yes",
            "bedroomNumbers" -> "100"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">How many bedrooms must be 99 or less</a>")
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatYourRentIncludesController.submit)
            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
