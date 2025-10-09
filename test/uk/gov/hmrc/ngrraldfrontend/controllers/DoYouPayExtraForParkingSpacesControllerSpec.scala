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
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.DoYouPayExtraForParkingSpacesPage
import uk.gov.hmrc.ngrraldfrontend.views.html.DoYouPayExtraForParkingSpacesView

import scala.concurrent.Future

class DoYouPayExtraForParkingSpacesControllerSpec extends ControllerSpecSupport{
  val pageTitle = "Do you pay extra for parking spaces or garages that are not included in your rent?"
  val view: DoYouPayExtraForParkingSpacesView = inject[DoYouPayExtraForParkingSpacesView]
  val controllerNoProperty : DoYouPayExtraForParkingSpacesController = new DoYouPayExtraForParkingSpacesController(view,fakeAuth, fakeData(None), mockNavigator, mockSessionRepository, mcc)(mockConfig)
  val controllerProperty : Option[UserAnswers] => DoYouPayExtraForParkingSpacesController = answers => new DoYouPayExtraForParkingSpacesController(view,fakeAuth, fakeDataProperty(Some(property), answers), mockNavigator, mockSessionRepository, mcc)(mockConfig)
  val doYouPayExtraForParkingSpacesAnswers: Option[UserAnswers] = UserAnswers("id").set(DoYouPayExtraForParkingSpacesPage, true).toOption

  "DoYouPayExtraForParkingSpacesController" when {
    "calling show method" should {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "return OK and the correct view with prepopulated data" in {
        val result = controllerProperty(doYouPayExtraForParkingSpacesAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=payExtra][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=payExtra][value=false]").hasAttr("checked") mustBe false
      }
      "Return Not Found Exception where no property is found in mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
    "method submit" must {
      "Return OK and the correct view and the answer is Yes" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.DoYouPayExtraForParkingSpacesController.submit(NormalMode))
          .withFormUrlEncodedBody("payExtra" -> "true")
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(NormalMode).url)
      }
      "Return OK and the correct view and the answer is No" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.CheckRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody("payExtra" -> "false")
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RepairsAndInsuranceController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for missing input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody("payExtra" -> "")
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#payExtra\">Select yes if your rent includes extra parking spaces or garages</a>")
      }
      "Return Exception if no address is in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody("payExtra" -> "")
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest)))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
