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
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.forms.CheckRentFreePeriodForm
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.CheckRentFreePeriodPage
import uk.gov.hmrc.ngrraldfrontend.views.html.CheckRentFreePeriodView

import scala.concurrent.Future

class CheckRentFreePeriodControllerSpec extends ControllerSpecSupport{
  val pageTitle = "Do you have a rent-free period at the start of your agreement?"
  val view: CheckRentFreePeriodView = inject[CheckRentFreePeriodView]
  val controllerNoProperty : CheckRentFreePeriodController = new CheckRentFreePeriodController(view, mockAuthJourney, fakeData(None), mockCheckRequestSentReference, mockNavigator, mockSessionRepository, mcc)(mockConfig)
  val controllerProperty : Option[UserAnswers] => CheckRentFreePeriodController = answers => new CheckRentFreePeriodController(view, mockAuthJourney, fakeDataProperty(Some(property), answers), mockCheckRequestSentReference, mockNavigator, mockSessionRepository, mcc)(mockConfig)
  val checkRentFreePeriodAnswers: Option[UserAnswers] = userAnswersWithoutData.set(CheckRentFreePeriodPage, true).toOption

  "CheckRentFreePeriodController" when {
    "calling show method" should {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "return OK and the correct view with prepopulated data" in {
        val result = controllerProperty(checkRentFreePeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=check-rent-period-radio][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=check-rent-period-radio][value=false]").hasAttr("checked") mustBe false
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
        val fakePostRequest = FakeRequest(routes.CheckRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody((CheckRentFreePeriodForm.checkRentPeriodRadio, "true"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentFreePeriodController.show(NormalMode).url)
      }
      "Return OK and the correct view and the answer is No" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.CheckRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody((CheckRentFreePeriodForm.checkRentPeriodRadio, "false"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentDatesAgreeStartController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for missing input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody((CheckRentFreePeriodForm.checkRentPeriodRadio, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return Exception if no address is in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody((CheckRentFreePeriodForm.checkRentPeriodRadio, ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest)))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
