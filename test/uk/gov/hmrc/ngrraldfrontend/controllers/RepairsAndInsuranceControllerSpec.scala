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
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.any
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.views.html.RepairsAndInsuranceView
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, RepairsAndInsurance, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.RepairsAndInsurancePage

import scala.concurrent.Future

class RepairsAndInsuranceControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Repairs and insurance"

  val view: RepairsAndInsuranceView = inject[RepairsAndInsuranceView]
  val controller: RepairsAndInsuranceController = new RepairsAndInsuranceController(
    repairsAndInsuranceView = view,
    authenticate = fakeAuth,
    navigator = mockNavigator,
    getData = fakeData(None),
    sessionRepository = mockSessionRepository,
    mcc = mcc)(mockConfig)

  val controllerProperty: Option[UserAnswers] => RepairsAndInsuranceController = answers => new RepairsAndInsuranceController(
    repairsAndInsuranceView = view,
    authenticate = fakeAuth,
    navigator = mockNavigator,
    getData = fakeDataProperty(Some(property), answers),
    sessionRepository = mockSessionRepository,
    mcc = mcc)(mockConfig)

  val repairsAndInsuranceAnswers = UserAnswers("id").set(RepairsAndInsurancePage, repairsAndInsuranceModel).toOption

  "RepairsAndInsuranceController" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "return OK and the correct view with prepopulated answers" in {
        val result = controllerProperty(repairsAndInsuranceAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[name=repairsAndInsurance-internalRepairs-radio-value]").attr("value") mustEqual "InternalRepairsYou"
        document.select("input[name=repairsAndInsurance-externalRepairs-radio-value]").attr("value") mustEqual "ExternalRepairsYou"
        document.select("input[name=repairsAndInsurance-buildingInsurance-radio-value]").attr("value") mustEqual "BuildingInsuranceYou"
      }
      "Return NotFoundException when property is not found in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controller.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
    "method submit" must {
      "Return OK and the correct view" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "InternalRepairsYou",
            "repairsAndInsurance-externalRepairs-radio-value" -> "ExternalRepairsYou",
            "repairsAndInsurance-buildingInsurance-radio-value" -> "BuildingInsuranceYou"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")

        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ConfirmBreakClauseController.show(NormalMode).url)
      }
      "Return BAD_REQUEST for not selecting a internal repairs radio, showing the correct view and error" in {
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "",
            "repairsAndInsurance-externalRepairs-radio-value" -> "ExternalRepairsYou",
            "repairsAndInsurance-buildingInsurance-radio-value" -> "BuildingInsuranceYou"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Select who pays for internal repairs")
      }
      "Return BAD_REQUEST for not selecting a external repairs radio, showing the correct view and error" in {
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "InternalRepairsYou",
            "repairsAndInsurance-externalRepairs-radio-value" -> "",
            "repairsAndInsurance-buildingInsurance-radio-value" -> "BuildingInsuranceYou"
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Select who pays for external repairs")
      }
      "Return BAD_REQUEST for not selecting a building insurance radio, showing the correct view and error" in {
        val fakePostRequest = FakeRequest(routes.RepairsAndInsuranceController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "repairsAndInsurance-internalRepairs-radio-value" -> "InternalRepairsYou",
            "repairsAndInsurance-externalRepairs-radio-value" -> "ExternalRepairsYou",
            "repairsAndInsurance-buildingInsurance-radio-value" -> ""
          ).withHeaders(HeaderNames.authorisation -> "Bearer 1")
        val result = controllerProperty(None).submit(NormalMode)(authenticatedFakePostRequest(fakePostRequest))
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include("Select who pays for buildings insurance")
      }
    }
  }
}
