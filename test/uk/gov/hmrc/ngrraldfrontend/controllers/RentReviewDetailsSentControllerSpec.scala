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
import play.api.test.Helpers.{await, contentAsString, redirectLocation, status}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.ngrraldfrontend.config.{AppConfig, FrontendAppConfig}
import uk.gov.hmrc.ngrraldfrontend.helpers.{ControllerSpecSupport, TestData}
import uk.gov.hmrc.ngrraldfrontend.models.PropertyLinkingUserAnswers
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.views.html.DeclarationView

import scala.concurrent.Future

class DeclarationControllerSpec extends ControllerSpecSupport with DefaultAwaitTimeout {
  lazy val view: DeclarationView = inject[DeclarationView]
  def controller() = new DeclarationController(
    view,
    mockAuthJourney,
    mockMandatoryCheck,
    mockPropertyLinkingRepo,
    mockNgrConnector,
    mcc
  )

  val baseAnswers: PropertyLinkingUserAnswers = PropertyLinkingUserAnswers(
    credId = CredId(testCredId.providerId),
    vmvProperty = properties1.properties.head
  )

  "DeclarationController" must {
    "Return OK and the correct view" in {
      val result = controller().show()(authenticatedFakeRequest)
      status(result) mustBe OK
      val content = contentAsString(result)
      content must include("Declaration")
    }


    "method accept" must {
      "Return OK and the correct view" in {
        when(mockPropertyLinkingRepo.insertRequestSentReference(any(), any())).thenReturn(Future.successful(Some(PropertyLinkingUserAnswers(credId = CredId(null), vmvProperty = testVmvProperty))))
        when(mockNgrConnector.upsertPropertyLinkingUserAnswers(any())(any())).thenReturn(Future.successful(HttpResponse(CREATED, "Created Successfully")))
        val result = controller().accept()(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.AddPropertyRequestSentController.show.url)
      }
      "Throw exception when fail to upsert property linking to the backend" in {
        when(mockPropertyLinkingRepo.insertRequestSentReference(any(), any())).thenReturn(Future.successful(Some(PropertyLinkingUserAnswers(credId = CredId(null), vmvProperty = testVmvProperty))))
        when(mockNgrConnector.upsertPropertyLinkingUserAnswers(any())(any())).thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))
        mockMandatoryCheckRequest()
        val exception = intercept[Exception] {
          await(controller().accept(authenticatedFakeRequest))
        }
        exception.getMessage contains "Failed upsert to backend for credId: 1234" mustBe true
      }
    }

  }
}