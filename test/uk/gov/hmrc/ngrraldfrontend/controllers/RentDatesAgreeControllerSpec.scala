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
import play.api.test.FakeRequest
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, RaldUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.{AgreedRentChangePage, RentDatesAgreePage}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentDatesAgreeView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import scala.concurrent.Future

class RentDatesAgreeControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Rent dates"
  val view: RentDatesAgreeView = inject[RentDatesAgreeView]
  val controllerNoProperty: RentDatesAgreeController = new RentDatesAgreeController(
    view,
    fakeAuth,
    mcc,
    fakeData(None),
    mockNavigator,
    mockSessionRepository
  )(mockConfig, ec)

  val controllerProperty: Option[UserAnswers] => RentDatesAgreeController = answers => new RentDatesAgreeController(
    view,
    fakeAuth,
    mcc,
    fakeDataProperty(Some(property), answers),
    mockNavigator,
    mockSessionRepository
  )(mockConfig, ec)

  val rentDatesAgreeAnswers: Option[UserAnswers] = UserAnswers("id").set(RentDatesAgreePage, "2025-02-01").toOption



  "Rent Date Agree controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(rentDatesAgreeAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=date.day]").attr("value") mustBe "01"
        document.select("input[name=date.month]").attr("value") mustBe "02"
        document.select("input[name=date.year]").attr("value") mustBe "2025"
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
      "Return OK and the correct view after submitting with first start date, first end date no radio button selected for first rent period" +
        "and second rent date start, end and amount is added" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "date.day" -> "12",
            "date.month" -> "12",
            "date.year" -> "2026"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/rent-dates-agree")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.WhatYourRentIncludesController.show(NormalMode).url)
      }
      "Return Form with Errors when no day is added" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "date.day" -> "",
            "date.month" -> "12",
            "date.year" -> "2026"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when no month is added" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "date.day" -> "12",
            "date.month" -> "",
            "date.year" -> "2026"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Form with Errors when no year is added" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "date.day" -> "12",
            "date.month" -> "12",
            "date.year" -> "",
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
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentDatesAgreeController.submit(NormalMode))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
