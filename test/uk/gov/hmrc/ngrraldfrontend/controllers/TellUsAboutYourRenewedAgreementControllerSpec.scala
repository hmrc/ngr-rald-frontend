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
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.RaldUserAnswers
import uk.gov.hmrc.ngrraldfrontend.views.html.TellUsAboutYourAgreementView

import scala.concurrent.Future

class TellUsAboutYourRenewedAgreementControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Tell us about your renewed agreement"
  val view: TellUsAboutYourAgreementView = inject[TellUsAboutYourAgreementView]
  val controller: TellUsAboutYourRenewedAgreementController = new TellUsAboutYourRenewedAgreementController(view, mockAuthJourney, mockPropertyLinkingAction, mockNgrConnector, mockRaldRepo, mcc)(mockConfig)

  "Tell us about your new agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        when(mockRaldRepo.upsertRaldUserAnswers(any())) thenReturn (Future.successful(true))
        when(mockNgrConnector.getLinkedProperty(any())(any()))thenReturn(Future.successful(true))
        when(mockRaldRepo.findByCredId(any())).thenReturn(Future.successful(Some(RaldUserAnswers(credId, property))))
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return NotFoundException when failing to connect to the backend" in {
        when(mockNgrConnector.getLinkedProperty(any())(any())) thenReturn (Future.successful(false))
        val exception = intercept[NotFoundException] {
          await(controller.show()(authenticatedFakeRequest()))
        }
        exception.getMessage contains "Couldn't connect to backend" mustBe true
      }
      "Return NotFoundException when property is not found in the mongo" in {
        when(mockRaldRepo.upsertRaldUserAnswers(any())) thenReturn (Future.successful(true))
        when(mockNgrConnector.getLinkedProperty(any())(any())) thenReturn (Future.successful(true))
        when(mockRaldRepo.findByCredId(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controller.show(authenticatedFakeRequest()))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }

    "method submit" must {
      "Return OK and the correct view" in {
        val result = controller.submit()(authenticatedFakeRequest())
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.TellUsAboutYourNewAgreementController.show.url)
      }
    }
  }
}
