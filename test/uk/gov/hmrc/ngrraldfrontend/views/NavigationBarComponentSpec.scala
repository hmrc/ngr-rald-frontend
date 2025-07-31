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
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.mvc.Call
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.*
import uk.gov.hmrc.ngrraldfrontend.views.html.components.navigationBarComponent

class NavigationBarComponentSpec extends ViewBaseSpec {
  val injectedView: navigationBarComponent = injector.instanceOf[navigationBarComponent]

  val content: NavigationBarContent = NavBarPageContents.CreateNavBar(
    contents = NavBarContents(
      homePage = Some(true),
      messagesPage = Some(false),
      profileAndSettingsPage = Some(false),
      signOutPage = Some(true)
    ),
    currentPage = NavBarCurrentPage(homePage = true),
    notifications = Some(1)
  )

  val backLine = "Back"

  object Selectors {
    val backLine = " div > a"
  }

  "The Nav Bar template" when {
    "navigation bar should render correctly" in {
      injectedView.f(content, false)(request, messages).toString() must not be empty
      injectedView.render(content, false, request, messages).toString() must not be empty
    }

    "back link should be created when showBackLine sets to true" in {
      val view = injectedView(content, true)(request, messages)
      lazy implicit val document: Document = Jsoup.parse(view.body)

      elementText(Selectors.backLine) mustBe backLine
    }

    "back link should be missing when showBackLine sets to false" in {
      val htmlReader = injectedView.render(content, false, request, messages).toString()

      htmlReader contains "<a href=\"#\" class=\"govuk-back-link\" data-module=\"hmrc-back-link\">Back</a>" shouldBe false
    }

    "Links are populated correctly" in {
      content.navigationButtons.isDefined mustBe true
      content.accountHome.get shouldBe NavButton(fieldName = "HomePage", call = Call("GET", mockConfig.ngrDashboardUrl), messageKey = "nav.home", linkId = "Home", selected = true, notification = None)
      content.navigationButtons.get shouldBe Seq(NavButton(fieldName = "SignOutPage", call = Call("GET", mockConfig.ngrLogoutUrl), messageKey = "nav.signOut", linkId = "SignOut", selected = false, notification = None))
    }
  }
}
