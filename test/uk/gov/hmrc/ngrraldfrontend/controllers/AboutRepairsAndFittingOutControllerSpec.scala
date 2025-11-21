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


import uk.gov.hmrc.ngrraldfrontend.models.{AboutRepairsAndFittingOut, NGRMonthYear, UserAnswers, NormalMode}
import uk.gov.hmrc.ngrraldfrontend.models.forms.AboutRepairsAndFittingOutForm
import uk.gov.hmrc.ngrraldfrontend.pages.AboutRepairsAndFittingOutPage
import uk.gov.hmrc.ngrraldfrontend.views.html.AboutRepairsAndFittingOutView
import org.jsoup.Jsoup
import play.api.http.Status.{BAD_REQUEST, OK, SEE_OTHER}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, redirectLocation, status}
import uk.gov.hmrc.http.{HeaderNames, NotFoundException}
import uk.gov.hmrc.ngrraldfrontend.helpers.ControllerSpecSupport
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId

import scala.concurrent.Future

class AboutRepairsAndFittingOutControllerSpec extends ControllerSpecSupport {

  val pageTitle = "About repairs and fitting out"
  val caption = "A, RODLEY LANE, RODLEY, LEEDS, BH1 7EY"
  val hintText = "You only need to give us an approximate date, for example, 9 2025"

  val view: AboutRepairsAndFittingOutView = inject[AboutRepairsAndFittingOutView]

  val controllerNoProperty: AboutRepairsAndFittingOutController = new AboutRepairsAndFittingOutController(
    view = view,
    authenticate = mockAuthJourney,
    getData = fakeData(None),
    checkRequestSentReference = mockCheckRequestSentReference,
    sessionRepository = mockSessionRepository,
    navigator = mockNavigator,
    mcc = mcc
  )(mockConfig, ec)

  val controllerWithProperty: Option[UserAnswers] => AboutRepairsAndFittingOutController = answers =>
    new AboutRepairsAndFittingOutController(
      view = view,
      authenticate = mockAuthJourney,
      getData = fakeDataProperty(Some(property), answers),
      checkRequestSentReference = mockCheckRequestSentReference,
      sessionRepository = mockSessionRepository,
      navigator = mockNavigator,
      mcc = mcc
    )(mockConfig, ec)


  "AboutRepairsAndFittingOutController" must {

    "method show" must {

      "return OK and the correct view" in {
        val result = controllerWithProperty(None).show(NormalMode)(authenticatedFakeRequest)
        status(result) mustBe OK
        val document = Jsoup.parse(contentAsString(result))
        document.title() mustBe s"$pageTitle - GOV.UK"
        document.select("h1").text() mustBe pageTitle
        document.select("span.govuk-caption-m").text() mustBe caption
        document.select("#date-hint").text() mustBe hintText
      }

      "return OK and the correct view with prepopulated answers" in {
        val form = AboutRepairsAndFittingOutForm.form.fill(
          AboutRepairsAndFittingOutForm(
            cost = BigDecimal("1234.56"),
            date = NGRMonthYear("01", "1990")
          )
        )

        val html = view(form, property.addressFull, NormalMode)(authenticatedFakeRequest, messages, mockConfig)
        val document = Jsoup.parse(html.body)

        document.select("input[name=cost]").attr("value") mustBe "1234.56"
        document.select("input[name=date.month]").attr("value") mustBe "01"
        document.select("input[name=date.year]").attr("value") mustBe "1990"
      }

      "throw NotFoundException when property is missing" in {
        when(mockNGRConnector.getLinkedProperty(any[CredId])(any())).thenReturn(Future.successful(None))
        val exception = intercept[NotFoundException] {
          await(controllerNoProperty.show(NormalMode)(authenticatedFakeRequest))
        }
        exception.getMessage must include("Could not find answers in backend mongo")
      }

      "method submit" must {
        "redirect to next page on valid submission" in {
          when(mockSessionRepository.set(any())).thenReturn(Future.successful(true))
          val request = FakeRequest(routes.AboutRepairsAndFittingOutController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "cost" -> "1234.56",
              "date.month" -> "01",
              "date.year" -> "1990"
            )
            .withHeaders(HeaderNames.authorisation -> "Bearer 1")

          val result = controllerWithProperty(None).submit(NormalMode)(authenticatedFakePostRequest(request))
          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(routes.DidYouGetMoneyFromLandlordController.show(NormalMode).url)

        }

        "return BAD_REQUEST when cost is missing" in {
          val request = FakeRequest(routes.AboutRepairsAndFittingOutController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "cost" -> "",
              "date.month" -> "01",
              "date.year" -> "1990"
            )
            .withHeaders(HeaderNames.authorisation -> "Bearer 1")

          val result = controllerWithProperty(None).submit(NormalMode)(authenticatedFakePostRequest(request))
          status(result) mustBe BAD_REQUEST
          contentAsString(result) must include("Enter the repairs or fitting out cost (excluding VAT), in pounds")
        }

        "return BAD_REQUEST when month is missing" in {
          val request = FakeRequest(routes.AboutRepairsAndFittingOutController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "cost" -> "1234.56",
              "date.month" -> "",
              "date.year" -> "1990"
            )
            .withHeaders(HeaderNames.authorisation -> "Bearer 1")

          val result = controllerWithProperty(None).submit(NormalMode)(authenticatedFakePostRequest(request))
          status(result) mustBe BAD_REQUEST
          contentAsString(result) must include("Date you did the repairs or fitting out must include a month or year")
        }

        "return BAD_REQUEST when year is missing" in {
          val request = FakeRequest(routes.AboutRepairsAndFittingOutController.submit(NormalMode))
            .withFormUrlEncodedBody(
              "cost" -> "1234.56",
              "date.month" -> "01",
              "date.year" -> ""
            )
            .withHeaders(HeaderNames.authorisation -> "Bearer 1")

          val result = controllerWithProperty(None).submit(NormalMode)(authenticatedFakePostRequest(request))
          status(result) mustBe BAD_REQUEST
          contentAsString(result) must include("Date you did the repairs or fitting out must include a month or year")
        }

        "throw NotFoundException when property is missing on submit" in {
          val request = FakeRequest(routes.AboutRepairsAndFittingOutController.submit(NormalMode))
            .withFormUrlEncodedBody("cost" -> "1234.56")
            .withHeaders(HeaderNames.authorisation -> "Bearer 1")

          val exception = intercept[NotFoundException] {
            await(controllerNoProperty.submit(NormalMode)(authenticatedFakePostRequest(request)))
          }
          exception.getMessage must include("Could not find answers in backend mongo")
        }
      }
    }
  }
}


