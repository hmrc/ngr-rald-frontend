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
import uk.gov.hmrc.govukfrontend.views.html.components.GovukRadios
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewDetailsForm
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, RentReviewDetails, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{DidYouAgreeRentWithLandlordPage, RentReviewDetailsPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputDateForMonthYear
import uk.gov.hmrc.ngrraldfrontend.views.html.{DidYouAgreeRentWithLandlordView, RentReviewDetailsView}

import scala.concurrent.Future

class RentReviewDetailsControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Rent review"
  val view: RentReviewDetailsView = inject[RentReviewDetailsView]
  val govukRadios: GovukRadios = inject[GovukRadios]
  val controllerNoProperty: RentReviewDetailsController = new RentReviewDetailsController(
    view, fakeAuth, fakeData(None), govukRadios, mockNavigator, mockSessionRepository, mcc)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => RentReviewDetailsController = answers => new RentReviewDetailsController(
    view, fakeAuth, fakeDataProperty(Some(property), answers), govukRadios, mockNavigator, mockSessionRepository, mcc)(mockConfig, ec)
  val rentReviewDetailsAnswers: Option[UserAnswers] = userAnswers.set(RentReviewDetailsPage, RentReviewDetails(BigDecimal("3000"), "OnlyGoUp", "2020-10-30", false, Some("IndependentExpert"))).toOption

  "Rent review details controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(rentReviewDetailsAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=text][name=annualAmount]").attr("value") mustBe "3000"
        document.select("input[type=radio][name=what-happens-at-rent-review-radio][value=GoUpOrDown]").hasAttr("checked") mustBe false
        document.select("input[type=radio][name=what-happens-at-rent-review-radio][value=OnlyGoUp]").hasAttr("checked") mustBe true
        document.select("input[type=text][name=startDate.day]").attr("value") mustBe "30"
        document.select("input[type=text][name=startDate.month]").attr("value") mustBe "10"
        document.select("input[type=text][name=startDate.year]").attr("value") mustBe "2020"
        document.select("input[type=radio][name=has-agreed-new-rent-radio][value=true]").hasAttr("checked") mustBe false
        document.select("input[type=radio][name=has-agreed-new-rent-radio][value=false]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=who-agreed-radio][value=Arbitrator]").hasAttr("checked") mustBe false
        document.select("input[type=radio][name=who-agreed-radio][value=IndependentExpert]").hasAttr("checked") mustBe true
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
      "Return SEE_OTHER and the correct view after submitting with who agreed new rent" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "3000",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
            "startDate.day" -> "30",
            "startDate.month" -> "10",
            "startDate.year" -> "2020",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> "false",
            RentReviewDetailsForm.whoAgreedRadio -> "Arbitrator"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.WhatIsYourRentBasedOnController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view after submitting without who agreed new rent" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "3000",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
            "startDate.day" -> "30",
            "startDate.month" -> "10",
            "startDate.year" -> "2020",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.WhatIsYourRentBasedOnController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio buttons are selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "3000",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "",
            "startDate.day" -> "30",
            "startDate.month" -> "10",
            "startDate.year" -> "2020",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include(s"<a href=\"#${RentReviewDetailsForm.whatHappensAtRentReviewRadio}\">Select what your agreement said can happen at the rent review</a>")
        content must include(s"<a href=\"#${RentReviewDetailsForm.hasAgreedNewRentRadio}\">Select yes if you and the landlord (or their agent) agreed the new rent</a>")
      }
      "Return Form with Errors when annual rent amount is not entered" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
            "startDate.day" -> "30",
            "startDate.month" -> "10",
            "startDate.year" -> "2020",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include(s"<a href=\"#${RentReviewDetailsForm.annualAmount}\">Enter your new total annual rent, in pounds</a>")
      }
      "Return Form with Errors when who agreed new rent is not selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "3000",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
            "startDate.day" -> "30",
            "startDate.month" -> "10",
            "startDate.year" -> "2020",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> "false",
            RentReviewDetailsForm.whoAgreedRadio -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include(s"<a href=\"#${RentReviewDetailsForm.whoAgreedRadio}\">Select who agreed the new rent</a>")
      }
      "Return Form with Errors when month and year are missing" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "3000",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
            "startDate.day" -> "30",
            "startDate.month" -> "",
            "startDate.year" -> "",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#startDate.month\">Date you will start paying rent must include a month and year</a>")
      }
      "Return Form with Errors when start date is before 1900" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "3000",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
            "startDate.day" -> "30",
            "startDate.month" -> "10",
            "startDate.year" -> "1899",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#startDate.year\">Year you will start paying rent must be 1900 or after</a>")
      }
      "Return Form with Errors when start date is not numeric" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewDetailsForm.annualAmount -> "3000",
            RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
            "startDate.day" -> "AS",
            "startDate.month" -> "-20",
            "startDate.year" -> "2899",
            RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#startDate.day\">Date you will start paying rent must be a real date</a>")
      }
      "Return Exception if no address is in the mongo" in {
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewDetailsController.submit(NormalMode))
            .withFormUrlEncodedBody(("did-you-agree-rent-with-landlord-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}