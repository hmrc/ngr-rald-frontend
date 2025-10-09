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
import uk.gov.hmrc.ngrraldfrontend.pages.WhatYourRentIncludesPage
import uk.gov.hmrc.ngrraldfrontend.views.html.WhatYourRentIncludesView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.NGRCharacterCountComponent

import scala.concurrent.Future

class WhatRentIncludesRatesWaterServiceControllerSpec  extends ControllerSpecSupport {
  val pageTitle = "What your rent includes"
  val view: WhatYourRentIncludesView = inject[WhatYourRentIncludesView]
  val controllerNoProperty: WhatRentIncludesRatesWaterServiceController = new WhatRentIncludesRatesWaterServiceController(
    view,
    fakeAuth,
    mockInputText,
    fakeData(None),
    mockSessionRepository,
    mockNavigator,
    mcc)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => WhatRentIncludesRatesWaterServiceController = answers => new WhatRentIncludesRatesWaterServiceController(
    view,
    fakeAuth,
    mockInputText,
    fakeDataProperty(Some(property), answers),
    mockSessionRepository,
    mockNavigator,
    mcc)(mockConfig, ec)
  val whatYourRentIncludesAnswersAllYes: Option[UserAnswers] = UserAnswers("id").set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllYes).toOption
  val whatYourRentIncludesAnswersAllNo: Option[UserAnswers] = UserAnswers("id").set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo).toOption

  "Tell us about what your rent includes rates water service controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers all Yes" in {
        val result = controllerProperty(whatYourRentIncludesAnswersAllYes).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=livingAccommodationRadio][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=rentPartAddressRadio][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=rentEmptyShellRadio][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=text][name=bedroomNumbers]").attr("value") mustBe "5"
        document.select("input[type=radio][name=rentIncBusinessRatesRadio]").isEmpty mustBe true
        document.select("input[type=radio][name=rentIncWaterChargesRadio]").isEmpty mustBe true
        document.select("input[type=radio][name=rentIncServiceRadio]").isEmpty mustBe true
      }
      "Return OK and the correct view with prepopulated answers all No" in {
        val result = controllerProperty(whatYourRentIncludesAnswersAllNo).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=livingAccommodationRadio][value=false]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=rentPartAddressRadio][value=false]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=rentEmptyShellRadio][value=false]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=rentIncBusinessRatesRadio]").isEmpty mustBe true
        document.select("input[type=radio][name=rentIncWaterChargesRadio]").isEmpty mustBe true
        document.select("input[type=radio][name=rentIncServiceRadio]").isEmpty mustBe true
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
      "Return SEE_OTHER and the correct view after submitting with all radio buttons selected" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "true",
            "rentPartAddressRadio" -> "false",
            "rentEmptyShellRadio" -> "true",
            "bedroomNumbers" -> "6"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/does-rent-include-parking-spaces-or-garages")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DoesYourRentIncludeParkingController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view after submitting with all radio buttons selected but no bedrooms" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "false",
            "rentPartAddressRadio" -> "true",
            "rentEmptyShellRadio" -> "false"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/does-rent-include-parking-spaces-or-garages")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DoesYourRentIncludeParkingController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio button is selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "",
            "rentPartAddressRadio" -> "false",
            "rentEmptyShellRadio" -> "true"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#livingAccommodationRadio\">Select yes if your rent includes any living accommodation</a>")
      }
      "Return Form with Errors when bedroom numbers is not provide" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "true",
            "rentPartAddressRadio" -> "false",
            "rentEmptyShellRadio" -> "true",
            "bedroomNumbers" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">Enter how many bedrooms the living accommodation has</a>")
      }
      "Return Form with Errors when bedroom numbers is not numeric" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "true",
            "rentPartAddressRadio" -> "false",
            "rentEmptyShellRadio" -> "true",
            "bedroomNumbers" -> "AS&"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">How many bedrooms must be a number, like 6</a>")
      }
      "Return Form with Errors when bedroom numbers is less than 1" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "true",
            "rentPartAddressRadio" -> "false",
            "rentEmptyShellRadio" -> "true",
            "bedroomNumbers" -> "0"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">How many bedrooms must be 1 or more</a>")
      }
      "Return Form with Errors when bedroom numbers is greater than 99" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "livingAccommodationRadio" -> "true",
            "rentPartAddressRadio" -> "false",
            "rentEmptyShellRadio" -> "true",
            "bedroomNumbers" -> "100"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-rent-includes")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#bedroomNumbers\">How many bedrooms must be 99 or less</a>")
      }
      "Return Exception if no address is in the mongo" in {
        
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.WhatRentIncludesRatesWaterServiceController.submit(NormalMode))
            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
