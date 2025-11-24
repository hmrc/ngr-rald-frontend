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
import play.api.http.Status.*
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.{CheckMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.DidYouPayAnyMoneyToLandlordPage
import uk.gov.hmrc.ngrraldfrontend.views.html.CheckAnswersView

class CheckAnswersControllerSpec extends ControllerSpecSupport {

  val pageTitle = "Check your answers"
  val view: CheckAnswersView = inject[CheckAnswersView]

  val controllerNoProperty: CheckAnswersController =
    new CheckAnswersController(view, mockAuthJourney, fakeData(None), mockCheckRequestSentReference, mockSessionRepository, mockNavigator, mcc)(mockConfig, ec)

  val controllerProperty: Option[UserAnswers] => CheckAnswersController = answers =>
    new CheckAnswersController(view, mockAuthJourney, fakeDataProperty(Some(property), answers), mockCheckRequestSentReference, mockSessionRepository, mockNavigator, mcc)(mockConfig, ec)

  val didYouPayAnyMoneyToLandlordAnswers: Option[UserAnswers] =
    userAnswersWithoutData.set(DidYouPayAnyMoneyToLandlordPage, true).toOption

  "show" should {
    "return OK and render the view with empty summaries when property exists but no answers" in {
      val controller = controllerProperty(None)
      val result = controller.show(CheckMode)(fakeRequest)

      status(result) mustBe OK
      contentAsString(result) must include(pageTitle)
    }
  }
  "method submit" must {
    "Return SEE_OTHER and the correct view" in {
      val result = controllerProperty(didYouPayAnyMoneyToLandlordAnswers).submit(authenticatedFakeRequest)
      status(result) mustBe SEE_OTHER
      redirectLocation(result) shouldBe Some(routes.DeclarationController.show.url)
    }
    "Return NotFoundException when can't find userAnswers from request" in {
      val exception = intercept[NotFoundException] {
        await(controllerProperty(None).submit(authenticatedFakeRequest))
      }
      exception.getMessage contains "Failed to find answers for credId: 1234" mustBe true
    }
  }
}
