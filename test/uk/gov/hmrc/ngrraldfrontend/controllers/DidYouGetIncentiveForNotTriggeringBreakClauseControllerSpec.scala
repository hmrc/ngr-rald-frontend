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
import uk.gov.hmrc.ngrraldfrontend.models.Incentive.*
import uk.gov.hmrc.ngrraldfrontend.models.forms.DidYouGetIncentiveForNotTriggeringBreakClauseForm
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{AuthenticatedUserRequest, DidYouGetIncentiveForNotTriggeringBreakClause, Incentive, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.DidYouGetIncentiveForNotTriggeringBreakClausePage
import uk.gov.hmrc.ngrraldfrontend.views.html.DidYouGetIncentiveForNotTriggeringBreakClauseView

import scala.concurrent.Future

class DidYouGetIncentiveForNotTriggeringBreakClauseControllerSpec extends ControllerSpecSupport {

  val pageTitle = "Did you get incentive for not triggering the break clause?"
  val formProvider = new DidYouGetIncentiveForNotTriggeringBreakClauseForm()
  val view: DidYouGetIncentiveForNotTriggeringBreakClauseView = inject[DidYouGetIncentiveForNotTriggeringBreakClauseView]
  val controllerNoProperty: DidYouGetIncentiveForNotTriggeringBreakClauseController = new DidYouGetIncentiveForNotTriggeringBreakClauseController(view = view,authenticate =  fakeAuth, fakeData(None), formProvider = formProvider,sessionRepository = mockSessionRepository,navigator = mockNavigator,mcc = mcc)(mockConfig, ec)
  val userAnswers: Option[UserAnswers] => DidYouGetIncentiveForNotTriggeringBreakClauseController = answers => new DidYouGetIncentiveForNotTriggeringBreakClauseController(view = view,authenticate =  fakeAuth,getData = fakeDataProperty(Some(property), answers), formProvider = formProvider,sessionRepository = mockSessionRepository,navigator = mockNavigator,mcc = mcc)(mockConfig, ec)
  val YesLumpSumAnswers: Option[UserAnswers] = UserAnswers("id").set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesLumpSum))).toOption
  val YesLumpSumYesRentFreePeriodAnswers: Option[UserAnswers] = UserAnswers("id").set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesLumpSum, YesRentFreePeriod))).toOption
  val NoAnswer: Option[UserAnswers] = UserAnswers("id").set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(No))).toOption

  "Did You Get Incentive For Not Triggering Break Clause Controller" must {
    "method show" must {
      "Return OK and the correct view" in {
        val result = userAnswers(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return OK and the correct view with prepopulated answers with YesLumpSum incentive checked" in {
        val result = userAnswers(YesLumpSumAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("#incentive_0").hasAttr("checked") mustBe true
        document.select("#incentive_1").hasAttr("checked") mustBe false
        document.select("#incentive_2").hasAttr("checked") mustBe false
      }
      "Return OK and the correct view with prepopulated answers with YesLumpSum and YesRentFreePeriod incentive checked" in {
        val result = userAnswers(YesLumpSumYesRentFreePeriodAnswers).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("#incentive_0").hasAttr("checked") mustBe true
        document.select("#incentive_1").hasAttr("checked") mustBe true
        document.select("#incentive_2").hasAttr("checked") mustBe false
      }
      "Return OK and the correct view with prepopulated answers with no incentive checked" in {
        val result = userAnswers(NoAnswer).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val content = contentAsString(result)
        val document = Jsoup.parse(content)
        document.select("#incentive_0").hasAttr("checked") mustBe false
        document.select("#incentive_1").hasAttr("checked") mustBe false
        document.select("#incentive_3").hasAttr("checked") mustBe true
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
      "Return SEE_OTHER and the correct view after submitting with yesLumpSum" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = userAnswers(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(("incentive[0]" -> "yesLumpSum"))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/how-much-was-the-lump-sum")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchWasTheLumpSumController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view after submitting with yesLumpSum and YesRentFreePeriod" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = userAnswers(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "incentive[0]" -> "yesLumpSum",
            "incentive[1]" -> "yesRentFreePeriod"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/how-much-was-the-lump-sum")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchWasTheLumpSumController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view after submitting with YesRentFreePeriod" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = userAnswers(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "incentive[1]" -> "yesRentFreePeriod"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/about-the-rent-free-period")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.AboutTheRentFreePeriodController.show(NormalMode).url)
      }
      "Return SEE_OTHER and the correct view after submitting with No, I did not get an incentive" in {
        when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
        val result = userAnswers(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(
            "incentive[3]" -> "no"
          )
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/how-much-was-the-lump-sum")
        })
        status(result) mustBe SEE_OTHER
        redirectLocation(result) mustBe Some(routes.HowMuchWasTheLumpSumController.show(NormalMode).url)
      }
      "Return Form with Errors when no checkbox is selected" in {
        val result = userAnswers(None).submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.submit(NormalMode))
          .withFormUrlEncodedBody(("", ""))
          .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some(""))))
        result.map(result => {
          result.header.headers.get("Location") mustBe Some("/ngr-rald-frontend/did-you-get-incentive-for-not-triggering-break-clause")
        })
        status(result) mustBe BAD_REQUEST
        val content = contentAsString(result)
        content must include(pageTitle)
      }
      "Return Exception if no address is in the mongo" in {
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.submit(NormalMode)(AuthenticatedUserRequest(FakeRequest(routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.submit(NormalMode))
            .withFormUrlEncodedBody(("", ""))
            .withHeaders(HeaderNames.authorisation -> "Bearer 1"), None, None, None, Some(property), credId = Some(credId.value), None, None, nino = Nino(true, Some("")))))
        }
        exception.getMessage contains "Could not find answers in backend mongo" mustBe true
      }
    }
  }
}

