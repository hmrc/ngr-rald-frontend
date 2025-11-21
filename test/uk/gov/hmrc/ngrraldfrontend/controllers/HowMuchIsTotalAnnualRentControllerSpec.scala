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
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.{NewAgreement, RenewedAgreement}
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.{HowMuchIsTotalAnnualRentPage, TellUsAboutYourNewAgreementPage, TellUsAboutYourRenewedAgreementPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.HowMuchIsTotalAnnualRentView

import scala.concurrent.Future


class HowMuchIsTotalAnnualRentControllerSpec extends ControllerSpecSupport {
  val pageTitle = "How much is your total annual rent?"
  val view: HowMuchIsTotalAnnualRentView = inject[HowMuchIsTotalAnnualRentView]
  val controllerNoProperty: HowMuchIsTotalAnnualRentController = new HowMuchIsTotalAnnualRentController(view, mockAuthJourney, fakeData(None), mockCheckRequestSentReference, mockSessionRepository, mockNavigator, mcc)(mockConfig)
  val controllerProperty: HowMuchIsTotalAnnualRentController = new HowMuchIsTotalAnnualRentController(view, mockAuthJourney, fakeDataProperty(Some(property),None), mockCheckRequestSentReference, mockSessionRepository, mockNavigator, mcc)(mockConfig)
  lazy val howMuchIsTotalAnnualRentAnswers: Option[UserAnswers] = userAnswersWithoutData.set(HowMuchIsTotalAnnualRentPage, BigDecimal(1234.67)).toOption
  lazy val filledController: Option[UserAnswers] => HowMuchIsTotalAnnualRentController = answers => HowMuchIsTotalAnnualRentController(
    view, mockAuthJourney, fakeDataProperty(Some(property), answers), mockCheckRequestSentReference, mockSessionRepository, mockNavigator, mcc
  )

  "TypeOfLeaseRenewalController" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty.show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct with prepopulated answers" in {
        val result = filledController(howMuchIsTotalAnnualRentAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=how–much–is–total–annual–rent-value]").attr("value") mustBe "1234.67"
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
      "Return OK and the correct view if its a renewedAgreement" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest =  FakeRequest(routes.HowMuchIsTotalAnnualRentController.submit(NormalMode))
          .withFormUrlEncodedBody(("how–much–is–total–annual–rent-value", "10000"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = filledController(renewedAgreementAnswers).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DidYouAgreeRentWithLandlordController.show(NormalMode).url)
      }
      "Return OK and the correct view if its a newAgreement" in {

        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.HowMuchIsTotalAnnualRentController.submit(NormalMode))
          .withFormUrlEncodedBody(("how–much–is–total–annual–rent-value", "10000"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = filledController(newAgreementAnswers).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.CheckRentFreePeriodController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for missing input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.HowMuchIsTotalAnnualRentController.submit(NormalMode))
          .withFormUrlEncodedBody(("how–much–is–total–annual–rent-value", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return Exception if no address is in the mongo" in {
        val fakePostRequest = FakeRequest(routes.WhatTypeOfLeaseRenewalController.submit(NormalMode))
          .withFormUrlEncodedBody(("how–much–is–total–annual–rent-value", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest)))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
