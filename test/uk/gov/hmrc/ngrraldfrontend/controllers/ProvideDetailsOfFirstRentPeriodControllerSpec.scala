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
import play.api.test.Helpers.{contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.ProvideDetailsOfFirstRentPeriodPage
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfFirstRentPeriodView

import scala.concurrent.Future

class ProvideDetailsOfFirstRentPeriodControllerSpec extends ControllerSpecSupport:

  val pageTitle = "First rent period"
  val view: ProvideDetailsOfFirstRentPeriodView = inject[ProvideDetailsOfFirstRentPeriodView]

  val controllerWithAnswers: Option[UserAnswers] => ProvideDetailsOfFirstRentPeriodController = answers => new ProvideDetailsOfFirstRentPeriodController(
    view,
    fakeAuth,
    mockInputText,
    mcc,
    fakeDataProperty(Some(property), answers),
    mockSessionRepository,
    mockNavigator
  )(mockConfig, ec)

  val firstRentPeriodAnswers: Option[UserAnswers] = UserAnswers("id").set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod).toOption
  val firstRentPeriodAnswersMin: Option[UserAnswers] = UserAnswers("id").set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriodNoRentPayed).toOption


  "ProvideDetailsOfFirstRentPeriodController" must {
    ".show" must {
      "return OK and the correct view" in {
        val result = controllerWithAnswers(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }

      "return OK and the correct view with prepopulated data for rent payable period" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=startDate.day]").attr("value") mustBe "1"
        document.select("input[name=startDate.month]").attr("value") mustBe "1"
        document.select("input[name=startDate.year]").attr("value") mustBe "2025"
        document.select("input[name=endDate.day]").attr("value") mustBe "31"
        document.select("input[name=endDate.month]").attr("value") mustBe "1"
        document.select("input[name=endDate.year]").attr("value") mustBe "2025"
        document.select("input[type=radio][name=provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod][value=false]").hasAttr("checked") mustBe false
        document.select("input[name=rentPeriodAmount]").attr("value") mustBe "1000"
      }

      "return OK and the correct view with prepopulated data if no rent payable" in {
        val result = controllerWithAnswers(firstRentPeriodAnswersMin).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=startDate.day]").attr("value") mustBe "1"
        document.select("input[name=startDate.month]").attr("value") mustBe "1"
        document.select("input[name=startDate.year]").attr("value") mustBe "2025"
        document.select("input[name=endDate.day]").attr("value") mustBe "31"
        document.select("input[name=endDate.month]").attr("value") mustBe "1"
        document.select("input[name=endDate.year]").attr("value") mustBe "2025"
        document.select("input[type=radio][name=provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod][value=true]").hasAttr("checked") mustBe false
        document.select("input[type=radio][name=provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod][value=false]").hasAttr("checked") mustBe true
      }
    }

    ".submit" must {
      "return SEE_OTHER after submitting with start date, end date, yes radio button selected, rent amount" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerWithAnswers(None).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfFirstRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "startDate.day" -> "1",
              "startDate.month" -> "10",
              "startDate.year" -> "2026",
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchIsTotalAnnualRentController.show(NormalMode).url)
      }

      "return SEE_OTHER after submitting with start date, end date, no radio button selected" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerWithAnswers(None).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfFirstRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "startDate.day" -> "1",
              "startDate.month" -> "10",
              "startDate.year" -> "2026",
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "false"
            )
        ))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchIsTotalAnnualRentController.show(NormalMode).url)
      }

      "return form with errors when day is empty for the start date" in {
        val result = controllerWithAnswers(None).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfFirstRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "startDate.day" -> "",
              "startDate.month" -> "10",
              "startDate.year" -> "2026",
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "false"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("Start date of the first rent period must include a day")
      }

      "return form with errors when no isRentPayablePeriod radio button is selected" in {
        val result = controllerWithAnswers(None).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfFirstRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "startDate.day" -> "1",
              "startDate.month" -> "10",
              "startDate.year" -> "2026",
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> ""
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("Select yes if you pay rent in this period")
      }

      "return form with errors when no rent period amount is added and isRentPayablePeriod has Yes selected" in {
        val result = controllerWithAnswers(None).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfFirstRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "startDate.day" -> "1",
              "startDate.month" -> "10",
              "startDate.year" -> "2026",
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("Enter the rent for the first rent period, in pounds")
      }
    }
  }
