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
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatTypeOfAgreementView
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId

import scala.concurrent.Future

class WhatTypeOfAgreementControllerSpec extends ControllerSpecSupport {
  val pageTitle = "What type of agreement do you have?"
  val view: WhatTypeOfAgreementView = inject[WhatTypeOfAgreementView]
  val controller: WhatTypeOfAgreementController = new WhatTypeOfAgreementController(view, mockAuthJourney, mockIsRegisteredCheck, mockRaldRepo, mcc)(mockConfig, ec)

  "Tell us about your new agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockRaldRepo.findByCredId(any()))thenReturn(Future.successful(Some(RaldUserAnswers(credId = CredId(null), selectedProperty = property))))
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Redirect to dashboard if no properties are in the mongo" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(None))
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe SEE_OTHER
      }
    }

    "method submit" must {
      "Return OK and the correct view after submitting with written radio button" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatTypeOfAgreementController.submit)
          .withFormUrlEncodedBody(("what-type-of-agreement-radio", "Written"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/what-type-of-agreement-do-you-have ")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.WhatTypeOfAgreementController.show.url)
      }
      "Return Form with Errors when no radio button is selected" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatTypeOfAgreementController.submit)
          .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/what-type-of-agreement-do-you-have ")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return to dashboard if no property is in the mongo" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(None))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.WhatTypeOfAgreementController.submit)
          .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/what-type-of-agreement-do-you-have ")
        })
        status(result) mustBe SEE_OTHER
      }
    }
  }
}