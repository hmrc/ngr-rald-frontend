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
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.models.forms.{AgreedRentChangeForm, WhatTypeOfLeaseRenewalForm}
import uk.gov.hmrc.ngrraldfrontend.views.html.{AgreedRentChangeView, WhatTypeOfLeaseRenewalView}
import uk.gov.hmrc.ngrraldfrontend.models.forms.AgreedRentChangeForm
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreedRentChangeView
import play.api.mvc.{AnyContentAsEmpty, AnyContentAsFormUrlEncoded}
import uk.gov.hmrc.auth.core.{ConfidenceLevel, Nino}
import uk.gov.hmrc.auth.core.ConfidenceLevel.L200
import uk.gov.hmrc.ngrraldfrontend.models.vmvProperty.VMVProperty

import scala.concurrent.Future

class AgreedRentChangeControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Have you agreed in advance with the landlord when and by how much rent goes up?"
  val view: AgreedRentChangeView = inject[AgreedRentChangeView]
  val controllerNoProperty = new AgreedRentChangeController(view, fakeAuth, fakeData(None), mockSessionRepository, navigator, mcc)(mockConfig)
  val controllerProperty = new AgreedRentChangeController(view, fakeAuth, fakeDataProperty(Some(property), None), mockSessionRepository, navigator, mcc)(mockConfig)

  "AgreedRentChangeController" must {
    "method show" should {
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
      "Return SEE and the correct view after submitting Yes" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest =  FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, "Yes"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit(NormalMode)(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ProvideDetailsOfFirstSecondRentPeriodController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting No" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.AgreedRentChangeController.submit(NormalMode))
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, "No"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchIsTotalAnnualRentController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for missing input and the correct view" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.AgreedRentChangeController.submit(NormalMode))
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controller.submit(NormalMode)(authenticatedFakeRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return Exception if no address is in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val fakePostRequest = FakeRequest(routes.AgreedRentChangeController.submit(NormalMode))
          .withFormUrlEncodedBody((AgreedRentChangeForm.agreedRentChangeRadio, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest)))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
