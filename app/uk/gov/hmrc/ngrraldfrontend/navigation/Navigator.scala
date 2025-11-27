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

import play.api.libs.json.{JsNull, Reads, Writes}
import play.api.mvc.Call
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.models.Incentive.{YesLumpSum, YesRentFreePeriod}
import uk.gov.hmrc.ngrraldfrontend.models.{AgreementVerbal, CheckMode, Mode, NormalMode, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.*
import uk.gov.hmrc.ngrraldfrontend.queries.{Gettable, Settable}
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository
import uk.gov.hmrc.ngrraldfrontend.models.RentBasedOn

import javax.inject.{Inject, Singleton}
import scala.util.{Failure, Success, Try}

@Singleton
class Navigator @Inject()(sessionRepository: SessionRepository) {
  implicit val repo: SessionRepository = sessionRepository
  private val normalRoutes: Page => UserAnswers => Call = {
    case TellUsAboutRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(NormalMode)
    case TellUsAboutYourRenewedAgreementPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfLeaseRenewalController.show(NormalMode)
    case TellUsAboutYourNewAgreementPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(NormalMode)
    case WhatTypeOfLeaseRenewalPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.LandlordController.show(NormalMode)
    case LandlordPage => answers =>
      (answers.get(TellUsAboutRentPage), answers.get(TellUsAboutYourRenewedAgreementPage), answers.get(TellUsAboutYourNewAgreementPage)) match {
        case (Some(_), None, None) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewDetailsController.show(NormalMode)
        case (None, Some(_), None) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(NormalMode)
        case (None, None, Some(_)) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatTypeOfAgreementController.show(NormalMode)
        case (Some(_), Some(_), Some(_)) => throw new RuntimeException("User should not have all three options")
        case (None, None, None) => throw new NotFoundException("Failed to find values")
      }
    case WhatTypeOfAgreementPage => answers =>
      answers.get(WhatTypeOfAgreementPage) match {
        case Some(value) => value match {
          case "Verbal" =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = WhatTypeOfAgreementPage,
              nextPage = AgreementVerbalPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementVerbalController.show(NormalMode),
              dropList = pagesOrdered,
              answers = answers
            )
          case _ =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = WhatTypeOfAgreementPage,
              nextPage = AgreementPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(NormalMode),
              dropList = pagesOrdered,
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - WhatTypeOfAgreementPage")
      }
    case AgreementPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(NormalMode)
    case AgreementVerbalPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode)
    case RentReviewDetailsPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(NormalMode)

    case WhatIsYourRentBasedOnPage => answers =>
      answers.get(WhatIsYourRentBasedOnPage) match {
        case Some(value) => value.rentBased match {
          case "PercentageTurnover" =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(NormalMode)
              case None =>
                genericNavigationSwitchHandler(
                  mode = NormalMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPage = HowMuchIsTotalAnnualRentPage,
                  nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode),
                  dropList = pagesOrdered,
                  answers = answers
                )
            }
          case "TotalOccupancyCost" if answers.get(TellUsAboutRentPage).nonEmpty =>
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatRentIncludesRatesWaterServiceController.show(NormalMode)
          case _ =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(NormalMode)
              case None =>
                genericNavigationSwitchHandler(
                  mode = NormalMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPage = AgreedRentChangePage,
                  nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(NormalMode),
                  dropList = pagesOrdered,
                  answers = answers
                )
            }
        }
        case None => throw new NotFoundException("Failed to find answers - WhatIsYourRentBasedOnPage")
      }
    case AgreedRentChangePage => answers =>
      answers.get(AgreedRentChangePage) match {
        case Some(value) => if (value) {
          genericNavigationSwitchHandler(
            mode = NormalMode,
            currentPage = AgreedRentChangePage,
            nextPage = ProvideDetailsOfFirstRentPeriodPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstRentPeriodController.show(NormalMode),
            dropList = pagesOrdered,
            answers = answers
          )
        } else {
          genericNavigationSwitchHandler(
            mode = NormalMode,
            currentPage = AgreedRentChangePage,
            nextPage = HowMuchIsTotalAnnualRentPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode),
            dropList = pagesOrdered,
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - AgreedRentChangePage")
      }
    case HowMuchIsTotalAnnualRentPage => answers =>
      (answers.get(TellUsAboutYourRenewedAgreementPage), answers.get(TellUsAboutYourNewAgreementPage)) match {
        case (Some(_), None) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(NormalMode)
        case (None, Some(_)) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
      }
    case DidYouAgreeRentWithLandlordPage => answers =>
      answers.get(DidYouAgreeRentWithLandlordPage) match {
        case Some(value) =>
          value match {
            case true => answers.get(ProvideDetailsOfSecondRentPeriodPage) match {
              case Some(value) => value match {
                case value => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(NormalMode)
              }
              case None => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
            }
            case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentInterimController.show(NormalMode)
          }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case ProvideDetailsOfFirstRentPeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfSecondRentPeriodController.show(NormalMode)
    case ProvideDetailsOfSecondRentPeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentPeriodsController.show(NormalMode)
    case RentPeriodsPage => answers =>
      answers.get(RentPeriodsPage) match {
        case Some(value) => value match {
          case true =>
            answers.get(ProvideDetailsOfSecondRentPeriodPage) match {
              case Some(rentPeriods) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AdditionalRentPeriodController.show(NormalMode, rentPeriods.size)
              case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfSecondRentPeriodController.show(NormalMode)
            }

          case _ => answers.get(TellUsAboutYourNewAgreementPage) match {
            case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(NormalMode)
            case None => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouAgreeRentWithLandlordController.show(NormalMode)
          }
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case CheckRentFreePeriodPage => answers =>
      answers.get(CheckRentFreePeriodPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentFreePeriodController.show(NormalMode)
          case _ =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = CheckRentFreePeriodPage,
              nextPage = RentDatesAgreePage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(NormalMode),
              dropList = List(CheckRentFreePeriodPage,RentFreePeriodPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - CheckRentFreePeriodPage")
      }
    case RentInterimPage => answers =>
      answers.get(RentInterimPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.InterimRentSetByTheCourtController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
        }
        //TODO ADD A TECHNICAL DIFFICULTIES PAGE
        case None => ???
      }
    case RentDatesAgreePage => answers =>
      answers.get(WhatIsYourRentBasedOnPage) match
        case Some(value) if value.rentBased == "TotalOccupancyCost" =>
          uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatRentIncludesRatesWaterServiceController.show(NormalMode)
        case _ =>
          uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(NormalMode)
    case WhatYourRentIncludesPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DoesYourRentIncludeParkingController.show(NormalMode)
    case RentDatesAgreeStartPage => answers =>
      answers.get(WhatIsYourRentBasedOnPage) match
        case Some(value) if value.rentBased == "TotalOccupancyCost" =>
          uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatRentIncludesRatesWaterServiceController.show(NormalMode)
        case _ =>
          uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(NormalMode)
    case DoesYourRentIncludeParkingPage => answers =>
      answers.get(DoesYourRentIncludeParkingPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(NormalMode)
          case _ => genericNavigationSwitchHandler(
            mode = NormalMode,
            currentPage = DoesYourRentIncludeParkingPage,
            nextPage = RentDatesAgreePage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DoYouPayExtraForParkingSpacesController.show(NormalMode),
            dropList = List(DoesYourRentIncludeParkingPage, HowManyParkingSpacesOrGaragesIncludedInRentPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case HowManyParkingSpacesOrGaragesIncludedInRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DoYouPayExtraForParkingSpacesController.show(NormalMode)
    case InterimSetByTheCourtPage => answers =>
      answers.get(ProvideDetailsOfSecondRentPeriodPage) match {
        case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(NormalMode)
        case None => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
      }

    case RentFreePeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(NormalMode)
    case ConfirmBreakClausePage => answers =>
      answers.get(ConfirmBreakClausePage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(NormalMode)
          case _ =>           genericNavigationSwitchHandler(
            mode = NormalMode,
            currentPage = ConfirmBreakClausePage,
            nextPage = HasAnythingElseAffectedTheRentPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode),
            dropList = List(ConfirmBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClausePage, HowMuchWasTheLumpSumPage, AboutTheRentFreePeriodPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers -  ConfirmBreakClausePage")
      }

    case DidYouGetIncentiveForNotTriggeringBreakClausePage => answers =>
      answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage) match {
        case Some(value) => value match {
          case value if value.checkBox.size == 1 && value.checkBox.contains(YesRentFreePeriod) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(NormalMode)
          case value if value.checkBox.contains(YesLumpSum) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchWasTheLumpSumController.show(NormalMode)
          case value => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
        }
      }
    case DidYouGetMoneyFromLandlordPage => answers =>
      answers.get(DidYouGetMoneyFromLandlordPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.MoneyToTakeOnTheLeaseController.show(NormalMode)
          case _ =>      genericNavigationSwitchHandler(
            mode = NormalMode,
            currentPage = DidYouGetMoneyFromLandlordPage,
            nextPage = DidYouPayAnyMoneyToLandlordPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouPayAnyMoneyToLandlordController.show(NormalMode),
            dropList = List(DidYouGetMoneyFromLandlordPage, MoneyToTakeOnTheLeasePage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers -  DidYouGetMoneyFromLandlordPage")
      }

    case MoneyToTakeOnTheLeasePage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouPayAnyMoneyToLandlordController.show(NormalMode)

    case DoYouPayExtraForParkingSpacesPage => answers =>
      answers.get(DoYouPayExtraForParkingSpacesPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(NormalMode)
          case _ => val redirectLocation = answers.get(WhatIsYourRentBasedOnPage) match {
            case Some(RentBasedOn("TotalOccupancyCost", None)) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewController.show(NormalMode)
            case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(NormalMode)
          }
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = DoYouPayExtraForParkingSpacesPage,
              nextPage = RepairsAndInsurancePage,
              nextPageCall = redirectLocation,
              dropList = List(DoYouPayExtraForParkingSpacesPage, ParkingSpacesOrGaragesNotIncludedInYourRentPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case RentReviewPage => answers =>
      answers.get(TellUsAboutYourRenewedAgreementPage) match {
        case None => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndFittingOutController.show(NormalMode)
        case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(NormalMode)
      }

    case RepairsAndFittingOutPage => answers =>
      answers.get(RepairsAndFittingOutPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutRepairsAndFittingOutController.show(NormalMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(NormalMode)
        }
        case None => throw new NotFoundException("Failed to find answers - RepairsAndFittingOutPage")
      }

    case AboutRepairsAndFittingOutPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(NormalMode)

    case HowMuchWasTheLumpSumPage => answers =>
      answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage) match {
        case Some(value) => value match {
          case value if value.checkBox.size == 1 && value.checkBox.contains(YesLumpSum) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
          case value => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(NormalMode)
        }
      }

    case ParkingSpacesOrGaragesNotIncludedInYourRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(NormalMode)

    case DidYouPayAnyMoneyToLandlordPage => answers =>
      answers.get(DidYouPayAnyMoneyToLandlordPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.MoneyYouPaidInAdvanceToLandlordController.show(NormalMode)
          case _ =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = DidYouPayAnyMoneyToLandlordPage,
              nextPage = DidYouPayAnyMoneyToLandlordPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode),
              dropList = List(DidYouPayAnyMoneyToLandlordPage, MoneyYouPaidInAdvanceToLandlordPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - DidYouPayAnyMoneyToLandlordPage")
      }

    case AboutTheRentFreePeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
    case RepairsAndInsurancePage => answers =>
      answers.get(TellUsAboutRentPage) match {
        case Some(value) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ConfirmBreakClauseController.show(NormalMode)
        case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewController.show(NormalMode)
      }

    case MoneyYouPaidInAdvanceToLandlordPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
    case HasAnythingElseAffectedTheRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
    case CheckAnswersPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DeclarationController.show
    case DeclarationPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewDetailsSentController.confirmation()
  }

  val pagesOrdered: List[uk.gov.hmrc.ngrraldfrontend.queries.Settable[?]] = List(
    TellUsAboutRentPage,
    TellUsAboutYourRenewedAgreementPage,
    TellUsAboutYourNewAgreementPage,
    LandlordPage,
    WhatTypeOfAgreementPage,
    AgreementVerbalPage,
    AgreementPage,
    WhatIsYourRentBasedOnPage,
    AgreedRentChangePage,
    ProvideDetailsOfFirstRentPeriodPage,
    ProvideDetailsOfSecondRentPeriodPage,
    RentPeriodsPage,
    RentDatesAgreePage,
    HowMuchIsTotalAnnualRentPage,
    CheckRentFreePeriodPage,
    RentFreePeriodPage,
    RentDatesAgreeStartPage,
    HowManyParkingSpacesOrGaragesIncludedInRentPage,
    ParkingSpacesOrGaragesNotIncludedInYourRentPage,
    DidYouGetMoneyFromLandlordPage,
    MoneyYouPaidInAdvanceToLandlordPage
  )


  val checkRouteMap: Page => Boolean => UserAnswers => Call = {

    case ProvideDetailsOfFirstRentPeriodPage => shouldGoToSecondRentPeriod =>
      answers =>
        shouldGoToSecondRentPeriod match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
        }

    case ProvideDetailsOfSecondRentPeriodPage => shouldGoToRentPeriodsPage =>
      answers =>
        shouldGoToRentPeriodsPage match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentPeriodsController.show(CheckMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
        }

    case RentPeriodsPage => _ => answers =>
      answers.get(RentPeriodsPage).getOrElse(false) match
        case true =>
          val rentPeriodsSize = answers.get(ProvideDetailsOfSecondRentPeriodPage).map(_.size).getOrElse(0)
          if (rentPeriodsSize == 0)
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
          else
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.AdditionalRentPeriodController.show(CheckMode, rentPeriodsSize)
        case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show

    case _ => _ => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show


    case WhatTypeOfAgreementPage => _ => answers =>
      answers.get(WhatTypeOfAgreementPage) match {
        case Some(value) => value match {
          case "Verbal" =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = WhatTypeOfAgreementPage,
              nextPage = AgreementVerbalPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementVerbalController.show(NormalMode),
              dropList = pagesOrdered,
              answers = answers
            )
          case _ =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = WhatTypeOfAgreementPage,
              nextPage = AgreementPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(NormalMode),
              dropList = pagesOrdered,
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - WhatTypeOfAgreementPage")
      }


    case WhatIsYourRentBasedOnPage => _ => answers =>
      answers.get(WhatIsYourRentBasedOnPage) match {
        case Some(value) => value.rentBased match {
          case "PercentageTurnover" =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
              case None =>
                genericNavigationSwitchHandler(
                  mode = CheckMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPage = HowMuchIsTotalAnnualRentPage,
                  nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode),
                  dropList = pagesOrdered,
                  answers = answers
                )
            }
          case _ =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
              case None =>
                genericNavigationSwitchHandler(
                  mode = CheckMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPage = AgreedRentChangePage,
                  nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(NormalMode),
                  dropList = pagesOrdered,
                  answers = answers
                )
            }
        }
        case None => throw new NotFoundException("Failed to find answers - WhatIsYourRentBasedOnPage")
      }

    case AgreedRentChangePage => _ => answers =>
      answers.get(AgreedRentChangePage) match {
        case Some(value) => if (value) {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = AgreedRentChangePage,
            nextPage = ProvideDetailsOfFirstRentPeriodPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstRentPeriodController.show(NormalMode),
            dropList = pagesOrdered,
            answers = answers
          )
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = AgreedRentChangePage,
            nextPage = HowMuchIsTotalAnnualRentPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode),
            dropList = pagesOrdered,
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - AgreedRentChangePage")
      }

    case CheckRentFreePeriodPage => _ => answers =>
      answers.get(CheckRentFreePeriodPage) match {
        case Some(value) => if (value) {
          if (answers.get(RentFreePeriodPage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentFreePeriodController.show(CheckMode)
          }
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = CheckRentFreePeriodPage,
            nextPage = RentDatesAgreePage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(CheckRentFreePeriodPage,RentFreePeriodPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - CheckRentFreePeriodPage")
      }

    case DoesYourRentIncludeParkingPage => _ => answers =>
      answers.get(DoesYourRentIncludeParkingPage) match {
        case Some(value) => if (value) {
          if (answers.get(HowManyParkingSpacesOrGaragesIncludedInRentPage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(CheckMode)
          }
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = DoesYourRentIncludeParkingPage,
            nextPage = RentDatesAgreePage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(DoesYourRentIncludeParkingPage, HowManyParkingSpacesOrGaragesIncludedInRentPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - DoesYourRentIncludeParkingPage")
      }

    case DoYouPayExtraForParkingSpacesPage => _ => answers =>
      answers.get(DoYouPayExtraForParkingSpacesPage) match {
        case Some(value) => if (value) {
          if (answers.get(ParkingSpacesOrGaragesNotIncludedInYourRentPage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode)
          }
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = DoYouPayExtraForParkingSpacesPage,
            nextPage = RepairsAndInsurancePage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(DoYouPayExtraForParkingSpacesPage, ParkingSpacesOrGaragesNotIncludedInYourRentPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - DoYouPayExtraForParkingSpacesPage")
      }

    case DidYouGetMoneyFromLandlordPage => _ => answers =>
      answers.get(DidYouGetMoneyFromLandlordPage) match {
        case Some(value) => if (value) {
          if (answers.get(MoneyToTakeOnTheLeasePage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.MoneyToTakeOnTheLeaseController.show(CheckMode)
          }
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = DidYouGetMoneyFromLandlordPage,
            nextPage = DidYouPayAnyMoneyToLandlordPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(DidYouGetMoneyFromLandlordPage, MoneyToTakeOnTheLeasePage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - DidYouGetMoneyFromLandlordPage")
      }

    case DidYouPayAnyMoneyToLandlordPage => _ => answers =>
      answers.get(DidYouPayAnyMoneyToLandlordPage) match {
        case Some(value) => if (value) {
          if (answers.get(MoneyYouPaidInAdvanceToLandlordPage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.MoneyYouPaidInAdvanceToLandlordController.show(CheckMode)
          }
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = DidYouPayAnyMoneyToLandlordPage,
            nextPage = DidYouPayAnyMoneyToLandlordPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(DidYouPayAnyMoneyToLandlordPage, MoneyYouPaidInAdvanceToLandlordPage),
            answers = answers
          )
          val updatedAnswers = answers.remove(MoneyYouPaidInAdvanceToLandlordPage).get
          sessionRepository.set(updatedAnswers)
          uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
        }
        case None => throw new NotFoundException("Failed to find answers - DidYouPayAnyMoneyToLandlordPage")
      }

    case DidYouAgreeRentWithLandlordPage => _ => answers =>
      answers.get(DidYouAgreeRentWithLandlordPage) match {
        case Some(value) => if (value) {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = DidYouAgreeRentWithLandlordPage,
            nextPage = DidYouPayAnyMoneyToLandlordPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList =  List(DidYouAgreeRentWithLandlordPage, RentInterimPage, InterimSetByTheCourtPage),
            answers = answers
          )
        } else {
          if (answers.get(RentInterimPage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentInterimController.show(NormalMode)
          }
        }
        case None => throw new NotFoundException("Failed to find answers - DidYouAgreeRentWithLandlordPage")
      }

    case ConfirmBreakClausePage => _ => answers =>
      answers.get(ConfirmBreakClausePage) match {
        case Some(value) => if (value) {
          if (answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(NormalMode)
          }
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = ConfirmBreakClausePage,
            nextPage = HasAnythingElseAffectedTheRentPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(ConfirmBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClausePage, HowMuchWasTheLumpSumPage, AboutTheRentFreePeriodPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - ConfirmBreakClausePage")
      }

    case DidYouGetIncentiveForNotTriggeringBreakClausePage => _ => answers =>
      def dropBreakClauseJourney = trimUserAnswersFromPage(DidYouGetIncentiveForNotTriggeringBreakClausePage, List(DidYouGetIncentiveForNotTriggeringBreakClausePage, HowMuchWasTheLumpSumPage, AboutTheRentFreePeriodPage), answers)
      answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage) match {
        case Some(value) => value match {
          case value if value.checkBox.size == 1 && value.checkBox.contains(YesRentFreePeriod) && answers.get(AboutTheRentFreePeriodPage).isEmpty =>
            dropBreakClauseJourney
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(NormalMode)
          case value if value.checkBox.contains(YesLumpSum) && answers.get(HowMuchWasTheLumpSumPage).isEmpty =>
            dropBreakClauseJourney
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchWasTheLumpSumController.show(NormalMode)
          case value if answers.get(HasAnythingElseAffectedTheRentPage).isEmpty =>
            dropBreakClauseJourney
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
          case value => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
        }
        case None => throw new NotFoundException("Failed to find answers - ConfirmBreakClausePage")
      }

    case _ => _ => _ =>
      uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
  }

  private def genericNavigationSwitchHandler[A](
                                                 mode: Mode,
                                                 currentPage: Settable[_],
                                                 nextPage: Gettable[A],
                                                 nextPageCall: Call,
                                                 dropList: List[Settable[_]],
                                                 answers: UserAnswers)(implicit sessionRepository: SessionRepository, reads: Reads[A]) =  {
    answers.get(nextPage) match {
      case None =>
        val updatedAnswers = trimUserAnswersFromPage(currentPage, dropList, answers).getOrElse(answers)
        sessionRepository.set(updatedAnswers)
        nextPageCall
      case Some(_) =>
        mode match {
          case CheckMode => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          case NormalMode => nextPageCall
        }
    }
  }

  private def trimUserAnswersFromPage(
                                       startPage: Settable[_],
                                       pages: List[Settable[_]],
                                       userAnswers: UserAnswers
                                     ): Try[UserAnswers] = {
    implicit val anyWrites: Writes[Any] = Writes(_ => JsNull)

    val indexOpt = pages.indexWhere(_ == startPage) match {
      case -1 => None
      case idx => Some(idx)
    }

    indexOpt match {
      case None => Success(userAnswers)
      case Some(idx) =>
        val pagesToRemove = pages.drop(idx + 1)
        var currentAnswers = userAnswers

        pagesToRemove.foreach { page =>
          currentAnswers = currentAnswers.remove(page.asInstanceOf[Settable[Any]]) match {
            case Success(updated) =>
              updated
            case Failure(_) => currentAnswers
          }
        }
        Success(currentAnswers)
    }
  }

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, shouldGoToRentPeriodsPage: Boolean = false): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(shouldGoToRentPeriodsPage)(userAnswers)
  }
}
