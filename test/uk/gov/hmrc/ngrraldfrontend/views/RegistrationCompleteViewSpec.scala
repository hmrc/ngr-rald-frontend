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
import org.scalatestplus.mockito.MockitoSugar
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.views.html.{Layout, RentReviewDetailsSentView}

class RegistrationCompleteViewSpec extends ViewBaseSpec {
  val layout: Layout = MockitoSugar.mock[Layout]
  lazy val testView: RentReviewDetailsSentView = inject[RentReviewDetailsSentView]
  val title = "Manage your business rates valuation"
  val heading = "Registration Successful"
  val body1Id = "Your service recovery number is 12345"
  val bodyP2 = "We’ve sent details about this registration to testEmail@emailProvider.com"
  val bodyP3 = "We’ve also sent a welcome email which has a guide to using your account."
  val bodyP4 = "Your service recovery number is 12345. You will need this if you have a problem signing in to the service using Government Gateway."
  val bodyH2 = "What happens next"
  val bodyP5 = "Use your Government Gateway ID details next time you sign in to your manage your business rates valuation account"
  val buttonText = "Go to account home"
  val printLinkText = "Print this page"

  object Selectors {
    val navTitle = ".govuk-header__service-name"
    val headingSelector = "#main-content > div > div > form > div > h1"
    val body1Selector = "#main-content > div > div > form > div > div"
    val bodyEmailSelector = "#main-content > div > div > form > p:nth-child(3)"
    val body2Selector = "#main-content > div > div > form > p:nth-child(4)"
    val body3Selector = "#main-content > div > div > form > p:nth-child(5)"
    val body5Selector = "#main-content > div > div > form > p:nth-child(7)"
    val bodyH2Selector = "#main-content > div > div > form > h2"
    val backLink = ".govuk-back-link"
    val button = "#continue"
    val printLink   = "#printPage > a"
  }

    "The RentReviewDetailsSentView view" should {
      "Render a page with the appropriate message" when {
        "a recovery ID is present" in {
          val htmlApply = testView.apply(Some("12345"), "testEmail@emailProvider.com").body
          val htmlRender = testView.render(Some("12345"), "testEmail@emailProvider.com", request, messages, mockConfig).body
          val htmlF = testView.f(Some("12345"), "testEmail@emailProvider.com")(request, messages, mockConfig).body
          htmlF must not be empty
          htmlApply mustBe htmlRender
          htmlApply.contains(bodyP2) mustBe true
          lazy implicit val document: Document = Jsoup.parse(testView(Some("12345"), "testEmail@emailProvider.com")(request, messages, mockConfig).body)
          elementText(Selectors.navTitle) mustBe title
          elementText(Selectors.headingSelector) mustBe heading
          elementText(Selectors.body1Selector) mustBe body1Id
          elementText(Selectors.bodyEmailSelector) mustBe bodyP2
          elementText(Selectors.body2Selector) mustBe bodyP3
          elementText(Selectors.body3Selector) mustBe bodyP4
          elementText(Selectors.body5Selector) mustBe bodyP5
          elementText(Selectors.bodyH2Selector) mustBe bodyH2
          elementText(Selectors.button) mustBe buttonText
          elementText(Selectors.printLink) mustBe printLinkText

        }
      }
    }
}