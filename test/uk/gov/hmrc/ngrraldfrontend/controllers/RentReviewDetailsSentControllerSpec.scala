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
import play.api.http.Status.{CREATED, INTERNAL_SERVER_ERROR, OK, SEE_OTHER}
import play.api.test.DefaultAwaitTimeout
import play.api.test.Helpers.{await, contentAsString, contentType, redirectLocation, status}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.ngrraldfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.ngrraldfrontend.helpers.{ControllerSpecSupport, TestData}
import uk.gov.hmrc.ngrraldfrontend.models.{PropertyLinkingUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.registration.{CredId, RatepayerRegistrationValuation}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentReviewDetailsSentView

import scala.concurrent.Future


class  RentReviewDetailsSentControllerSpec extends ControllerSpecSupport with DefaultAwaitTimeout {
  val pageTitle = "Renewed agreement details sent"
  val view: RentReviewDetailsSentView = inject[RentReviewDetailsSentView]
  val controller: RentReviewDetailsSentController = new RentReviewDetailsSentController(view, fakeAuth, mcc, fakeDataProperty(Some(property), Some(userAnswersWithoutData)), mockNGRConnector)

  "RentReviewDetailsSent Controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val response: Option[UserAnswers] = Some(userAnswersWithoutData)
        when(mockNGRConnector.getRaldUserAnswers(any())(any()))
          .thenReturn(Future.successful(response))
        val result = controller.confirmation()(authenticatedFakeRequestEmail)
        status(result) mustBe OK
        contentType(result) shouldBe Some("text/html")
        val content = contentAsString(result)
        content must include(pageTitle)
      }
    }
  }
}