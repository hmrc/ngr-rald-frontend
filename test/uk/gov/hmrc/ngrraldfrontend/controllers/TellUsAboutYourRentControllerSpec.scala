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

import org.scalatest.matchers.should.Matchers.shouldBe
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{AssessmentId, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.views.html.TellUsAboutYourAgreementView

import java.time.Instant
import scala.concurrent.Future

class TellUsAboutYourRentControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Tell us about your rent review"
  val view: TellUsAboutYourAgreementView = inject[TellUsAboutYourAgreementView]
  val controllerProperty: Option[UserAnswers] => TellUsAboutRentController = (answers: Option[UserAnswers]) => new TellUsAboutRentController(view, fakeAuth, mockNavigator, mcc, fakeDataProperty(Some(property), answers), mockSessionRepository)(mockConfig)
  val controllerNoProperty: TellUsAboutRentController = new TellUsAboutRentController(view, fakeAuth, mockNavigator, mcc, fakeData(None), mockSessionRepository)(mockConfig)

  "Tell us about your rent controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).show(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return NotFoundException when property is not found in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
    "method submit" must {
      "Return SEE_OTHER and the correct view" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.LandlordController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view when user resumes rent review journey" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(Some(UserAnswers(credId, Json.obj(
          "tellUsAboutRent" -> "RentAgreement",
          "landlord" -> Json.obj(
            "landlordName" -> "Anna"
          )
        ), Instant.now))).submit(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.LandlordController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view when user switched to rent review journey" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(Some(UserAnswers(credId, Json.obj(
          "tellUsAboutRenewedAgreement" -> "RenewedAgreement",
          "whatTypeOfLeaseRenewal" -> "SurrenderAndRenewal"
        ), Instant.now))).submit(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.LandlordController.show(NormalMode).url)
      }
    }
  }
}
