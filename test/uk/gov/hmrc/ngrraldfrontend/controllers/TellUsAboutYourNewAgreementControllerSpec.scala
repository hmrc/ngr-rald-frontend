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

import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.http.Status.{OK, SEE_OTHER}
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.NewAgreement
import uk.gov.hmrc.ngrraldfrontend.models.RaldUserAnswers
import uk.gov.hmrc.ngrraldfrontend.views.html.TellUsAboutYourAgreementView

import scala.concurrent.Future

class TellUsAboutYourNewAgreementControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Tell us about your new agreement"
  val view: TellUsAboutYourAgreementView = inject[TellUsAboutYourAgreementView]
  val controller: TellUsAboutYourNewAgreementController = new TellUsAboutYourNewAgreementController(view, mockAuthJourney, mockPropertyLinkingAction, mockRaldRepo, mcc)(mockConfig)

  "Tell us about your new agreement controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controller.show()(authenticatedFakeRequest())
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return NotFoundException when property is not found in the mongo" in {
        mockRequestWithoutProperty()
        val exception = intercept[NotFoundException] {
          await(controller.show(authenticatedFakeRequest()))
        }
        exception.getMessage contains "Couldn't find property in mongo" mustBe true
      }
    }
    "method submit" must {
      "Return OK and the correct view" in {
        val result = controller.submit()(authenticatedFakeRequest())
        when(mockRaldRepo.upsertRaldUserAnswers(
          raldUserAnswers = RaldUserAnswers(credId,
            NewAgreement,
            property,
            None)
        )).thenReturn(Future.successful(true))
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.LandlordController.show.url)
      }
    }
  }
}
