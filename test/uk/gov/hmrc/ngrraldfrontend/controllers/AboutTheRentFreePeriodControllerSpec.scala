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


import org.jsoup.Jsoup
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AboutTheRentFreePeriod, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{AgreedRentChangePage, AboutTheRentFreePeriodPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.AboutTheRentFreePeriodView
import uk.gov.hmrc.ngrraldfrontend.views.html.components.InputText

import scala.concurrent.Future


class AboutTheRentFreePeriodControllerSpec extends ControllerSpecSupport {
  val pageTitle = "About the rent-free period"
  val view: AboutTheRentFreePeriodView = inject[AboutTheRentFreePeriodView]
  val controllerNoProperty: AboutTheRentFreePeriodController = new AboutTheRentFreePeriodController(
    aboutTheRentFreePeriodView = view,
    authenticate = fakeAuth,
    inputText = mockInputText,
    getData = fakeData(None),
    sessionRepository = mockSessionRepository,
    navigator = mockNavigator,
    mcc = mcc)(mockConfig)
  val controllerProperty: Option[UserAnswers] => AboutTheRentFreePeriodController = answers => new AboutTheRentFreePeriodController(
    aboutTheRentFreePeriodView = view,
    authenticate = fakeAuth,
    inputText = mockInputText,
    getData = fakeDataProperty(Some(property),answers),
    sessionRepository = mockSessionRepository,
    navigator = mockNavigator,
    mcc = mcc)(mockConfig)

  val aboutTheRentFreePeriodAnswers: Option[UserAnswers] = userAnswers.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel).toOption

  "AboutTheRentFreePeriodController" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(aboutTheRentFreePeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=howManyMonths]").attr("value") mustBe "1"
        document.select("input[name=date.day]").attr("value") mustBe "1"
        document.select("input[name=date.month]").attr("value") mustBe "1"
        document.select("input[name=date.year]").attr("value") mustBe "1990"

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
      "Return SEE_OTHER and the correct view" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest =  FakeRequest(routes.AboutTheRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "howManyMonths" -> "1",
            "date.day" -> "1",
            "date.month" -> "1",
            "date.year" -> "1990"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HasAnythingElseAffectedTheRentController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for missing how much input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.AboutTheRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "howManyMonths" -> "",
            "date.day" -> "1",
            "date.month" -> "1",
            "date.year" -> "1990"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Enter how many months your rent-free period is")
      }
      "Return BAD_REQUEST for missing month input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.AboutTheRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "howManyMonths" -> "1",
            "date.day" -> "1",
            "date.month" -> "",
            "date.year" -> "1990"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Rent-free period start date must include a month")
      }
      "Return BAD_REQUEST for missing year input and the correct view" in {
        val fakePostRequest = FakeRequest(routes.AboutTheRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "howManyMonths" -> "1",
            "date.day" -> "1",
            "date.month" -> "1",
            "date.year" -> ""
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Rent-free period start date must include a year")
      }
      "Return Exception if no address is in the mongo" in {
        val fakePostRequest = FakeRequest(routes.AboutTheRentFreePeriodController.submit(NormalMode))
          .withFormUrlEncodedBody(("how–much–is–total–annual–rent-value", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest)))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}
