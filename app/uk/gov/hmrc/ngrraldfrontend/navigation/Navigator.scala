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

package uk.gov.hmrc.ngrraldfrontend.navigation

import play.api.mvc.Call
import uk.gov.hmrc.hmrcfrontend.controllers.routes
import uk.gov.hmrc.ngrraldfrontend.models.{CheckMode, Landlord, Mode, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{LandlordPage, Page, TellUsAboutRentPage, WhatTypeOfAgreementPage}

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case TellUsAboutRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show
    case LandlordPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
    mode match {
      case CheckMode => normalRoutes(page)(userAnswers)
      case NormalMode => normalRoutes(page)(userAnswers)
    }
      
  }

}
