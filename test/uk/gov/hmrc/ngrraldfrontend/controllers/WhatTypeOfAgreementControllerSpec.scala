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

import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.WhatTypeOfAgreementPage
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatTypeOfAgreementView

import scala.concurrent.Future

class WhatTypeOfAgreementControllerSpec extends ControllerSpecSupport {
  val pageTitle = "What type of agreement do you have?"
  val view: WhatTypeOfAgreementView = inject[WhatTypeOfAgreementView]
  val controllerProperty: Option[UserAnswers] => WhatTypeOfAgreementController = answers => new WhatTypeOfAgreementController(view, fakeAuth, mcc, fakeDataProperty(Some(property),answers), mockNavigator, mockSessionRepository)(mockConfig, ec)
  val controllerNoProperty: WhatTypeOfAgreementController = new WhatTypeOfAgreementController(view, fakeAuth, mcc, fakeData(None), mockNavigator, mockSessionRepository)(mockConfig, ec)
  val whatTypeOfAgreementAnswers: Option[UserAnswers] = UserAnswers("id").set(WhatTypeOfAgreementPage, "Verbal").toOption



  "Tell us about your new agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers for Verbal" in {
        val result = controllerProperty(whatTypeOfAgreementAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result)
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=what-type-of-agreement-radio][value=Verbal]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=what-type-of-agreement-radio][value=Written]").hasAttr("checked") mustBe false
        document.select("input[type=radio][name=what-type-of-agreement-radio][value=LeaseOrTenancy]").hasAttr("checked") mustBe false
      }
      "Return NotFoundException when property is not found in the mongo" in {
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }

    "method submit" must {
      "Return OK and the correct view after submitting with written radio button" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatTypeOfAgreementController.submit(NormalMode))
          .withFormUrlEncodedBody(("what-type-of-agreement-radio", "Written"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/what-type-of-agreement-do-you-have ")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.AgreementController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting with verbal radio button" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatTypeOfAgreementController.submit(NormalMode))
          .withFormUrlEncodedBody(("what-type-of-agreement-radio", "Verbal"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/agreement-verbal")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.AgreementVerbalController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio button is selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatTypeOfAgreementController.submit(NormalMode))
          .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") shouldBe Some("/ngr-rald-frontend/what-type-of-agreement-do-you-have ")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Exception if no address is in the mongo" in {
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatTypeOfAgreementController.submit(NormalMode))
            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}