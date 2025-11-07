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
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.HttpResponse
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.UserAnswers
import uk.gov.hmrc.ngrraldfrontend.views.html.DeclarationView

import scala.concurrent.Future

class DeclaratioinControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Declaration"
  val view: DeclarationView = inject[DeclarationView]
  val controllerNoProperty: DeclarationController = new DeclarationController(view, fakeAuth, fakeData(None), mockNavigator, mockSessionRepository, mockNGRConnector, mcc)
  val controllerProperty: Option[UserAnswers] => DeclarationController = answers => new DeclarationController(view, fakeAuth, fakeDataProperty(Some(property), answers), mockNavigator, mockSessionRepository, mockNGRConnector, mcc)

  "Declaration controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show()(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
    }
    "method submit" must {
      "Return SEE_OTHER and the correct view" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockNGRConnector.upsertRaldUserAnswers(any())(any())).thenReturn(Future.successful(HttpResponse(CREATED, "Created Successfully")))
        val result = controllerProperty(None).submit(authenticatedFakeRequest)
        status(result) mustBe SEE_OTHER
        redirectLocation(result) shouldBe Some(routes.DeclarationController.show.url)
      }
      "Return Exception when fail to store send request reference user answers" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(false))
        val exception = intercept[Exception] {
          await(controllerProperty(None).submit(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not save reference for credId: 1234" mustBe true
      }
      "Return Exception when fail to store user answers to backend mongoDB" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        when(mockNGRConnector.upsertRaldUserAnswers(any())(any())).thenReturn(Future.successful(HttpResponse(INTERNAL_SERVER_ERROR, "Internal server error")))
        val exception = intercept[Exception] {
          await(controllerProperty(None).submit(authenticatedFakeRequest))
        }
        exception.getMessage contains "Failed upsert to backend for credId: 1234" mustBe true
      }
    }
  }
}
