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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.auth.core.Nino
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.ConfirmBreakClausePage
import uk.gov.hmrc.ngrraldfrontend.views.html.ConfirmBreakClauseView

import scala.concurrent.Future

class ConfirmBreakClauseControllerSpec extends ControllerSpecSupport {
  val pageTitle = "Did your agreement allow you to trigger a break clause?"
  val view: ConfirmBreakClauseView = inject[ConfirmBreakClauseView]
  val controllerNoProperty: ConfirmBreakClauseController = new ConfirmBreakClauseController(view, fakeAuth, fakeData(None), mockSessionRepository, mockNavigator, mcc)(mockConfig, ec)
  val controllerProperty: Option[UserAnswers] => ConfirmBreakClauseController = answers => new ConfirmBreakClauseController(view, fakeAuth, fakeDataProperty(Some(property),answers), mockSessionRepository, mockNavigator, mcc)(mockConfig, ec)
  val confirmBreakClauseAnswers: Option[UserAnswers] =  userAnswers.set(ConfirmBreakClausePage, true).toOption


  "Confirm Break Clause Controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = controllerProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "return OK and the correct view with prepopulated data" in {
        val result = controllerProperty(confirmBreakClauseAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("input[type=radio][name=confirmBreakClause-radio-value][value=true]").hasAttr("checked") mustBe true
        document.select("input[type=radio][name=confirmBreakClause-radio-value][value=false]").hasAttr("checked") mustBe false
      }
      "Return NotFoundException when property is not found in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }

    "method submit" must {
      "Return See_Other and the correct view after submitting yes" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ConfirmBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "confirmBreakClause-radio-value" -> "true",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord") //TODO this is currently going to the wrong page as the journey hasn't yet been completed
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(NormalMode).url)
      }
      "Return See_Other and the correct view after submitting no" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ConfirmBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "confirmBreakClause-radio-value" -> "false",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/has-anything-else-affected-the-rent") //TODO this is currently going to the wrong page as the journey hasn't yet been completed
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HasAnythingElseAffectedTheRentController.show(NormalMode).url)
      }
      "Return Form with Errors when no radio selection is input" in {
        val result = controllerProperty(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.ConfirmBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "confirmBreakClause-radio-value" -> "",
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/landlord") //TODO this is currently going to the wrong page as the journey hasn't yet been completed
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
        content must include("Select yes if you have a break clause is a term in your agreement that allows you to end the agreement early.")
      }
      
      "Return Exception if no address is in the mongo" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.LandlordController.submit(NormalMode))
            .withFormUrlEncodedBody(("what-type-of-agreement-radio", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}

