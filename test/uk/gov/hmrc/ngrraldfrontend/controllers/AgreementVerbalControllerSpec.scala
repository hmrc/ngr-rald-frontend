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
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, headers, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, RaldUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.AgreementVerbalPage
import uk.gov.hmrc.ngrraldfrontend.views.html.AgreementVerbalView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.DateTextFields

import scala.collection.immutable.TreeMap
import scala.concurrent.Future

class AgreementVerbalControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Agreement"
  val view: AgreementVerbalView = inject[AgreementVerbalView]
  val mockDateTextFields: DateTextFields = inject[DateTextFields]
  val controllerNoProperty: AgreementVerbalController = new AgreementVerbalController(view, fakeAuth, mockDateTextFields, mcc, fakeData(None), mockSessionRepository, mockNavigator)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => AgreementVerbalController = answers => new AgreementVerbalController(view, fakeAuth, mockDateTextFields, mcc, fakeDataProperty(Some(property), answers), mockSessionRepository, mockNavigator)(mockConfig, ec)
  val agreementVerbalMinAnswers: Option[UserAnswers] = UserAnswers("id").set(AgreementVerbalPage, agreementVerbalModelMin).toOption
  val agreementVerbalAnswers: Option[UserAnswers] = UserAnswers("id").set(AgreementVerbalPage, agreementVerbalModel).toOption

  "Agreement Verbal controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "return OK and the correct view with prepopulated answers but no end date" in {
        val result = controllerProperty(agreementVerbalMinAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=agreementStartDate.day]").attr("value") mustBe "01"
        document.select("input[name=agreementStartDate.month]").attr("value") mustBe "01"
        document.select("input[name=agreementStartDate.year]").attr("value") mustBe "2025"
        document.select("input[type=radio][name=agreement-verbal-radio][value=Yes]").hasAttr("checked") mustBe true
      }
      "return OK and the correct view with prepopulated answers with an end date" in {
        val result = controllerProperty(agreementVerbalAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=agreementStartDate.day]").attr("value") mustBe "01"
        document.select("input[name=agreementStartDate.month]").attr("value") mustBe "01"
        document.select("input[name=agreementStartDate.year]").attr("value") mustBe "2025"
        document.select("input[type=radio][name=agreement-verbal-radio][value=No]").hasAttr("checked") mustBe true
        document.select("input[name=agreementEndDate.day]").attr("value") mustBe "02"
        document.select("input[name=agreementEndDate.month]").attr("value") mustBe "02"
        document.select("input[name=agreementEndDate.year]").attr("value") mustBe "2025"
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
      "Return SEE_OTHER and redirect HowMuchIsTotalAnnualRent view when radio button selected yes" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "Yes"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/how-much-is-total-annual-rent")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchIsTotalAnnualRentController.show(NormalMode).url)
      }
      "Return SEE_OTHER and redirect HowMuchIsTotalAnnualRent view when radio button selected no" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "No",
            "agreementEndDate.day" -> "30",
            "agreementEndDate.month" -> "4",
            "agreementEndDate.year" -> "2027"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result) mustBe TreeMap("Location" -> "/ngr-rald-frontend/how-much-is-total-annual-rent")
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchIsTotalAnnualRentController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio button is selected" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        headers(result).isEmpty mustBe true
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreement-verbal-radio\">Select Yes if your agreement is open-ended</a>")
      }
      "Return Form with Errors when radio button No is selected but no end date is given" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "No",
            "agreementEndDate.day" -> "",
            "agreementEndDate.month" -> "",
            "agreementEndDate.year" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementEndDate\">Select yes if your agreement is open-ended</a>")
      }
      "Return Form with Errors when radio button No is selected but end date is invalid" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "No",
            "agreementEndDate.day" -> "30",
            "agreementEndDate.month" -> "2",
            "agreementEndDate.year" -> "2027"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementEndDate\">Date your agreement ends must be a real date</a>")
      }
      "Return Form with Errors when start date is missing day" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "Yes"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementStartDate.day\">Date your agreement started must include a day</a>")
      }
      "Return Form with Errors when start date is missing month" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "Yes"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementStartDate.month\">Date your agreement started must include a month</a>")
      }
      "Return Form with Errors when start date is missing year" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "",
            "agreement-verbal-radio" -> "Yes"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementStartDate.year\">Date your agreement started must include a year</a>")
      }
      "Return Form with Errors when end date is missing day" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "No",
            "agreementEndDate.day" -> "",
            "agreementEndDate.month" -> "2",
            "agreementEndDate.year" -> "2027"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementEndDate.day\">Date your agreement ends must include a day</a>")
      }
      "Return Form with Errors when end date is missing month" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "No",
            "agreementEndDate.day" -> "30",
            "agreementEndDate.month" -> "",
            "agreementEndDate.year" -> "2027"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementEndDate.month\">Date your agreement ends must include a month</a>")
      }
      "Return Form with Errors when end date is missing year" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "agreementStartDate.day" -> "30",
            "agreementStartDate.month" -> "4",
            "agreementStartDate.year" -> "2025",
            "agreement-verbal-radio" -> "No",
            "agreementEndDate.day" -> "30",
            "agreementEndDate.month" -> "12",
            "agreementEndDate.year" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#agreementEndDate.year\">Date your agreement ends must include a year</a>")
      }
      "Return Exception if no address is in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.AgreementVerbalController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "agreementStartDate.day" -> "30",
              "agreementStartDate.month" -> "4",
              "agreementStartDate.year" -> "2025",
              "agreement-verbal-radio" -> ""
            )
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
