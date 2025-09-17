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


import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.views.html.InterimRentSetByTheCourtView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

class InterimRentSetByTheCourtControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Interim rent set by the court"
  val view: InterimRentSetByTheCourtView = inject[InterimRentSetByTheCourtView]
  val mockInputText: InputText = inject[InputText]
  val controller: InterimRentSetByTheCourtController = new InterimRentSetByTheCourtController(
    interimRentSetByTheCourtView = view,
    authenticate = mockAuthJourney,
    hasLinkedProperties = mockPropertyLinkingAction,
    inputText = mockInputText,
    raldRepo = mockRaldRepo,
    mcc = mcc)(mockConfig)

  "InterimRentSetByTheCourtController" must {
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
        val fakePostRequest =  FakeRequest(routes.InterimRentSetByTheCourtController.submit)
          .withFormUrlEncodedBody(
            "interimAmount" -> "10000",
            "date.month" -> "1",
            "date.year" -> "1990"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CheckRentFreePeriodController.show.url)
      }
      "Return BAD_REQUEST for missing how much input and the correct view" in {
        mockRequest()
        val fakePostRequest = FakeRequest(routes.InterimRentSetByTheCourtController.submit)
          .withFormUrlEncodedBody(
            "interimAmount" -> "",
            "date.month" -> "1",
            "date.year" -> "1990"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Enter how much the interim rent was, in pounds")
      }
      "Return BAD_REQUEST for missing month input and the correct view" in {
        mockRequest()
        val fakePostRequest = FakeRequest(routes.InterimRentSetByTheCourtController.submit)
          .withFormUrlEncodedBody(
            "interimAmount" -> "1000",
            "date.month" -> "",
            "date.year" -> "1990"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Date you started paying the interim rent must include a month")
      }
      "Return BAD_REQUEST for missing year input and the correct view" in {
        mockRequest()
        val fakePostRequest = FakeRequest(routes.InterimRentSetByTheCourtController.submit)
          .withFormUrlEncodedBody(
            "interimAmount" -> "1000",
            "date.month" -> "1",
            "date.year" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Date you started paying the interim rent must include a year")
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val fakePostRequest = FakeRequest(routes.InterimRentSetByTheCourtController.submit)
          .withFormUrlEncodedBody(("how–much–is–total–annual–rent-value", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controller.submit()(authenticatedFakeRequest(fakePostRequest)))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
