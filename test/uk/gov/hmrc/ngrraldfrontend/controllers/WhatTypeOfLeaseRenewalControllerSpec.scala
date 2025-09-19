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
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.NormalMode
import uk.gov.hmrc.ngrraldfrontend.models.forms.WhatTypeOfLeaseRenewalForm
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatTypeOfLeaseRenewalView

import scala.concurrent.Future

class WhatTypeOfLeaseRenewalControllerSpec extends ControllerSpecSupport {
  val pageTitle = "What type of lease renewal is it?"
  val view: WhatTypeOfLeaseRenewalView = inject[WhatTypeOfLeaseRenewalView]
  val controllerNoProperty: WhatTypeOfLeaseRenewalController = new WhatTypeOfLeaseRenewalController(view, fakeAuth, fakeData(None),mockSessionRepository,mockNavigator, mcc)(mockConfig)
  val controllerProperty: WhatTypeOfLeaseRenewalController = new WhatTypeOfLeaseRenewalController(view, fakeAuth, fakeDataProperty(Some(property),None),mockSessionRepository,mockNavigator, mcc)(mockConfig)

  "TypeOfLeaseRenewalController" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty.show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return NotFoundException when property is not found in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }

    "method submit" must {
      "Return OK and the correct view for Renewed Agreement" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest =  FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody((WhatTypeOfLeaseRenewalForm.formName, "RenewedAgreement"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.LandlordController.show(NormalMode).url)
      }
      "Return OK and the correct view for " in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody((WhatTypeOfLeaseRenewalForm.formName, "SurrenderAndRenewal"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.LandlordController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for missing input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody((WhatTypeOfLeaseRenewalForm.formName, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return Exception if no address is in the mongo" in {
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit((NormalMode)))
          .withFormUrlEncodedBody((WhatTypeOfLeaseRenewalForm.formName, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest)))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}