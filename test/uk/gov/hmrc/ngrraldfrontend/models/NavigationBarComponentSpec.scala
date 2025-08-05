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

package uk.gov.hmrc.ngrraldfrontend.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.mvc.Call
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.helpers.ViewBaseSpec
import uk.gov.hmrc.ngrraldfrontend.models.components.{NavBarContents, NavBarCurrentPage, NavBarPageContents, NavButton}

class NavigationBarComponentSpec extends ViewBaseSpec {

  "NavButton" should {
    "store all fields correctly" in {
      val call = Call("GET", "/test")
      val button = NavButton("TestField", call, "test.key", "TestLink", Some(5), selected = true)

      button.fieldName shouldBe "TestField"
      button.call.method shouldBe "GET"
      button.call.url shouldBe "/test"
      button.messageKey shouldBe "test.key"
      button.linkId shouldBe "TestLink"
      button.notification shouldBe Some(5)
      button.selected shouldBe true
    }
  }

  "NavBarCurrentPage" should {
    "default all pages to false" in {
      val currentPage = NavBarCurrentPage()

      currentPage.homePage shouldBe false
      currentPage.messagesPage shouldBe false
      currentPage.profileAndSettingsPage shouldBe false
      currentPage.signOutPage shouldBe false
    }
  }

  "NavBarContents" should {
    "store optional page flags correctly" in {
      val contents = NavBarContents(
        homePage = Some(true),
        messagesPage = Some(false),
        profileAndSettingsPage = None,
        signOutPage = Some(true)
      )

      contents.homePage shouldBe Some(true)
      contents.messagesPage shouldBe Some(false)
      contents.profileAndSettingsPage shouldBe None
      contents.signOutPage shouldBe Some(true)
    }
  }

  "CreateNavBar" should {
    "generate correct NavigationBarContent" in {
      val contents = NavBarContents(
        homePage = Some(true),
        messagesPage = Some(true),
        profileAndSettingsPage = Some(false),
        signOutPage = Some(true)
      )

      val currentPage = NavBarCurrentPage(
        homePage = true,
        messagesPage = false,
        profileAndSettingsPage = false,
        signOutPage = false
      )

      val navBar = NavBarPageContents.CreateNavBar(contents, currentPage, notifications = Some(3))

      navBar.accountHome shouldBe defined
      navBar.accountHome.get.fieldName shouldBe "HomePage"

      navBar.navigationButtons shouldBe defined
      navBar.navigationButtons.get.map(_.fieldName) must contain allOf("MessagesPage", "SignOutPage")
      navBar.navigationButtons.get.exists(_.notification.contains(3)) shouldBe true
    }
  }

  "createDefaultNavBar" should{
    "use default NavBarContents and NavBarCurrentPage" in {
      val defaultNavBar = NavBarPageContents.createDefaultNavBar

      defaultNavBar.accountHome shouldBe defined
      defaultNavBar.accountHome.get.fieldName shouldBe "HomePage"

      defaultNavBar.navigationButtons shouldBe defined
      defaultNavBar.navigationButtons.get.map(_.fieldName) must contain only ("SignOutPage")
    }
  }
}

