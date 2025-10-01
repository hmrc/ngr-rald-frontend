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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.libs.json.Json
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.views.html.TellUsAboutYourAgreementView

import java.time.Instant
import scala.concurrent.Future

class TellUsAboutYourRenewedAgreementControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Tell us about your renewed agreement"
  val view: TellUsAboutYourAgreementView = inject[TellUsAboutYourAgreementView]
  val controllerNoProperty: TellUsAboutYourRenewedAgreementController = new TellUsAboutYourRenewedAgreementController(view, fakeAuth, mcc, fakeData(None), mockSessionRepository, mockNavigator)(mockConfig)
  val controllerProperty = (answers: Option[UserAnswers]) => new TellUsAboutYourRenewedAgreementController(view, fakeAuth, mcc, fakeDataProperty(Some(property), answers), mockSessionRepository, mockNavigator)(mockConfig)

  "Tell us about your new agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show()(authenticatedFakeRequest)
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
        val result = controllerProperty(None).submit()(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.WhatTypeOfLeaseRenewalController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view when user resumes renewed agreement journey" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(Some(UserAnswers(credId.value, Json.obj(
          "tellUsAboutRenewedAgreement" -> "RenewedAgreement",
          "whatTypeOfLeaseRenewal" -> "SurrenderAndRenewal"
        ), Instant.now))).submit()(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.WhatTypeOfLeaseRenewalController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view when user switches to renewed agreement journey" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(Some(UserAnswers(credId.value, Json.obj(
          "tellUsAboutYourNewAgreement" -> "NewAgreement",
          "landlord" -> Json.obj(
            "landlordName" -> "Anna"
          )
        ), Instant.now))).submit()(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.WhatTypeOfLeaseRenewalController.show(NormalMode).url)
      }
    }
  }
}
