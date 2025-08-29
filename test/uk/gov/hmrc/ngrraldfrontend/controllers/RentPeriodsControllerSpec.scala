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
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.HeaderNames
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, ProvideDetailsOfFirstSecondRentPeriod, RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentPeriodView

import scala.concurrent.Future

class RentPeriodsControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Rent periods"
  val view: RentPeriodView = inject[RentPeriodView]
  val controller: RentPeriodsController = new RentPeriodsController(view, mockAuthJourney, mockPropertyLinkingAction, mockRaldRepo, mcc)(mockConfig, ec)

  "Tell us about your new agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
    }

    "method show" must {
      "Return OK and the correct view when the user has said yes to having paid rent for the first period" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn Future.successful(Some(RaldUserAnswers(
          credId = CredId(null),
          agreementType = NewAgreement,
          selectedProperty = property,
          provideDetailsOfFirstSecondRentPeriod =
            Some(ProvideDetailsOfFirstSecondRentPeriod(
              firstDateStart = "2016-12-12",
              firstDateEnd = "2017-12-12",
              firstRentPeriodRadio = true,
              firstRentPeriodAmount = Some("10000"),
              secondDateStart = "2018-12-12",
              secondDateEnd = "2019-12-12",
              secondHowMuchIsRent = "10000")))))
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
    }

    "method submit" must {
      "Return OK and the correct view after submitting yes" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.RentPeriodsController.submit)
          .withFormUrlEncodedBody(
            "rent-periods-radio" -> "Yes"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfAgreementController.show.url)
      }
      "Return OK and the correct view after submitting no" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit)
          .withFormUrlEncodedBody(
            "rent-periods-radio" -> "No"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfAgreementController.show.url)
      }
      "Return Form with Errors when no name is input" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit)
          .withFormUrlEncodedBody(
            "rent-periods-radio" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
    }
  }
}

