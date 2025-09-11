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

///*
// * Copyright 2025 HM Revenue & Customs
// *
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// *
// *     http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//
//package uk.gov.hmrc.ngrraldfrontend.controllers
//
//import org.mockito.ArgumentMatchers.any
//import org.mockito.Mockito.when
//import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
//import play.api.test.FakeRequest
//import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
//import uk.gov.hmrc.auth.core.Nino
//import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
//import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
//import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
//import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, RaldUserAnswers}
//import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
//import uk.gov.hmrc.ngrraldfrontend.views.html.AgreementView
//import uk.gov.hmrc.ngrraldfrontend.views.html.components.{DateTextFields, NGRCharacterCountComponent}
//
//import scala.concurrent.Future
//
//class AgreementControllerSpec extends ControllerSpecSupport {
//  val pageTitle = "Agreement"
//  val view: AgreementView = inject[AgreementView]
//  val mockNGRCharacterCountComponent: NGRCharacterCountComponent = inject[NGRCharacterCountComponent]
//  val mockDateTextFieldsComponent: DateTextFields = inject[DateTextFields]
//  val controller: AgreementController = new AgreementController(view, mockAuthJourney,mockDateTextFieldsComponent, mockNGRCharacterCountComponent, mcc, fakeData(None),navigator, mockSessionRepository)(mockConfig, ec)
//  val over250Characters = "Bug Me Not PVT LTD, RODLEY LANE, RODLEY, LEEDS, BH1 1HU What is your rent based on?Open market value This is the rent a landlord could rent the property for if, it was available to anyoneA percentage of open market value This is a percentage of the rent a landlord could rent the property for if, it was available to anyoneTurnover top-up The rent is a fixed base rent with an additional payment based on a percentage of your turnoverA percentage of expected turnover The rent paid is based on a percentage of turnoverTotal Occupancy Cost leases (TOCs)The rent is the total cost of leasing the property. It includes base rent, business rates, insurance and utilities. It also includes common area maintenance and tenant improvements Indexation The rent is reviewed according to an index (such as Retail Price Index)Other The rent was agreed another way Can you tell us how your rent was agreed?"
//
//  "Agreement controller" must {
//    "method show" must {
//      "Return OK and the correct view" in {
//        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
//        val result = controller.show(NormalMode)(authenticatedFakeRequest())
//        status(result) mustBe OK
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
////      "Return NotFoundException when property is not found in the mongo" in {
////        mockRequestWithoutProperty()
////        val exception = intercept[NotFoundException] {
////          await(controller.show(NormalMode)(authenticatedFakeRequest()))
////        }
////        exception.getMessage contains "Couldn't find property in mongo" mustBe true
////      }
//    }
//
//    "method submit" must {
//      "Return OK and the correct view after submitting with start date, yes radio button selected for open ended " +
//        "and no radio button selected for break clause" in {
//        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-is-your-rent-based-on")
//        })
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(routes.WhatIsYourRentBasedOnController.show(NormalMode).url)
//      }
//      "Return OK and the correct view after submitting with start date, no radio button selected for open ended" +
//        "with an end date added in the conditional field and no radio button selected for break clause" in {
//        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(
//          Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))
//        ))
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "12",
//            "agreementEndDate.month" -> "12",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-is-your-rent-based-on")
//        })
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(routes.WhatIsYourRentBasedOnController.show(NormalMode).url)
//      }
//      "Return OK and the correct view after submitting with start date, no radio button selected for open ended" +
//        "with an end date added in the conditional field and yes radio button selected for break clause with" +
//        "reason in the conditional text box" in {
//        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "12",
//            "agreementEndDate.month" -> "12",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-is-your-rent-based-on")
//        })
//        status(result) mustBe SEE_OTHER
//        redirectLocation(result) mustBe Some(routes.WhatIsYourRentBasedOnController.show(NormalMode).url)
//      }
//      "Return Form with Errors when no day is input for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when no month is input for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when no year is input for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when no day and month is input for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "",
//            "agreementStartDate.month" -> "",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when no month and year is input for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "",
//            "agreementStartDate.year" -> "",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when no day and year is input for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when no date is input for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "",
//            "agreementStartDate.month" -> "",
//            "agreementStartDate.year" -> "",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when date is not numbers for the start date" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "one",
//            "agreementStartDate.month" -> "one",
//            "agreementStartDate.year" -> "two",
//            "agreement-radio-openEnded" -> "YesOpenEnded",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when no open ended radio is selected" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "",
//            "agreement-breakClause-radio" -> "NoBreakClause",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/agreement")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and no date is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "",
//            "agreementEndDate.month" -> "",
//            "agreementEndDate.year" -> "",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and incorrect date format is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "one",
//            "agreementEndDate.month" -> "one",
//            "agreementEndDate.year" -> "two",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and no day is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "",
//            "agreementEndDate.month" -> "12",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and no month is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "12",
//            "agreementEndDate.month" -> "",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and no year is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "12",
//            "agreementEndDate.month" -> "12",
//            "agreementEndDate.year" -> "",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and no month and year is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "12",
//            "agreementEndDate.month" -> "",
//            "agreementEndDate.year" -> "",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and no day and year is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "",
//            "agreementEndDate.month" -> "12",
//            "agreementEndDate.year" -> "",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when open ended radio is selected and no day and month is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "",
//            "agreementEndDate.month" -> "",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "Reason...",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when break clause radio is not selected" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "",
//            "agreementEndDate.month" -> "",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> ""
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when break clause radio is selected as yes and no reason is input" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "12",
//            "agreementEndDate.month" -> "12",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> "",
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
//      "Return Form with Errors when break clause radio is selected as yes and reason input is too long" in {
//        mockRequest(hasCredId = true)
//        val result = controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
//          .withFormUrlEncodedBody(
//            "agreementStartDate.day" -> "12",
//            "agreementStartDate.month" -> "12",
//            "agreementStartDate.year" -> "2026",
//            "agreement-radio-openEnded" -> "NoOpenEnded",
//            "agreementEndDate.day" -> "12",
//            "agreementEndDate.month" -> "12",
//            "agreementEndDate.year" -> "2026",
//            "agreement-breakClause-radio" -> "YesBreakClause",
//            "about-break-clause" -> over250Characters,
//          )
//          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
//        result.map(result => {
//          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
//        })
//        status(result) mustBe BAD_REQUEST
//        val content = contentAsString(result)
//        content must include(pageTitle)
//      }
////      "Return Exception if no address is in the mongo" in {
////        mockRequestWithoutProperty()
////        val exception = intercept[NotFoundException] {
////          await(controller.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
////            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
////            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
////        }
////        exception.getMessage contains "Couldn't find property in mongo" mustBe true
////      }
//    }
//  }
//}
