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

import play.api.http.Status.{BAD_REQUEST, NOT_IMPLEMENTED, OK}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, status}
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.forms.TypeOfLeaseRenewalForm
import uk.gov.hmrc.ngrraldfrontend.views.html.TypeOfLeaseRenewalView

class TypeOfLeaseRenewalControllerSpec extends ControllerSpecSupport {
  val pageTitle = "What type of lease renewal is it?"
  val view: TypeOfLeaseRenewalView = inject[TypeOfLeaseRenewalView]
  val controller: TypeOfLeaseRenewalController = new TypeOfLeaseRenewalController(view, mockAuthJourney, mockIsRegisteredCheck, mcc)(mockConfig)

  "TypeOfLeaseRenewalController" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
    }

    "method submit" must {
      "Return OK and the correct view" in {
        val fakePostRequest =  FakeRequest(routes.TypeOfLeaseRenewalController.submit)
          .withFormUrlEncodedBody((TypeOfLeaseRenewalForm.formName, "RenewedAgreement"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        
        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe NOT_IMPLEMENTED
      }

      "Return BAD_REQUEST for missing input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.TypeOfLeaseRenewalController.submit)
          .withFormUrlEncodedBody((TypeOfLeaseRenewalForm.formName, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit()(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
    }
  }
}