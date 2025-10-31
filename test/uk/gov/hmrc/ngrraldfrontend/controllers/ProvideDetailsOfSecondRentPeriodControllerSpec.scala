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
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstRentPeriodPage, ProvideDetailsOfSecondRentPeriodPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.ProvideDetailsOfSecondRentPeriodView

import scala.concurrent.Future

class ProvideDetailsOfSecondRentPeriodControllerSpec extends ControllerSpecSupport:

  val pageTitle = "Second rent period"
  val view: ProvideDetailsOfSecondRentPeriodView = inject[ProvideDetailsOfSecondRentPeriodView]

  val controllerWithAnswers: Option[UserAnswers] => ProvideDetailsOfSecondRentPeriodController = answers => new ProvideDetailsOfSecondRentPeriodController(
    view,
    fakeAuth,
    mcc,
    fakeDataProperty(Some(property), answers),
    mockSessionRepository,
    mockNavigator
  )(mockConfig, ec)


  val firstRentPeriodAnswers: Option[UserAnswers] = UserAnswers("id").set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod).toOption
  val secondRentPeriodAnswers: Option[UserAnswers] = UserAnswers("id").set(ProvideDetailsOfSecondRentPeriodPage, secondRentPeriod).toOption
  val firstSecondAnswers: Option[UserAnswers] =
    UserAnswers("id").set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod)
      .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, secondRentPeriod))
      .toOption

  "ProvideDetailsOfSecondRentPeriodController" must {
    ".show" must {
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
        document.select("input[name=endDate.day]").attr("value") mustBe "31"
        document.select("input[name=endDate.month]").attr("value") mustBe "1"
        document.select("input[name=endDate.year]").attr("value") mustBe "2025"
        document.select("input[name=provideDetailsOfSecondRentPeriod]").attr("value") mustBe "1000"
      }
    }

    ".submit" must {
      "return SEE_OTHER after submitting with end date selected, rent amount" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerWithAnswers(firstSecondAnswers).submit(NormalMode)(authenticatedFakePostRequest(
          FakeRequest(routes.ProvideDetailsOfSecondRentPeriodController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "endDate.day" -> "31",
              "endDate.month" -> "12",
              "endDate.year" -> "2026",
              "provideDetailsOfSecondRentPeriod" -> "22000.00"
            )
        ))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ProvideDetailsOfSecondRentPeriodController.show(NormalMode).url)
      }
    }
    }
