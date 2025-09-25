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
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.{NewAgreement, RenewedAgreement, RentAgreement}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, RaldUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{LandlordPage, TellUsAboutRentPage, TellUsAboutYourNewAgreementPage, TellUsAboutYourRenewedAgreementPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.LandlordView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

import scala.concurrent.Future

class LandlordControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Landlord"
  val view: LandlordView = inject[LandlordView]
  val mockNGRCharacterCountComponent: NGRCharacterCountComponent = inject[NGRCharacterCountComponent]
  val controllerNoProperty: LandlordController = new LandlordController(view, fakeAuth, mockNGRCharacterCountComponent, mcc, fakeData(None), mockSessionRepository, mockNavigator)(mockConfig, ec)
  val controllerProperty: LandlordController = new LandlordController(view, fakeAuth, mockNGRCharacterCountComponent, mcc, fakeDataProperty(Some(property),None), mockSessionRepository, mockNavigator)(mockConfig, ec)
  lazy val renewedAgreementAnswers: Option[UserAnswers] = UserAnswers("id").set(TellUsAboutYourRenewedAgreementPage, RenewedAgreement).toOption
  lazy val newAgreementAnswers: Option[UserAnswers] = UserAnswers("id").set(TellUsAboutYourNewAgreementPage, NewAgreement).toOption
  lazy val rentAgreementAnswers: Option[UserAnswers] = UserAnswers("id").set(TellUsAboutRentPage, RentAgreement).toOption
  lazy val filledController: Option[UserAnswers] => LandlordController = answers => LandlordController(view, fakeAuth, mockNGRCharacterCountComponent, mcc, fakeDataProperty(Some(property), answers), mockSessionRepository, mockNavigator)
  lazy val landlordAnswers: Option[UserAnswers] = UserAnswers("id").set(LandlordPage, landlordModel).toOption

  "Landlord controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty.show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated data" in {
        val result = filledController(landlordAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=landlord-name-value]").attr("value") mustBe "Joe Bloggs"
        document.select("input[name=landlord-radio]").attr("value") mustBe "LandlordYes"

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
      "Return OK and the correct view after submitting with name and radio button selected while in the rent agreement journey" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = filledController(rentAgreementAnswers).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "Bob",
            "landlord-radio" -> "LandLordAndTenant"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatIsYourRentBasedOnController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting with name and radio button selected while in the renewedAgreement journey" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = filledController(renewedAgreementAnswers).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "Bob",
            "landlord-radio" -> "LandLordAndTenant"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfAgreementController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting with name and other radio button selected with description added while in the new agreement journey" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = filledController(newAgreementAnswers).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "Bob",
            "landlord-radio" -> "LandLordAndTenant",
            "landlordOther" -> "Description of other",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatTypeOfAgreementController.show(NormalMode).url)
      }
      "Return Form with Errors when no name is input" in {
        val result = controllerProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "",
            "landlord-radio" -> "LandLordAndTenant"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when no radio button is selected" in {
        val result = controllerProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "Bob",
            "landlord-radio" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when other radio button is selected with no text" in {
        val result = controllerProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "landlord-name-value" -> "Bob",
            "landlord-radio" -> "LandlordYes",
            "landlord-yes" -> "",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
//      "Return Exception if no address is in the mongo" in {
//        mockRequestWithoutProperty()
//        val exception = intercept[NotFoundException] {
//          await(controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
//            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
//        }
//        exception.getMessage contains "Couldn't find property in mongo" mustBe true
//      }
    }
  }
}
