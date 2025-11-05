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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, headers, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.WhatIsYourRentBasedOnPage
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatIsYourRentBasedOnView

import scala.collection.immutable.TreeMap
import scala.concurrent.Future

class WhatIsYourRentBasedOnControllerSpec extends ControllerSpecSupport {
  val pageTitle = "What is your rent based on?"
  val view: WhatIsYourRentBasedOnView = inject[WhatIsYourRentBasedOnView]
  val controllerProperty: Option[UserAnswers] => WhatIsYourRentBasedOnController = answers => new WhatIsYourRentBasedOnController(view, fakeAuth, mockNGRCharacterCountComponent, mcc, fakeDataProperty(Some(property),answers), mockNavigator, mockSessionRepository)(mockConfig, ec)
  val controllerNoProperty: WhatIsYourRentBasedOnController = new WhatIsYourRentBasedOnController(view, fakeAuth, mockNGRCharacterCountComponent, mcc, fakeData(None), mockNavigator, mockSessionRepository)(mockConfig, ec)
  val whatIsYourRentBasedOnAnswers: Option[UserAnswers] = userAnswers.set(WhatIsYourRentBasedOnPage, rentBasedOnModel).toOption

  "What is your rent based on controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated data" in {
        val result = controllerProperty(whatIsYourRentBasedOnAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=rent-based-on-radio][value=Other]").hasAttr("checked") mustBe true
        document.select("textarea[name=rent-based-on-other-desc]").text() mustBe "The rent was agreed"

      }
      "Return NotFoundException when property is not found in the mongo" in {
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }

    "method submit" must {
      "Return SEE_OTHER and redirect HowMuchIsTotalAnnualRent view when radio button selected PercentageTurnover" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-based-on-radio" -> "PercentageTurnover"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/how-much-is-total-annual-rent")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchIsTotalAnnualRentController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view  when radio button selected Other and description has been entered" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-based-on-radio" -> "Other",
            "rent-based-on-other-desc" -> "The rent was agreed"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/have-you-agreed-rent-changes-with-landlord")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AgreedRentChangeController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio button is selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-based-on-radio" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result).isEmpty mustBe true
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("Select what your rent is based on")
      }
      "Return Form with Errors when radio button Other is selected but no other description is given" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-based-on-radio" -> "Other",
            "rent-based-on-other-desc" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("Tell us how your rent was agreed")
      }
      "Return Form with Errors when radio button Other is selected but other description is over 250 characters" in {
        val over250Characters = "Bug Me Not PVT LTD, RODLEY LANE, RODLEY, LEEDS, BH1 1HU What is your rent based on?Open market value This is the rent a landlord could rent the property for if, it was available to anyoneA percentage of open market value This is a percentage of the rent a landlord could rent the property for if, it was available to anyoneTurnover top-up The rent is a fixed base rent with an additional payment based on a percentage of your turnoverA percentage of expected turnover The rent paid is based on a percentage of turnoverTotal Occupancy Cost leases (TOCs)The rent is the total cost of leasing the property. It includes base rent, business rates, insurance and utilities. It also includes common area maintenance and tenant improvements Indexation The rent is reviewed according to an index (such as Retail Price Index)Other The rent was agreed another way Can you tell us how your rent was agreed?"
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "rent-based-on-radio" -> "Other",
            "rent-based-on-other-desc" -> over250Characters
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("Maximum character allowed is 250")
      }
      "Return Exception if no address is in the mongo" in {
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatIsYourRentBasedOnController.submit(NormalMode))
            .withFormUrlEncodedBody(("rent-based-on-radio" -> "Other"))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
