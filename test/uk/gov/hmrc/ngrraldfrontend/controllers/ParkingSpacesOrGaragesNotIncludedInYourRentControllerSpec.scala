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
import play.api.test.Helpers.defaultAwaitTimeout
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{HowManyParkingSpacesOrGarages, NGRDate, NormalMode, ParkingSpacesOrGaragesNotIncludedInYourRent, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.{HowManyParkingSpacesOrGaragesIncludedInRentPage, ParkingSpacesOrGaragesNotIncludedInYourRentPage}
import uk.gov.hmrc.ngrraldfrontend.views.html.{HowManyParkingSpacesOrGaragesIncludedInRentView, ParkingSpacesOrGaragesNotIncludedInYourRentView}

import scala.concurrent.Future

class ParkingSpacesOrGaragesNotIncludedInYourRentControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Parking spaces or garages not included in your rent"
  val view: ParkingSpacesOrGaragesNotIncludedInYourRentView = inject[ParkingSpacesOrGaragesNotIncludedInYourRentView]
  val controllerNoProperty: ParkingSpacesOrGaragesNotIncludedInYourRentController = new ParkingSpacesOrGaragesNotIncludedInYourRentController(
    view = view,
    authenticate = fakeAuth,
    inputText = mockInputText,
    ngrCharacterCountComponent = mockNGRCharacterCountComponent,
    mcc = mcc,
    getData = fakeData(None),
    sessionRepository = mockSessionRepository,
    navigator = mockNavigator
  )(mockConfig)

  val controllerProperty: Option[UserAnswers] => ParkingSpacesOrGaragesNotIncludedInYourRentController = answers => new ParkingSpacesOrGaragesNotIncludedInYourRentController(
    view = view,
    authenticate = fakeAuth,
    inputText = mockInputText,
    ngrCharacterCountComponent = mockNGRCharacterCountComponent,
    mcc = mcc,
    getData = fakeDataProperty(Some(property), answers),
    sessionRepository = mockSessionRepository,
    navigator = mockNavigator,
    )(mockConfig)

  val parkingSpacesOrGaragesNotIncludedInYourRentAnswers: Option[UserAnswers] = UserAnswers("id").set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, ParkingSpacesOrGaragesNotIncludedInYourRent(1, 2, 3, 2000, NGRDate(day = "01",month = "10",year = "2025"))).toOption

  "ParkingSpacesOrGaragesNotIncludedInYourRentController" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(parkingSpacesOrGaragesNotIncludedInYourRentAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=uncoveredSpaces]").attr("value") mustEqual  "1"
        document.select("input[name=coveredSpaces]").attr("value") mustEqual "2"
        document.select("input[name=garages]").attr("value")  mustEqual "3"
        document.select("#totalCost").attr("value")  mustEqual "2000"
        document.select("#agreementDate\\.day").attr("value")  mustEqual "01"
        document.select("#agreementDate\\.month").attr("value")  mustEqual "10"
        document.select("#agreementDate\\.year").attr("value")  mustEqual "2025"
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
      "Return OK and the correct view" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest =  FakeRequest(routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "1",
            "coveredSpaces" -> "0",
            "garages" -> "0",
            "totalCost" -> "2000",
            "agreementDate.day" -> "01",
            "agreementDate.month" -> "10",
            "agreementDate.year" -> "2025"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.RepairsAndInsuranceController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for inputting 0 in all parking space fields and show the correct view" in {
        val fakePostRequest =  FakeRequest(routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "0",
            "coveredSpaces" -> "0",
            "garages" -> "0",
            "totalCost" -> "2000",
            "agreementDate.day" -> "01",
            "agreementDate.month" -> "10",
            "agreementDate.year" -> "2025"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("")
      }
      "Return BAD_REQUEST for not inputting a total cost and show the correct view" in {
        val fakePostRequest = FakeRequest(routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "1",
            "coveredSpaces" -> "0",
            "garages" -> "0",
            "totalCost" -> "",
            "agreementDate.day" -> "01",
            "agreementDate.month" -> "10",
            "agreementDate.year" -> "2025"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return BAD_REQUEST for not inputting a day and show the correct view" in {
        val fakePostRequest = FakeRequest(routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "1",
            "coveredSpaces" -> "0",
            "garages" -> "0",
            "totalCost" -> "1000",
            "agreementDate.day" -> "",
            "agreementDate.month" -> "10",
            "agreementDate.year" -> "2025"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
      }
      "Return Exception if no address is in the mongo" in {
        val fakePostRequest = FakeRequest(routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "uncoveredSpaces" -> "",
            "coveredSpaces" -> "0",
            "garages" -> "0",
            "totalCost" -> "2000",
            "agreementDate.day" -> "01",
            "agreementDate.month" -> "10",
            "agreementDate.year" -> "2025"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest)))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}

