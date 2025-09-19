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
import controllers.routes
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.models.{CheckMode, Mode, NormalMode, ProvideDetailsOfFirstSecondRentPeriod, UserAnswers, WhatYourRentIncludes}
import uk.gov.hmrc.ngrraldfrontend.pages._

import javax.inject.{Inject, Singleton}

@Singleton
class Navigator @Inject()() {

  private val normalRoutes: Page => UserAnswers => Call = {
    case TellUsAboutRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(NormalMode)
    case TellUsAboutYourRenewedAgreementPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfLeaseRenewalController.show(NormalMode)
    case TellUsAboutYourNewAgreementPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(NormalMode)
    case WhatTypeOfLeaseRenewalPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(NormalMode)
    case LandlordPage => answers => (
      answers.get(TellUsAboutRentPage),
      answers.get(TellUsAboutYourRenewedAgreementPage),
      answers.get(TellUsAboutYourNewAgreementPage)
    ) match {
      case (Some(_),None, None) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(NormalMode)
      case (None, Some(_), None) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(NormalMode)
      case (None, None, Some(_)) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(NormalMode)
      case (Some(_), Some(_), Some(_)) => throw new RuntimeException("User should not have all three options")
      case (None, None, None) => throw new NotFoundException("Failed to find values")
    }
    case WhatTypeOfAgreementPage => answers =>
      answers.get(WhatTypeOfAgreementPage) match {
        case Some(value) => value match {
          case "Verbal" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementVerbalController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(NormalMode)
        }
        case None => throw new NotFoundException("Failed to find value from What type of agreement page")
      }
    case AgreementPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(NormalMode)
    case AgreementVerbalPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode)
    case WhatIsYourRentBasedOnPage => answers =>
      answers.get(WhatIsYourRentBasedOnPage) match {
        case Some(value) => value.rentBased match {
          case "PercentageTurnover" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(NormalMode)
        }
        case None => throw new NotFoundException("Not found answers")
      }
    case AgreedRentChangePage => answers =>
      answers.get(AgreedRentChangePage) match {
        case Some(value) => value match {
          case "Yes" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstSecondRentPeriodController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode)
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case HowMuchIsTotalAnnualRentPage => answers => (answers.get(TellUsAboutYourRenewedAgreementPage),
    answers.get(TellUsAboutYourNewAgreementPage)) match {
      case (Some(_),None) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(NormalMode)
      case (None,Some(_)) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
    }
    case DidYouAgreeRentWithLandlordPage => answers =>
      answers.get(DidYouAgreeRentWithLandlordPage) match {
        case Some(value)  => println(Console.MAGENTA + value + Console.RESET)
          value match {
          case "YesTheLandlord" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentInterimController.show(NormalMode)
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case ProvideDetailsOfFirstSecondRentPeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentPeriodsController.show(NormalMode)
    case RentPeriodsPage => answers =>
      answers.get(RentPeriodsPage) match {
        case Some(value) => value match {
          case "Yes" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstSecondRentPeriodController.show(NormalMode)
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
      //TODO CHANGE ROUTE TO CORRECT PAGE
    case CheckRentFreePeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
    //TODO CHECK THIS ROUTE
    case RentInterimPage => answers =>
      answers.get(RentInterimPage) match {
        case Some(value) => value match {
          case "Yes" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstSecondRentPeriodController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
        }
        //TODO ADD A TECHNICAL DIFFICULTIES PAGE
        case None => ???
      }
      //TODO Fix this route once the rebase is done
    case RentDatesAgreePage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(NormalMode)
    case WhatYourRentIncludesPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DoesYourRentIncludeParkingController.show(NormalMode)
    case RentDatesAgreeStartPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(NormalMode)
    case DoesYourRentIncludeParkingPage => answers =>
      answers.get(DoesYourRentIncludeParkingPage) match {
        case Some(value) => value match {
          case "Yes" => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case HowManyParkingSpacesOrGaragesIncludedInRentPage => _ =>  uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
    case InterimSetByTheCourtPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
  }

  //TODO change to check your answers page
  private val checkRouteMap: Page => UserAnswers => Call = {
    case _ => _ => ???
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(userAnswers)
  }
}
