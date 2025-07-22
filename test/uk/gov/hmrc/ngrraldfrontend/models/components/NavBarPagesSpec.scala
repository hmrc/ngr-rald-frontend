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

package uk.gov.hmrc.ngrraldfrontend.models.components

import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport

class NavBarPagesSpec extends TestSupport {

  "NavBarPageContents.CreateNavBar" should {
    
    "include HomePage and SignOutPage when set to true" in {
      val contents = NavBarContents(
        homePage = Some(true),
        messagesPage = Some(false),
        profileAndSettingsPage = Some(false),
        signOutPage = Some(true)
      )
      val currentPage = NavBarCurrentPage()

      val result = NavBarPageContents.CreateNavBar(contents, currentPage)(mockConfig)

      result.accountHome.isDefined mustBe true
      result.accountHome.get.linkId mustBe "Home"

      result.navigationButtons.isDefined mustBe true
      result.navigationButtons.get.exists(_.linkId == "SignOut") mustBe true
      result.navigationButtons.get.exists(_.linkId == "Messages") mustBe false
    }

    "not include buttons set to false or None in NavBarContents" in {
      val contents = NavBarContents(
        homePage = Some(false),
        messagesPage = None,
        profileAndSettingsPage = Some(false),
        signOutPage = Some(false)
      )
      val currentPage = NavBarCurrentPage()

      val result = NavBarPageContents.CreateNavBar(contents, currentPage)(mockConfig)

      result.accountHome mustBe None
      result.navigationButtons mustBe None
    }

    "apply selected state and notification correctly" in {
      val contents = NavBarContents(
        homePage = Some(true),
        messagesPage = Some(true),
        profileAndSettingsPage = Some(false),
        signOutPage = Some(true)
      )
      val currentPage = NavBarCurrentPage(messagesPage = true)

      val result = NavBarPageContents.CreateNavBar(contents, currentPage, notifications = Some(5))(mockConfig)

      val messagesButton = result.navigationButtons.get.find(_.linkId == "Messages").get
      messagesButton.selected mustBe true
      messagesButton.notification mustBe Some(5)
    }

    "only assign accountHome if HomePage is included" in {
      val contents = NavBarContents(
        homePage = Some(true),
        messagesPage = Some(true),
        profileAndSettingsPage = Some(true),
        signOutPage = Some(true)
      )
      val currentPage = NavBarCurrentPage()

      val result = NavBarPageContents.CreateNavBar(contents, currentPage)(mockConfig)

      result.accountHome.map(_.fieldName) mustBe Some("HomePage")
      result.navigationButtons.get.map(_.fieldName) must not contain "HomePage"
    }
  }
}