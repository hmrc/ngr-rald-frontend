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

import play.api.test.FakeRequest
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.forms.{AgreedRentChangeForm, WhatTypeOfLeaseRenewalForm}
import uk.gov.hmrc.ngrraldfrontend.views.html.{AgreedRentChangeView, WhatTypeOfLeaseRenewalView}

class AgreedRentChangeControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Have you agreed in advance with the landlord when and by how much rent goes up?"
  val view: AgreedRentChangeView = inject[AgreedRentChangeView]
  val controller: AgreedRentChangeController = new AgreedRentChangeController(view, mockAuthJourney, mockPropertyLinkingAction, mockRaldRepo, mcc)(mockConfig)

  "TypeOfLeaseRenewalController" must {
    "method show" must {
      "Return OK and the correct view" in {
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
      "Return OK and the correct view after submitting Yes" in {
        val fakePostRequest =  FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit)
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, "Yes"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ProvideDetailsOfFirstSecondRentPeriodController.show.url)
      }
      "Return OK and the correct view after submitting No" in {
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit)
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, "No"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfLeaseRenewalController.show.url)
      }
      "Return BAD_REQUEST for missing input and the correct view" in {
        mockRequest()
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit)
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit)
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controller.submit()(authenticatedFakeRequest(fakePostRequest)))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
