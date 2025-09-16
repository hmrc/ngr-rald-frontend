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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, RaldUserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfFirstSecondRentPeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import scala.concurrent.Future

class ProvideDetailsOfFirstSecondRentPeriodControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Provide details of each rent period"
  val view: ProvideDetailsOfFirstSecondRentPeriodView = inject[ProvideDetailsOfFirstSecondRentPeriodView]
  val controller: ProvideDetailsOfFirstSecondRentPeriodController = new ProvideDetailsOfFirstSecondRentPeriodController(
    view,
    mockAuthJourney,
    mockInputText,
    mockPropertyLinkingAction,
    mockRaldRepo, mcc
  )(mockConfig, ec)

  "Agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return NotFoundException when property is not found in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.show(authenticatedFakeRequest()))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }

    "method submit" must {
      "Return OK and the correct view after submitting with first start date, first end date no radio button selected for first rent period" +
        "and second rent date start, end and amount is added" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "noRentPayed",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentPeriodsController.show.url)
      }
      "Return OK and the correct view after submitting with first start date, first end date yes radio button selected for first rent period with first rent amount" +
        "and second rent date start, end and amount is added" in {
        when(mockRaldRepo.findByCredId(any())) thenReturn (Future.successful(Some(RaldUserAnswers(credId = CredId(null), NewAgreement, selectedProperty = property))))
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))

        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentPeriodsController.show.url)
      }
      "Return Form with Errors when no day is added to the first periods start date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#first.startDate.day\">Start date of the first rent period must include a day</a>")
      }
      "Return Form with Errors when no month is added to the first periods start date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#first.startDate.month\">Start date of the first rent period must include a month</a>")
      }
      "Return Form with Errors when no year is added to the first periods start date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#first.startDate.year\">Start date of the first rent period must include a year</a>")
      }
      "Return Form with Errors when no day is added to the first periods end date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#first.endDate.day\">End date of the first rent period must include a day</a>")
      }
      "Return Form with Errors when no month is added to the first periods end date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#first.endDate.month\">End date of the first rent period must include a month</a>")
      }

      "Return Form with Errors when no year is added to the first periods end date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#first.endDate.year\">End date of the first rent period must include a year</a>")
      }
      "Return Form with Errors when no day is added to the second period start date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#second.startDate.day\">Start date of the second rent period must include a day</a>")
      }

      "Return Form with Errors when no month is added to the second period start date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#second.startDate.month\">Start date of the second rent period must include a month</a>")
      }
      "Return Form with Errors when no year is added to the second period start date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#second.startDate.year\">Start date of the second rent period must include a year</a>")
      }
      "Return Form with Errors when no day is added to the second period end date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.day" -> "12",
            "first.startDate.day" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate..year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#second.endDate.day\">End date of the second rent period must include a day</a>")
      }
      "Return Form with Errors when no month is added to the second period end date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#second.endDate.month\">End date of the second rent period must include a month</a>")
      }
      "Return Form with Errors when no year is added to the second period end date" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#second.endDate.year\">End date of the second rent period must include a year</a>")
      }
      "Return Form with Errors when no first rent period radio is selected" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio\">Select yes if you pay rent in this period.</a>")
      }
      "Return Form with Errors when no rent period amount is added and firstRentPeriodRadio has yesPayedRent selected" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000.00",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#RentPeriodAmount\">Enter the rent for the first rent period, in pounds.</a>")
      }
      "Return Form with Errors when no rent second period amount is added" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#SecondRentPeriodAmount\">Enter the rent for the second rent period, in pounds.</a>")
      }
      "Return Form with Errors when no radio is selected for first rent" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio\">Select yes if you pay rent in this period.</a>")
      }
      "Return Form with Errors when format is wrong for RentPeriodAmount" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "hello",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "10000",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#RentPeriodAmount\">Rent for the first rent period must be a number, like 30,000</a>")
      }
      "Return Form with Errors when format is wrong for SecondRentPeriodAmount" in {
        mockRequest(hasCredId = true)
        val result = controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit)
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "yesPayedRent",
            "RentPeriodAmount" -> "20000.00",
            "second.startDate.day" -> "12",
            "second.startDate.month" -> "12",
            "second.startDate.year" -> "2026",
            "second.endDate.day" -> "12",
            "second.endDate.month" -> "12",
            "second.endDate.year" -> "2026",
            "SecondRentPeriodAmount" -> "hello",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#SecondRentPeriodAmount\">Rent for the second rent period must be a number, like 30,000</a>")
      }
      "Return Exception if no address is in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.submit()(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit)
            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
  }
}


