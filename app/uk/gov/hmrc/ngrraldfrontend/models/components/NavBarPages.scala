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

import play.api.mvc.Call
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.controllers.routes

case class NavButton(
                      fieldName: String,
                      call: Call,
                      messageKey: String,
                      linkId: String,
                      notification: Option[Int],
                      selected: Boolean,
                    )

case class NavBarCurrentPage(
                              homePage: Boolean = false,
                              messagesPage: Boolean = false,
                              profileAndSettingsPage: Boolean = false,
                              signOutPage: Boolean = false,
                            )

case class NavBarContents(
                           homePage: Option[Boolean] = None,
                           messagesPage: Option[Boolean] = None,
                           profileAndSettingsPage: Option[Boolean] = None,
                           signOutPage: Option[Boolean] = None
                         )

case class NavigationBarContent(
                                 accountHome: Option[NavButton],
                                 navigationButtons: Option[Seq[NavButton]]
                               )

object NavBarPageContents {
  private val navBarContents: NavBarContents = NavBarContents(
    homePage = Some(true),
    messagesPage = Some(false),
    profileAndSettingsPage = Some(false),
    signOutPage = Some(true)
  )

  def createDefaultNavBar(implicit appConfig: AppConfig): NavigationBarContent = CreateNavBar(
    contents = navBarContents,
    currentPage = NavBarCurrentPage(),
    notifications = Some(1)
  )

  def CreateNavBar(contents: NavBarContents, currentPage: NavBarCurrentPage, notifications: Option[Int] = None)(implicit appConfig: AppConfig): NavigationBarContent = {

    // Define buttons
    val homePageButton     = NavButton(fieldName = "HomePage", call = Call("GET", appConfig.ngrDashboardUrl), messageKey = "nav.home", linkId = "Home", selected = currentPage.homePage, notification = None)
    val messagesPageButton = NavButton(fieldName = "MessagesPage", call = Call("GET", "/messages"), messageKey = "nav.messages", linkId = "Messages", selected = currentPage.messagesPage, notification = notifications)
    val profilePageButton  = NavButton(fieldName = "ProfileAndSettingsPage", call = Call("GET", ""), messageKey = "nav.profileAndSettings", linkId = "Profile", selected = currentPage.profileAndSettingsPage, notification = None)
    val signOutPageButton  = NavButton(fieldName = "SignOutPage", call = Call("GET", routes.BeforeYouGoController.signout.url), messageKey = "nav.signOut", linkId = "SignOut", selected = currentPage.signOutPage, notification = None)

    // Map fields to their NavButton equivalents
    val buttonMapping = Seq(
      "homePage"           -> (contents.homePage, homePageButton),
      "messagesPage"       -> (contents.messagesPage, messagesPageButton),
      "profileAndSettings" -> (contents.profileAndSettingsPage, profilePageButton),
      "signOutPage"        -> (contents.signOutPage, signOutPageButton)
    )

    // Filter buttons based on their corresponding content value
    val filteredButtons = buttonMapping.collect {
      case (_, (Some(true), button)) => button
    }

    // Ensure HomePage is always first if it exists
    val (homePageButtons, otherButtons) = filteredButtons.partition(_.fieldName == "HomePage")
    val sortedButtons = homePageButtons ++ otherButtons

    // Create the NavigationBarContent
    NavigationBarContent(
      accountHome = homePageButtons.headOption,
      navigationButtons = if (sortedButtons.size > 1) Some(sortedButtons.tail) else None
    )
  }
}
