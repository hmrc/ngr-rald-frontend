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
import uk.gov.hmrc.ngrraldfrontend.views.html.{LandlordView, WhatIsYourRentBasedOnView}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

import scala.collection.immutable.TreeMap
import scala.concurrent.Future

class WhatIsYourRentBasedOnControllerSpec extends ControllerSpecSupport {
  val pageTitle = "What is your rent based on?"
  val view: WhatIsYourRentBasedOnView = inject[WhatIsYourRentBasedOnView]
  val mockNGRCharacterCountComponent: NGRCharacterCountComponent = inject[NGRCharacterCountComponent]
  val controller: WhatIsYourRentBasedOnController = new WhatIsYourRentBasedOnController(view, mockAuthJourney, mockPropertyLinkingAction, mockRaldRepo, mockNGRCharacterCountComponent,  mcc)(mockConfig, ec)

  "What is your rent based on controller" must {
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
      "Return SEE_OTHER and the correct view when radio button selected" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.submit(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit)
          .withFormUrlEncodedBody(
            "rent-based-on-radio" -> "PercentageTurnover"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/what-type-of-agreement-do-you-have")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfAgreementController.show.url)
      }
      "Return SEE_OTHER and the correct view  when radio button selected Other and description has been entered" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.submit(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit)
          .withFormUrlEncodedBody(
            "rent-based-on-radio" -> "Other",
            "rent-based-on-other-desc" -> "The rent was agreed"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/what-type-of-agreement-do-you-have")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfAgreementController.show.url)
      }
      "Return Form with Errors when no name is input" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit)
          .withFormUrlEncodedBody(
            "landlord-name-value"-> "",
            "landlord-radio" ->  "LandLordAndTennant"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when no radio button is selected" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit)
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "Bob",
            "landlord-radio" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when other radio button is selected with no text" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit)
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "Bob",
            "landlord-radio" -> "OtherRelationship",
            "landlordOther" -> "",
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
          await(controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit)
            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}
