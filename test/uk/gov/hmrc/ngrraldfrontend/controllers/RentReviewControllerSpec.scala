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
import uk.gov.hmrc.ngrraldfrontend.models.forms.RentReviewForm
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, RentReview, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{DidYouAgreeRentWithLandlordPage, RentReviewPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputDateForMonthYear
import uk.gov.hmrc.ngrraldfrontend.views.html.{DidYouAgreeRentWithLandlordView, RentReviewView}

import scala.concurrent.Future

class RentReviewControllerSpec  extends ControllerSpecSupport {
  val pageTitle = "Rent review"
  val view: RentReviewView = inject[RentReviewView]
  val inputDateForMonthYear: InputDateForMonthYear = inject[InputDateForMonthYear]
  val controllerNoProperty: RentReviewController = new RentReviewController(view, fakeAuth, fakeData(None), mockNavigator, mockSessionRepository, inputDateForMonthYear, mcc)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => RentReviewController = answers => new RentReviewController(view, fakeAuth, fakeDataProperty(Some(property), answers), mockNavigator, mockSessionRepository, inputDateForMonthYear, mcc)(mockConfig, ec)
  val rentReviewAnswers: Option[UserAnswers] = UserAnswers("id").set(RentReviewPage, RentReview(true, Some(11), None, false)).toOption

  "Rent review controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(rentReviewAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=has-include-rent-review-radio][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=can-rent-go-down-radio][value=false]").hasAttr("checked") mustBe true
        document.select("input[type=text][name=date.month]").attr("value") mustBe "11"
        document.select("input[type=text][name=date.year]").attr("value") mustBe ""
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
      "Return SEE_OTHER and the correct view after submitting with month and year" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "true",
            "date.month" -> "11",
            "date.year" -> "2",
            RentReviewForm.canRentGoDownRadio -> "false"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.RepairsAndFittingOutController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view after submitting without years and months when enter false on included rent view radio" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "false",
            RentReviewForm.canRentGoDownRadio -> "true"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.RepairsAndFittingOutController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio buttons are selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "",
            RentReviewForm.canRentGoDownRadio -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include(s"<a href=\"#${RentReviewForm.hasIncludeRentReviewRadio}\">Select yes if your agreement includes a rent review</a>")
        content must include(s"<a href=\"#${RentReviewForm.canRentGoDownRadio}\">Select yes if the rent can go down when it is reviewed</a>")
      }
      "Return Form with Errors when months and years are not entered" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "true",
            RentReviewForm.canRentGoDownRadio -> "false"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#date.year\">Enter how often your rent is reviewed</a>")
      }
      "Return Form with Errors when months is over 12 and years is not entered" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "true",
            "date.month" -> "13",
            "date.year" -> "",
            RentReviewForm.canRentGoDownRadio -> "false"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#date.month\">The number of months for how often your rent is reviewed must be 12 or less</a>")
      }
      "Return Form with Errors when months is over 11 and years is entered" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "true",
            "date.month" -> "12",
            "date.year" -> "2",
            RentReviewForm.canRentGoDownRadio -> "false"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#date.month\">The number of months for how often your rent is reviewed must be 11 or less</a>")
      }
      "Return Form with Errors when years is over 1000" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "true",
            "date.month" -> "11",
            "date.year" -> "2000",
            RentReviewForm.canRentGoDownRadio -> "false"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#date.year\">How often your rent is reviewed must be 1,000 years or less</a>")
      }
      "Return Form with Errors when months and years are not numeric" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentReviewController.submit(NormalMode))
          .withFormUrlEncodedBody(
            RentReviewForm.hasIncludeRentReviewRadio -> "true",
            "date.month" -> "AS",
            "date.year" -> "-2",
            RentReviewForm.canRentGoDownRadio -> "false"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#date.month\">The number of months for how often your rent is reviewed must be a number, like 6</a>")
        content must include("<a href=\"#date.year\">The number of years for how often your rent is reviewed must be a number, like 2</a>")
      }
      "Return Exception if no address is in the mongo" in {

        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouAgreeRentWithLandlordController.submit(NormalMode))
            .withFormUrlEncodedBody(("did-you-agree-rent-with-landlord-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
