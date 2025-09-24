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

import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.views.html.RepairsAndInsuranceView
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}

class RepairsAndInsuranceControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Repairs and insurance"

  val view: RepairsAndInsuranceView = inject[RepairsAndInsuranceView]
  val controller: RepairsAndInsuranceController = new RepairsAndInsuranceController(
    repairsAndInsuranceView = view,
    authenticate = mockAuthJourney,
    hasLinkedProperties = mockPropertyLinkingAction,
    raldRepo = mockRaldRepo,
    mcc = mcc)(mockConfig)

  "RepairsAndInsuranceController" must {
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
      "Return OK and the correct view" in {
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit)
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "InternalRepairsYou",
            "repairsAndInsurance-externalRepairs-radio-value" -> "ExternalRepairsYou",
            "repairsAndInsurance-buildingInsurance-radio-value" -> "BuildingInsuranceYou"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.InterimRentSetByTheCourtController.show.url)
      }
      "Return BAD_REQUEST for not selecting a internal repairs radio, showing the correct view and error" in {
        mockRequest()
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit)
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "",
            "repairsAndInsurance-externalRepairs-radio-value" -> "ExternalRepairsYou",
            "repairsAndInsurance-buildingInsurance-radio-value" -> "BuildingInsuranceYou"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Select who pays for internal repairs")
      }
      "Return BAD_REQUEST for not selecting a external repairs radio, showing the correct view and error" in {
        mockRequest()
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit)
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "InternalRepairsYou",
            "repairsAndInsurance-externalRepairs-radio-value" -> "",
            "repairsAndInsurance-buildingInsurance-radio-value" -> "BuildingInsuranceYou"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Select who pays for external repairs")
      }
      "Return BAD_REQUEST for not selecting a building insurance radio, showing the correct view and error" in {
        mockRequest()
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit)
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "InternalRepairsYou",
            "repairsAndInsurance-externalRepairs-radio-value" -> "ExternalRepairsYou",
            "repairsAndInsurance-buildingInsurance-radio-value" -> ""
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Select who pays for buildings insurance")
      }
    }
  }
}
