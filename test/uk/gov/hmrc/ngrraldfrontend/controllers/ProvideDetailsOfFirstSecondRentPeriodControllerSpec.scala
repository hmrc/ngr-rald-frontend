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
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.ProvideDetailsOfFirstSecondRentPeriodPage
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfFirstSecondRentPeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.{DateTextFields, InputText}

import scala.concurrent.Future

class ProvideDetailsOfFirstSecondRentPeriodSpec extends ControllerSpecSupport {
  val pageTitle = "Provide details of each rent period"
  val view: ProvideDetailsOfFirstSecondRentPeriodView = inject[ProvideDetailsOfFirstSecondRentPeriodView]
  val controllerNoProperty: ProvideDetailsOfFirstSecondRentPeriodController = new ProvideDetailsOfFirstSecondRentPeriodController(
    view,
    fakeAuth,
    mockInputText,
    mcc,
    fakeData(None),
    mockSessionRepository,
    mockNavigator
  )(mockConfig, ec)

  val controllerProperty: Option[UserAnswers] => ProvideDetailsOfFirstSecondRentPeriodController = answers => new ProvideDetailsOfFirstSecondRentPeriodController(
    view,
    fakeAuth,
    mockInputText,
    mcc,
    fakeDataProperty(Some(property), answers),
    mockSessionRepository,
    mockNavigator
  )(mockConfig, ec)

  val firstSecondRentPeriodAnswers: Option[UserAnswers] = userAnswersWithoutData.set(ProvideDetailsOfFirstSecondRentPeriodPage, firstSecondRentPeriod).toOption
  val firstSecondRentPeriodAnswersMin: Option[UserAnswers] = userAnswersWithoutData.set(ProvideDetailsOfFirstSecondRentPeriodPage, firstSecondRentPeriodNoRentPayed).toOption


  "Agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated data and YesPayedRent" in {
        val result = controllerProperty(firstSecondRentPeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=first.startDate.day]").attr("value") mustBe "1"
        document.select("input[name=first.startDate.month]").attr("value") mustBe "1"
        document.select("input[name=first.startDate.year]").attr("value") mustBe "2025"
        document.select("input[name=first.endDate.day]").attr("value") mustBe "31"
        document.select("input[name=first.endDate.month]").attr("value") mustBe "1"
        document.select("input[name=first.endDate.year]").attr("value") mustBe "2025"
        document.select("input[type=radio][name=provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio][value=true]").hasAttr("checked") mustBe true
        document.select("input[name=second.startDate.day]").attr("value") mustBe "1"
        document.select("input[name=second.startDate.month]").attr("value") mustBe "2"
        document.select("input[name=second.startDate.year]").attr("value") mustBe "2025"
        document.select("input[name=second.endDate.day]").attr("value") mustBe "28"
        document.select("input[name=second.endDate.month]").attr("value") mustBe "2"
        document.select("input[name=second.endDate.year]").attr("value") mustBe "2025"
        document.select("input[name=SecondRentPeriodAmount]").attr("value") mustBe "1000"
      }
      "Return OK and the correct view with prepopulated data and noRentPayed" in {
        val result = controllerProperty(firstSecondRentPeriodAnswersMin).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=first.startDate.day]").attr("value") mustBe "1"
        document.select("input[name=first.startDate.month]").attr("value") mustBe "1"
        document.select("input[name=first.startDate.year]").attr("value") mustBe "2025"
        document.select("input[name=first.endDate.day]").attr("value") mustBe "31"
        document.select("input[name=first.endDate.month]").attr("value") mustBe "1"
        document.select("input[name=first.endDate.year]").attr("value") mustBe "2025"
        document.select("input[type=radio][name=provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio][value=false]").hasAttr("checked") mustBe true
        document.select("input[name=second.startDate.day]").attr("value") mustBe "1"
        document.select("input[name=second.startDate.month]").attr("value") mustBe "2"
        document.select("input[name=second.startDate.year]").attr("value") mustBe "2025"
        document.select("input[name=second.endDate.day]").attr("value") mustBe "28"
        document.select("input[name=second.endDate.month]").attr("value") mustBe "2"
        document.select("input[name=second.endDate.year]").attr("value") mustBe "2025"
        document.select("input[name=SecondRentPeriodAmount]").attr("value") mustBe "1000"
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
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-is-your-rent-based-on")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentPeriodsController.show(NormalMode).url)
      }
      "Return OK and the correct view after submitting with first start date, first end date yes radio button selected for first rent period with first rent amount" +
        "and second rent date start, end and amount is added" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "false",
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
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/what-is-your-rent-based-on")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentPeriodsController.show(NormalMode).url)
      }
      "Return Form with Errors when no day is added to the first periods start date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no month is added to the first periods start date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no year is added to the first periods start date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no day is added to the first periods end date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no month is added to the first periods end date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }

      "Return Form with Errors when no year is added to the first periods end date" in {

        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no day is added to the second period start date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }

      "Return Form with Errors when no month is added to the second period start date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no year is added to the second period start date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no day is added to the second period end date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.day" -> "12",
            "first.startDate.day" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate..year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no month is added to the second period end date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no year is added to the second period end date" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no first rent period radio is selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
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
      }
      "Return Form with Errors when no rent period amount is added and firstRentPeriodRadio has yesPayedRent selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no rent second period amount is added" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when no radio is selected for first rent" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
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
      }
      "Return Form with Errors when format is wrong for RentPeriodAmount" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Form with Errors when format is wrong for SecondRentPeriodAmount" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ProvideDetailsOfFirstSecondRentPeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "first.startDate.day" -> "12",
            "first.startDate.month" -> "12",
            "first.startDate.year" -> "2026",
            "first.endDate.day" -> "12",
            "first.endDate.month" -> "12",
            "first.endDate.year" -> "2026",
            "provideDetailsOfFirstSecondRentPeriod-radio-firstRentPeriodRadio" -> "true",
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
      }
      "Return Exception if no address is in the mongo" in {
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


