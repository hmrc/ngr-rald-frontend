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

import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.views.html.HowManyParkingSpacesOrGaragesIncludedInRentView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

class HowManyParkingSpacesOrGaragesIncludedInRentControllerSpec extends ControllerSpecSupport {
  val pageTitle = "How many parking spaces or garages are included in your rent?"
  val view: HowManyParkingSpacesOrGaragesIncludedInRentView = inject[HowManyParkingSpacesOrGaragesIncludedInRentView]
  val controller: HowManyParkingSpacesOrGaragesIncludedInRentController = new HowManyParkingSpacesOrGaragesIncludedInRentController(
    howManyParkingSpacesOrGaragesIncludedInRentView = view,
    authenticate = mockAuthJourney,
    inputText = mockInputText,
    hasLinkedProperties = mockPropertyLinkingAction,
    raldRepo = mockRaldRepo,
    mcc = mcc)(mockConfig)

  " HowManyParkingSpacesOrGaragesIncludedInRentController" must {
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
        val fakePostRequest =  FakeRequest(routes.HowManyParkingSpacesOrGaragesIncludedInRentController.submit)
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "1",
            "coveredSpaces" -> "0",
            "garages" -> "0"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CheckRentFreePeriodController.show.url)
      }
      "Return BAD_REQUEST for inputting 0 in all fields and the correct view" in {
        mockRequest()
        val fakePostRequest =  FakeRequest(routes.HowManyParkingSpacesOrGaragesIncludedInRentController.submit)
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "0",
            "coveredSpaces" -> "0",
            "garages" -> "0"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val fakePostRequest = FakeRequest(routes.HowManyParkingSpacesOrGaragesIncludedInRentController.submit)
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "",
            "coveredSpaces" -> "0",
            "garages" -> "0"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controller.submit()(authenticatedFakeRequest(fakePostRequest)))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
