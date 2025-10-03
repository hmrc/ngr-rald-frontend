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
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.DoesYourRentIncludeParkingPage
import uk.gov.hmrc.ngrraldfrontend.views.html.DoesYourRentIncludeParkingView

import scala.concurrent.Future

class DoesYourRentIncludeParkingControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Does your rent include parking spaces or garages?"
  val view: DoesYourRentIncludeParkingView = inject[DoesYourRentIncludeParkingView]
  val controllerNoProperty: DoesYourRentIncludeParkingController = new DoesYourRentIncludeParkingController(view, fakeAuth, fakeData(None), mockSessionRepository, mockNavigator, mcc)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => DoesYourRentIncludeParkingController = answers => new DoesYourRentIncludeParkingController(view, fakeAuth, fakeDataProperty(Some(property),answers), mockSessionRepository, mockNavigator, mcc)(mockConfig, ec)
  val doesYourRentIncludeParkingAnswers: Option[UserAnswers] =  UserAnswers("id").set(DoesYourRentIncludeParkingPage, true).toOption


  "Does Your Rent IncludeParking Controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(doesYourRentIncludeParkingAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=doesYourRentIncludeParking-radio-value][value=true]").hasAttr("checked") mustBe true
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
      "Return OK and the correct view after submitting yes" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DoesYourRentIncludeParkingController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "doesYourRentIncludeParking-radio-value" -> "true",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/how-many-parking-spaces-or-garages-included-in-rent")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting no" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DoesYourRentIncludeParkingController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "doesYourRentIncludeParking-radio-value" -> "false",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/do-you-pay-extra-for-parking-spaces-or-garages-not-included-in-rent")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DoYouPayExtraForParkingSpacesController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio selection is input" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DoesYourRentIncludeParkingController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "doesYourRentIncludeParking-radio-value" -> "",
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
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}

