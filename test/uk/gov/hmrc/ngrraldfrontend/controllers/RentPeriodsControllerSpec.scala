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
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstRentPeriodPage, ProvideDetailsOfSecondRentPeriodPage, RentPeriodsPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentPeriodView

import scala.concurrent.Future

class RentPeriodsControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Rent periods"
  val view: RentPeriodView = inject[RentPeriodView]
  val controllerNoProperty: RentPeriodsController = new RentPeriodsController(view, mockAuthJourney, fakeData(None), mockCheckRequestSentReference, mcc, mockSessionRepository, mockNavigator)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => RentPeriodsController = answers => new RentPeriodsController(view, mockAuthJourney, fakeDataProperty(Some(property), answers), mockCheckRequestSentReference, mcc, mockSessionRepository, mockNavigator)(mockConfig, ec)

  lazy val firstSecondRentPeriodAnswers: Option[UserAnswers] = userAnswersWithoutData.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod)
    .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod)).toOption

  "method show" must {
    "Return OK and the correct view when the user has said yes to having paid rent for the first period" in {
      val result = controllerProperty(firstSecondRentPeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
      status(result) mustBe OK
      val content = contentAsString(result)
      content must include(pageTitle)
    }
    "Return OK and the correct view with prepopulated answers" in {
      val answers = firstSecondRentPeriodAnswers.get.set(RentPeriodsPage, true).toOption
      val result = controllerProperty(answers).show(NormalMode)(authenticatedFakeRequest)
      status(result) mustBe OK
      val content = contentAsString(result)
      val document = Jsoup.parse(content)
      document.select("span[id=first-period-start-date-id]").text() mustBe "1 January 2025"
      document.select("span[id=first-period-end-date-id]").text() mustBe "31 January 2025"
      document.select("span[id=first-period-rent-value-id]").text() mustBe "£1,000.46"
      document.select("span[id=first-period-has-pay-id]").text() mustBe "Yes"
      document.select("span[id=second-period-start-date-id]").text() mustBe "1 February 2025"
      document.select("span[id=second-period-end-date-id]").text() mustBe "31 March 2025"
      document.select("span[id=second-period-rent-value-id]").text() mustBe "£1,350"
      document.select("span[id=third-period-start-date-id]").text() mustBe "1 April 2025"
      document.select("span[id=third-period-end-date-id]").text() mustBe "31 May 2025"
      document.select("span[id=third-period-rent-value-id]").text() mustBe "£1,550"
      document.select("span[id=fourth-period-start-date-id]").text() mustBe "1 June 2025"
      document.select("span[id=fourth-period-end-date-id]").text() mustBe "31 August 2025"
      document.select("span[id=fourth-period-rent-value-id]").text() mustBe "£2,550"
      document.select("input[type=radio][name=rent-periods-radio][value=true]").hasAttr("checked") mustBe true
      document.select("input[type=radio][name=rent-periods-radio][value=false]").hasAttr("checked") mustBe false
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
    "Return SEE_OTHER and the correct view after submitting yes" in {
      val answers = firstSecondRentPeriodAnswers.get.set(RentPeriodsPage, true).toOption
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val result = controllerProperty(answers).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentPeriodsController.submit(NormalMode))
        .withFormUrlEncodedBody(
          "rent-periods-radio" -> "true"
        )
        .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.AdditionalRentPeriodController.show(NormalMode, detailsOfRentPeriod.size).url)
    }
    "Return SEE_OTHER and the correct view after submitting no" in {
      when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
      val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentPeriodsController.submit(NormalMode))
        .withFormUrlEncodedBody(
          "rent-periods-radio" -> "false"
        )
        .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
      result.map(result => {
        result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
      })
      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(routes.DidYouAgreeRentWithLandlordController.show(NormalMode).url)
    }
    "Return Form with Errors when no name is input" in {
      val result = controllerProperty(firstSecondRentPeriodAnswers).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.RentPeriodsController.submit(NormalMode))
        .withFormUrlEncodedBody(
          "rent-periods-radio" -> ""
        )
        .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
      result.map(result => {
        result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord")
      })
      status(result) mustBe BAD_REQUEST
      val content = contentAsString(result)
      content must include(pageTitle)
    }
  }
}
