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
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.models.{CheckMode, Mode, NormalMode, ProvideDetailsOfFirstSecondRentPeriod, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{AgreedRentChangePage, DidYouAgreeRentWithLandlordPage, HowMuchIsTotalAnnualRentPage, LandlordPage, Page, ProvideDetailsOfFirstSecondRentPeriodPage, RentPeriodsPage, TellUsAboutRentPage, TellUsAboutYourRenewedAgreementPage, WhatIsYourRentBasedOnPage, WhatTypeOfAgreementPage, WhatTypeOfLeaseRenewalPage}

import java.lang.ProcessBuilder.Redirect
import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {



  private val normalRoutes: Page => UserAnswers => Call = {
    case TellUsAboutRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show
    case TellUsAboutYourRenewedAgreementPage =>_ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfLeaseRenewalController.show
    case WhatTypeOfLeaseRenewalPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show
    case LandlordPage => answers => (answers.get(TellUsAboutRentPage),answers.get(TellUsAboutYourRenewedAgreementPage)) match {
      case (Some(_),None) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show
      case (None, Some(_)) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show
      case (Some(_), Some(_)) => throw new RuntimeException("User should not have all three options")
      case (None, None) => throw new NotFoundException("Failed to find values")
    }
    case WhatTypeOfAgreementPage => answers =>
      answers.get(WhatTypeOfAgreementPage) match {
        case Some(value) => value match {
          case "Verbal" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementVerbalController.show
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show
        }
        case None => throw new NotFoundException("Failed to find value from What type of agreement page")
      }
    case WhatIsYourRentBasedOnPage => answers =>
      answers.get(WhatIsYourRentBasedOnPage) match {
        case Some(value) => value.rentBased match {
          case "PercentageTurnover" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show
        }
        case None => throw new NotFoundException("Not found answers")
      }
    case AgreedRentChangePage => answers =>
      answers.get(AgreedRentChangePage) match {
        case Some(value) => value match {
          case "Yes" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstSecondRentPeriodController.show
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case HowMuchIsTotalAnnualRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show
    case DidYouAgreeRentWithLandlordPage => answers =>
      answers.get(DidYouAgreeRentWithLandlordPage) match {
        case Some(value) => value match {
          case "YesTheLandlord" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case ProvideDetailsOfFirstSecondRentPeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentPeriodsController.show
    case RentPeriodsPage => answers =>
      answers.get(RentPeriodsPage) match {
        case Some(value) => value match {
          case "Yes" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstSecondRentPeriodController.show
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = {
     normalRoutes(page)(userAnswers)
  }
}
