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

package uk.gov.hmrc.ngrraldfrontend.views

import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import uk.gov.hmrc.ngrraldfrontend.controllers.routes
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.views.html.BeforeYouGoView

class BeforeYouGoViewSpec extends ViewBaseSpec {
  lazy val view: BeforeYouGoView = inject[BeforeYouGoView]
  val title = "You have signed out - GOV.UK"
  val heading = "You have signed out"
  val signInHref = "Sign in to the service"
  val subHeading = "Before you go"
  val body1 = "Your feedback helps us make our service better."
  val feedbackHref = "Take a short survey"
  val body2 = "Take a short survey to share your feedback on this service."

  object Selectors {
    val navTitle = "head > title"
    val heading = "#main-content > div > div > h1"
    val signIn = "#main-content > div > div > p:nth-child(3) > a"
    val subHeading = "#main-content > div > div > h2"
    val body1 = "#main-content > div > div > p:nth-child(5)"
    val feedback = "#main-content > div > div > p:nth-child(6) > a"
    val body2 = "#main-content > div > div > p:nth-child(6)"
  }

  "BeforeYouGoView" must {
    val beforeYouGoView = view(mockConfig.ngrDashboardUrl, routes.BeforeYouGoController.feedback.url)
    lazy implicit val document: Document = Jsoup.parse(beforeYouGoView.body)
    val htmlApply = view.apply(mockConfig.ngrDashboardUrl, routes.BeforeYouGoController.feedback.url).body
    val htmlRender = view.render(mockConfig.ngrDashboardUrl, routes.BeforeYouGoController.feedback.url, request, messages, mockConfig).body
    lazy val htmlF = view.f(mockConfig.ngrDashboardUrl, routes.BeforeYouGoController.feedback.url)

    "htmlF is not empty" in {
      htmlF.toString() must not be empty
    }

    "apply must be the same as render" in {
      htmlApply mustBe htmlRender
    }

    "render is not empty" in {
      htmlRender must not be empty
    }

    "show correct title" in {
      elementText(Selectors.navTitle) mustBe title
    }

    "show correct heading" in {
      elementText(Selectors.heading) mustBe heading
    }

    "show correct sign in href" in {
      elementText(Selectors.signIn) mustBe signInHref
    }

    "show correct feedback survey" in {
      elementText(Selectors.feedback) mustBe feedbackHref
    }

    "show correct sub heading" in {
      elementText(Selectors.subHeading) mustBe subHeading
    }

    "show correct body1" in {
      elementText(Selectors.body1) mustBe body1
    }

    "show correct body2" in {
      elementText(Selectors.body2) mustBe body2
    }
  }

}
