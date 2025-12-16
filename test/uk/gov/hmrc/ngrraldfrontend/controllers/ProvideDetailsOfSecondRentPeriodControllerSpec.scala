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
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{CheckMode, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstRentPeriodPage, ProvideDetailsOfSecondRentPeriodPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfSecondRentPeriodView

import scala.concurrent.Future

class ProvideDetailsOfSecondRentPeriodControllerSpec extends ControllerSpecSupport:

  val pageTitle = "Second rent period"
  val view: ProvideDetailsOfSecondRentPeriodView = inject[ProvideDetailsOfSecondRentPeriodView]

  val controllerWithAnswers: Option[UserAnswers] => ProvideDetailsOfSecondRentPeriodController = answers => new ProvideDetailsOfSecondRentPeriodController(
    view,
    mockAuthJourney,
    mcc,
    fakeDataProperty(Some(property), answers),
    mockCheckRequestSentReference,
    mockSessionRepository,
    mockNavigator
  )(mockConfig, ec)


  val firstRentPeriodAnswers: Option[UserAnswers] = userAnswersWithoutData.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod).toOption
  val firstSecondAnswers: Option[UserAnswers] =
    userAnswersWithoutData.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod)
      .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, Seq(secondRentPeriod)))
      .toOption

  val fourRentPeriodsAnswers: Option[UserAnswers] =
    userAnswersWithoutData.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod)
      .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod))
      .toOption

  "ProvideDetailsOfSecondRentPeriodController" must {
    "method show" must {
      "return OK and the correct view" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }

      "return OK and the correct view with prepopulated data for rent payable period" in {
        val result = controllerWithAnswers(firstSecondAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("p[class=govuk-body]").text() mustBe "1 February 2025"
        document.select("input[name=endDate.day]").attr("value") mustBe "31"
        document.select("input[name=endDate.month]").attr("value") mustBe "3"
        document.select("input[name=endDate.year]").attr("value") mustBe "2025"
        document.select("input[name=rentPeriodAmount]").attr("value") mustBe "1350"
      }

      "return SEE_OTHER and redirect to first rent period page" in {
        val result = controllerWithAnswers(Some(userAnswersWithoutData)).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ProvideDetailsOfFirstRentPeriodController.show(NormalMode).url)
      }
    }

    "method submit" must {
      "return SEE_OTHER after submitting with end date selected, rent amount" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentPeriodsController.show(NormalMode).url)
      }
      "return SEE_OTHER after change end date in check mode and direct to review rent periods" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerWithAnswers(firstSecondAnswers).submit(CheckMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(CheckMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentPeriodsController.show(CheckMode).url)
      }
      "return SEE_OTHER after change end date in check mode and direct to rent periods" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerWithAnswers(fourRentPeriodsAnswers).submit(CheckMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(CheckMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RentPeriodsController.show(CheckMode).url)
      }
      "Return Form with Errors when end date day is missing" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.day\">End date of the second rent period must include a day</a>")
      }
      "Return Form with Errors when end date month is missing" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.month\">End date of the second rent period must include a month</a>")
      }
      "Return Form with Errors when end date year is missing" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.year\">End date of the second rent period must include a year</a>")
      }
      "Return Form with Errors when end date day and year are missing" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "",
              "endDate.month" -> "12",
              "endDate.year" -> "",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.day\">End date of the second rent period must include a day and year</a>")
      }
      "Return Form with Errors when end date day contains characters" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "AS",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.day\">End date of the second rent period must be a real date</a>")
      }
      "Return Form with Errors when end date is before 1900" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "1899",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.year\">Year for the second rent period end date must be 1900 or after</a>")
      }
      "Return Form with Errors when end date is before start date" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "1",
              "endDate.month" -> "1",
              "endDate.year" -> "2025",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.day\">End date of this rent period must be a date after the period starts</a>")
      }
      "Return Form with Errors when end day contains characters" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "AS",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "22000.00"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#endDate.day\">End date of the second rent period must be a real date</a>")
      }
      "Return Form with Errors when rent period amount is missing" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> ""
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#rentPeriodAmount\">Enter the rent for the second rent period, in pounds</a>")
      }
      "Return Form with Errors when rent period amount contains special characters" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "AS"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#rentPeriodAmount\">Rent for the second rent period must be a number, like 30,000</a>")
      }
      "Return Form with Errors when rent period amount is greater than max amount" in {
        val result = controllerWithAnswers(firstRentPeriodAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "rentPeriodAmount" -> "19999999.99"
            )
        ))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("<a href=\"#rentPeriodAmount\">Rent for the second rent period must be £9,999,999.99 or less</a>")
      }
      "Return NotFoundException when first rent period is not found in user answers" in {
        val exception = intercept[NotFoundException] {
          await(controllerWithAnswers(Some(userAnswersWithoutData)).submit(NormalMode)(authenticatedFakePostRequest(
            FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
              .withFormUrlEncodedBody(
                "endDate.day" -> "",
                "endDate.month" -> "12",
                "endDate.year" -> "2026",
                "rentPeriodAmount" -> "22000.00"
              )
          )))
        }
        exception.getMessage contains "Can't find previous end date" mustBe true
      }
    }
  }
