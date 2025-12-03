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
import uk.gov.hmrc.http.{HttpResponse, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.ngrraldfrontend.helpers.{ControllerSpecSupport, TestData}
import uk.gov.hmrc.ngrraldfrontend.models.registration.{CredId, RatepayerRegistrationValuation}
import uk.gov.hmrc.ngrraldfrontend.models.{PropertyLinkingUserAnswers, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.views.html.RentReviewDetailsSentView

import scala.concurrent.Future


class  RentReviewDetailsSentControllerSpec extends ControllerSpecSupport with DefaultAwaitTimeout {
  val pageTitleRenewedAgreement = "Renewed agreement details sent"
  val pageTitleNewAgreement = "New agreement details sent"
  val pageTitleRentAgreement = "Rent review details sent"
  val view: RentReviewDetailsSentView = inject[RentReviewDetailsSentView]
  val controller: RentReviewDetailsSentController = new RentReviewDetailsSentController(view, fakeAuth, mcc, fakeDataProperty(Some(property), Some(userAnswersWithoutData)), mockNGRConnector)
  val controllerNoProperty: RentReviewDetailsSentController = new RentReviewDetailsSentController(view, fakeAuth, mcc, fakeData(None), mockNGRConnector)
  lazy val filledController: Option[UserAnswers] => RentReviewDetailsSentController = answers => RentReviewDetailsSentController(view, fakeAuth, mcc, fakeDataProperty(Some(property), answers), mockNGRConnector)
  
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
        content must include(pageTitleRentAgreement)
      }


      "Return OK and the correct title for new agreement" in {
        when(mockNGRConnector.getRaldUserAnswers(any())(any()))
          .thenReturn(Future.successful(newAgreementAnswers))
        val result = filledController(newAgreementAnswers).confirmation()(authenticatedFakeRequestEmail)
        status(result) mustBe OK
        contentType(result) shouldBe Some("text/html")
        val content = contentAsString(result)
        content must include(pageTitleNewAgreement)
      }

      "Return OK and the correct title for renewed agreement" in {
        when(mockNGRConnector.getRaldUserAnswers(any())(any()))
          .thenReturn(Future.successful(renewedAgreementAnswers))
        val result = filledController(renewedAgreementAnswers).confirmation()(authenticatedFakeRequestEmail)
        status(result) mustBe OK
        contentType(result) shouldBe Some("text/html")
        val content = contentAsString(result)
        content must include(pageTitleRenewedAgreement)
      }
    }

    "Return NotFoundException when property is not found in the mongo" in {
      when(mockNGRConnector.getRaldUserAnswers(any[CredId])(any())).thenReturn(Future.successful(None))
      val exception = intercept[NotFoundException] {
        await(controllerNoProperty.confirmation(authenticatedFakeRequest))
      }
      exception.getMessage contains "Could not find answers in backend mongo" mustBe true
    }
  }
}