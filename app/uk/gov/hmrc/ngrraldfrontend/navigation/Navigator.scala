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

  val typeOfAgreementJourney: List[uk.gov.hmrc.ngrraldfrontend.queries.Settable[?]] = List(
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
    RentDatesAgreeStartPage
  )
  val rentBasedOnTOCJourney: List[uk.gov.hmrc.ngrraldfrontend.queries.Settable[?]] = List(
    WhatIsYourRentBasedOnPage,
    RepairsAndInsurancePage,
    HowMuchIsTotalAnnualRentPage,
    CheckRentFreePeriodPage,
    RentFreePeriodPage,
    RentDatesAgreeStartPage
  )


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
              nextPageToBeClean = AgreementPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementVerbalController.show(NormalMode),
              dropList = typeOfAgreementJourney,
              answers = answers
            )
          case _ =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = WhatTypeOfAgreementPage,
              nextPageToBeClean = AgreementVerbalPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(NormalMode),
              dropList = typeOfAgreementJourney,
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
                  nextPageToBeClean = AgreedRentChangePage,
                  nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode),
                  dropList = typeOfAgreementJourney,
                  answers = answers
                )
            }
          case "TotalOccupancyCost" =>
            val direction = if (answers.get(TellUsAboutRentPage).nonEmpty)
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatRentIncludesRatesWaterServiceController.show(NormalMode)
            else
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(NormalMode)

            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = WhatIsYourRentBasedOnPage,
              nextPageToBeClean = RepairsAndInsurancePage,
              nextPageCall = direction,
              dropList = rentBasedOnTOCJourney,
              answers = answers
            )
          case _ =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(NormalMode)
              case None =>
                genericNavigationSwitchHandler(
                  mode = NormalMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPageToBeClean = HowMuchIsTotalAnnualRentPage,
                  nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(NormalMode),
                  dropList = typeOfAgreementJourney,
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
            nextPageToBeClean = HowMuchIsTotalAnnualRentPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstRentPeriodController.show(NormalMode),
            dropList = typeOfAgreementJourney,
            answers = answers
          )
        } else {
          genericNavigationSwitchHandler(
            mode = NormalMode,
            currentPage = AgreedRentChangePage,
            nextPageToBeClean = ProvideDetailsOfFirstRentPeriodPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(NormalMode),
            dropList = typeOfAgreementJourney,
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
            case true =>
              val direction = if (answers.get(ProvideDetailsOfSecondRentPeriodPage).nonEmpty)
                uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(NormalMode)
              else
                uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
              genericNavigationSwitchHandler(
                mode = NormalMode,
                currentPage = DidYouAgreeRentWithLandlordPage,
                nextPageToBeClean = RentInterimPage,
                nextPageCall = direction,
                dropList = List(DidYouAgreeRentWithLandlordPage, RentInterimPage, InterimSetByTheCourtPage),
                answers = answers
              )
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
              nextPageToBeClean = RentFreePeriodPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(NormalMode),
              dropList = List(CheckRentFreePeriodPage, RentFreePeriodPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - CheckRentFreePeriodPage")
      }
    case RentInterimPage => answers =>
      answers.get(RentInterimPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.InterimRentSetByTheCourtController.show(NormalMode)
          case _ =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = RentInterimPage,
              nextPageToBeClean = InterimSetByTheCourtPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode),
              dropList = List(RentInterimPage, InterimSetByTheCourtPage),
              answers = answers
            )
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
            nextPageToBeClean = HowManyParkingSpacesOrGaragesIncludedInRentPage,
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
        case None    => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(NormalMode)
      }

    case RentFreePeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(NormalMode)
    case ConfirmBreakClausePage => answers =>
      answers.get(ConfirmBreakClausePage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(NormalMode)
          case _ => genericNavigationSwitchHandler(
            mode = NormalMode,
            currentPage = ConfirmBreakClausePage,
            nextPageToBeClean = DidYouGetIncentiveForNotTriggeringBreakClausePage,
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
          case value if value.checkBox.size == 1 && value.checkBox.contains(YesRentFreePeriod) =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = DidYouGetIncentiveForNotTriggeringBreakClausePage,
              nextPageToBeClean = HowMuchWasTheLumpSumPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(NormalMode),
              dropList = List(DidYouGetIncentiveForNotTriggeringBreakClausePage, HowMuchWasTheLumpSumPage),
              answers = answers
            )
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
            nextPageToBeClean = MoneyToTakeOnTheLeasePage,
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
          case _    =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = DoYouPayExtraForParkingSpacesPage,
              nextPageToBeClean = ParkingSpacesOrGaragesNotIncludedInYourRentPage,
              nextPageCall = skipRepairsAndInsuranceIfRentBasedOnIsTOC(answers),
              dropList = List(DoYouPayExtraForParkingSpacesPage, ParkingSpacesOrGaragesNotIncludedInYourRentPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers")
      }
    case RentReviewPage => answers =>
      answers.get(TellUsAboutYourRenewedAgreementPage) match {
        case None => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndFittingOutController.show(NormalMode)
        case _    => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(NormalMode)
      }

    case RepairsAndFittingOutPage => answers =>
      answers.get(RepairsAndFittingOutPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutRepairsAndFittingOutController.show(NormalMode)
          case _    =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = RepairsAndFittingOutPage,
              nextPageToBeClean = AboutRepairsAndFittingOutPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(NormalMode),
              dropList = List(RepairsAndFittingOutPage, AboutRepairsAndFittingOutPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - RepairsAndFittingOutPage")
      }

    case AboutRepairsAndFittingOutPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetMoneyFromLandlordController.show(NormalMode)

    case HowMuchWasTheLumpSumPage => answers =>
      answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage) match {
        case Some(value) => value match {
          case value if value.checkBox.size == 1 && value.checkBox.contains(YesLumpSum) =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = HowMuchWasTheLumpSumPage,
              nextPageToBeClean = AboutTheRentFreePeriodPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode),
              dropList = List(HowMuchWasTheLumpSumPage, AboutTheRentFreePeriodPage),
              answers = answers
            )
          case value => uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(NormalMode)
        }
      }

    case ParkingSpacesOrGaragesNotIncludedInYourRentPage => answers => skipRepairsAndInsuranceIfRentBasedOnIsTOC(answers)
    case DidYouPayAnyMoneyToLandlordPage => answers =>
      answers.get(DidYouPayAnyMoneyToLandlordPage) match {
        case Some(value) => value match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.MoneyYouPaidInAdvanceToLandlordController.show(NormalMode)
          case _ =>
            genericNavigationSwitchHandler(
              mode = NormalMode,
              currentPage = DidYouPayAnyMoneyToLandlordPage,
              nextPageToBeClean = MoneyYouPaidInAdvanceToLandlordPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode),
              dropList = List(DidYouPayAnyMoneyToLandlordPage, MoneyYouPaidInAdvanceToLandlordPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - DidYouPayAnyMoneyToLandlordPage")
      }
    case AboutTheRentFreePeriodPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
    case RepairsAndInsurancePage => answers => nextPageForRepairsAndInsurance(answers)

    case MoneyYouPaidInAdvanceToLandlordPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
    case HasAnythingElseAffectedTheRentPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
    case CheckAnswersPage => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.DeclarationController.show
    case DeclarationPage => answers => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewDetailsSentController.confirmation()
  }

  val checkRouteMap: Page => Boolean => UserAnswers => Call = {
    case ProvideDetailsOfFirstRentPeriodPage => shouldGoToSecondRentPeriod => answers =>
        shouldGoToSecondRentPeriod match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
          case _    => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
        }

    case ProvideDetailsOfSecondRentPeriodPage => shouldGoToRentPeriodsPage => answers =>
        shouldGoToRentPeriodsPage match {
          case true => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentPeriodsController.show(CheckMode)
          case _    => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
        }

    case RentPeriodsPage => _ => answers =>
      answers.get(RentPeriodsPage).getOrElse(false) match
        case true =>
          val rentPeriodsSize = answers.get(ProvideDetailsOfSecondRentPeriodPage).map(_.size).getOrElse(0)
          if (rentPeriodsSize == 0)
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
          else
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.AdditionalRentPeriodController.show(CheckMode, rentPeriodsSize)
        case _ =>
          if (answers.get(TellUsAboutYourNewAgreementPage).nonEmpty && answers.get(RentDatesAgreePage).isEmpty)
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(CheckMode)
          else
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show

    case WhatTypeOfAgreementPage => _ => answers =>
      answers.get(WhatTypeOfAgreementPage) match {
        case Some(value) => value match {
          case "Verbal" =>
            genericNavigationSwitchHandler(
              mode = CheckMode,
              currentPage = WhatTypeOfAgreementPage,
              nextPageToBeClean = AgreementPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementVerbalController.show(CheckMode),
              dropList = typeOfAgreementJourney,
              answers = answers,
              returnCYA = answers.get(AgreementVerbalPage).nonEmpty
            )
          case _ =>
            genericNavigationSwitchHandler(
              mode = CheckMode,
              currentPage = WhatTypeOfAgreementPage,
              nextPageToBeClean = AgreementVerbalPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreementController.show(CheckMode),
              dropList = typeOfAgreementJourney,
              answers = answers,
              returnCYA = answers.get(AgreementPage).nonEmpty
            )
        }
        case None => throw new NotFoundException("Failed to find answers - WhatTypeOfAgreementPage")
      }

    case AgreementVerbalPage => _ => answers =>
      checkModeNextPage(HowMuchIsTotalAnnualRentPage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(CheckMode), answers)
    case RentFreePeriodPage => _ => answers =>
      if (answers.get(TellUsAboutYourNewAgreementPage).nonEmpty)
        checkModeNextPage(RentDatesAgreeStartPage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(CheckMode), answers)
      else
        uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
    case AgreementPage => _ => answers =>
      checkModeNextPage(WhatIsYourRentBasedOnPage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(CheckMode), answers)

    case WhatIsYourRentBasedOnPage => _ => answers =>
      answers.get(WhatIsYourRentBasedOnPage) match {
        case Some(value) => value.rentBased match {
          case "PercentageTurnover" =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) =>
                if (answers.get(RepairsAndInsurancePage).isEmpty)
                  nextPageForRentBasedOnRentReviewCheckMode(answers, false)
                else
                  uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
              case None =>
                val direction = if (answers.get(AgreedRentChangePage).nonEmpty)
                  uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(CheckMode)
                else
                  nextPageForRentBaseOnNonTOCCheckModeOnly(HowMuchIsTotalAnnualRentPage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(CheckMode), answers)
                genericNavigationSwitchHandler(
                  mode = CheckMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPageToBeClean = AgreedRentChangePage,
                  nextPageCall = direction,
                  dropList = typeOfAgreementJourney,
                  answers = answers,
                  returnCYA = answers.get(HowMuchIsTotalAnnualRentPage).nonEmpty
                )
            }
          case "TotalOccupancyCost" =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) =>
                if (answers.get(RepairsAndInsurancePage).nonEmpty)
                  nextPageForRentBasedOnRentReviewCheckMode(answers, true)
                else
                  uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
              case _ =>
                val direction = if (answers.get(AgreedRentChangePage).isEmpty)
                    uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(CheckMode)
                else
                  uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show

                genericNavigationSwitchHandler(
                  mode = CheckMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPageToBeClean = RepairsAndInsurancePage,
                  nextPageCall = direction,
                  dropList = rentBasedOnTOCJourney,
                  answers = answers
                )
            }

          case _ =>
            answers.get(TellUsAboutRentPage) match {
              case Some(_) =>
                if (answers.get(RepairsAndInsurancePage).isEmpty)
                  nextPageForRentBasedOnRentReviewCheckMode(answers, false)
                else
                  uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
              case None =>
                genericNavigationSwitchHandler(
                  mode = CheckMode,
                  currentPage = WhatIsYourRentBasedOnPage,
                  nextPageToBeClean = HowMuchIsTotalAnnualRentPage,
                  nextPageCall = nextPageForRentBaseOnNonTOCCheckModeOnly(AgreedRentChangePage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.AgreedRentChangeController.show(CheckMode), answers),
                  dropList = typeOfAgreementJourney,
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
            nextPageToBeClean = HowMuchIsTotalAnnualRentPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.ProvideDetailsOfFirstRentPeriodController.show(CheckMode),
            dropList = typeOfAgreementJourney,
            answers = answers,
            returnCYA = answers.get(ProvideDetailsOfFirstRentPeriodPage).nonEmpty
          )
        } else {
            genericNavigationSwitchHandler(
              mode = CheckMode,
              currentPage = AgreedRentChangePage,
              nextPageToBeClean = ProvideDetailsOfFirstRentPeriodPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchIsTotalAnnualRentController.show(CheckMode),
              dropList = typeOfAgreementJourney,
              answers = answers,
              returnCYA = answers.get(HowMuchIsTotalAnnualRentPage).nonEmpty
            )
        }
        case None => throw new NotFoundException("Failed to find answers - AgreedRentChangePage")
      }

    case HowMuchIsTotalAnnualRentPage => _ => answers =>
      answers.get(TellUsAboutYourRenewedAgreementPage) match
        case Some(_) if answers.get(WhatIsYourRentBasedOnPage).exists(_.rentBased != "TotalOccupancyCost") =>
          checkModeNextPage(RepairsAndInsurancePage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode), answers)
        case _ => checkModeNextPage(CheckRentFreePeriodPage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(CheckMode), answers)

    case RentDatesAgreePage => _ => answers =>
      answers.get(TellUsAboutYourNewAgreementPage) match
        case Some(_) if answers.get(WhatIsYourRentBasedOnPage).exists(_.rentBased != "TotalOccupancyCost") =>
          checkModeNextPage(RepairsAndInsurancePage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode), answers)
        case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show

    case RentDatesAgreeStartPage => _ => answers =>
      answers.get(TellUsAboutYourNewAgreementPage) match
        case Some(_) if answers.get(WhatIsYourRentBasedOnPage).exists(_.rentBased != "TotalOccupancyCost") =>
          checkModeNextPage(RepairsAndInsurancePage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode), answers)
        case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show

    case WhatYourRentIncludesPage => _ => answers =>
      answers.get(TellUsAboutRentPage) match
        case Some(_) if answers.get(WhatIsYourRentBasedOnPage).exists(_.rentBased != "TotalOccupancyCost") =>
          checkModeNextPage(RepairsAndInsurancePage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode), answers)
        case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show

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
            nextPageToBeClean = RentFreePeriodPage,
            nextPageCall = checkModeNextPage(RentDatesAgreeStartPage, uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeStartController.show(CheckMode), answers),
            dropList = List(CheckRentFreePeriodPage, RentFreePeriodPage),
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
            nextPageToBeClean = HowManyParkingSpacesOrGaragesIncludedInRentPage,
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
            nextPageToBeClean = ParkingSpacesOrGaragesNotIncludedInYourRentPage,
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
            nextPageToBeClean = MoneyToTakeOnTheLeasePage,
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
            nextPageToBeClean = MoneyYouPaidInAdvanceToLandlordPage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(DidYouPayAnyMoneyToLandlordPage, MoneyYouPaidInAdvanceToLandlordPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - DidYouPayAnyMoneyToLandlordPage")
      }

    case DidYouAgreeRentWithLandlordPage => _ => answers =>
      answers.get(DidYouAgreeRentWithLandlordPage) match {
        case Some(value) => if (value) {
          val direction = if (answers.get(ProvideDetailsOfSecondRentPeriodPage).nonEmpty) {
            if (answers.get(RentDatesAgreePage).nonEmpty)
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
            else
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentDatesAgreeController.show(CheckMode)
          } else {
            if (answers.get(CheckRentFreePeriodPage).nonEmpty)
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
            else
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckRentFreePeriodController.show(CheckMode)
          }
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = DidYouAgreeRentWithLandlordPage,
            nextPageToBeClean = RentInterimPage,
            nextPageCall = direction,
            dropList =  List(DidYouAgreeRentWithLandlordPage, RentInterimPage, InterimSetByTheCourtPage),
            answers = answers
          )
        } else {
          if (answers.get(RentInterimPage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentInterimController.show(CheckMode)
          }
        }
        case None => throw new NotFoundException("Failed to find answers - DidYouAgreeRentWithLandlordPage")
      }

    case RentInterimPage => _ => answers =>
      answers.get(RentInterimPage) match {
        case Some(value) => value match {
          case true =>
            if (answers.get(InterimSetByTheCourtPage).nonEmpty)
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
            else
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.InterimRentSetByTheCourtController.show(CheckMode)
          case _ =>
            genericNavigationSwitchHandler(
              mode = CheckMode,
              currentPage = RentInterimPage,
              nextPageToBeClean = InterimSetByTheCourtPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
              dropList = List(RentInterimPage, InterimSetByTheCourtPage),
              answers = answers
            )
        }
        //TODO ADD A TECHNICAL DIFFICULTIES PAGE
        case None => ???
      }

    case ConfirmBreakClausePage => _ => answers =>
      answers.get(ConfirmBreakClausePage) match {
        case Some(value) => if (value) {
          if (answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage).nonEmpty) {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
          } else {
            uk.gov.hmrc.ngrraldfrontend.controllers.routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(CheckMode)
          }
        } else {
          genericNavigationSwitchHandler(
            mode = CheckMode,
            currentPage = ConfirmBreakClausePage,
            nextPageToBeClean = DidYouGetIncentiveForNotTriggeringBreakClausePage,
            nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
            dropList = List(ConfirmBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClausePage, HowMuchWasTheLumpSumPage, AboutTheRentFreePeriodPage),
            answers = answers
          )
        }
        case None => throw new NotFoundException("Failed to find answers - ConfirmBreakClausePage")
      }

    case DidYouGetIncentiveForNotTriggeringBreakClausePage => _ => answers =>
      answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage) match {
        case Some(value) => value match {
          case value if value.checkBox.size == 1 && value.checkBox.contains(YesRentFreePeriod) =>
            val direction = if (answers.get(AboutTheRentFreePeriodPage).nonEmpty)
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
            else
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(CheckMode)
            genericNavigationSwitchHandler(
              mode = CheckMode,
              currentPage = DidYouGetIncentiveForNotTriggeringBreakClausePage,
              nextPageToBeClean = HowMuchWasTheLumpSumPage,
              nextPageCall = direction,
              dropList = List(DidYouGetIncentiveForNotTriggeringBreakClausePage, HowMuchWasTheLumpSumPage),
              answers = answers
            )
          case value if value.checkBox.contains(YesLumpSum) =>
            answers.get(HowMuchWasTheLumpSumPage) match
              case Some(_) =>
                if (value.checkBox.size == 1)
                  genericNavigationSwitchHandler(
                    mode = CheckMode,
                    currentPage = DidYouGetIncentiveForNotTriggeringBreakClausePage,
                    nextPageToBeClean = AboutTheRentFreePeriodPage,
                    nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
                    dropList = List(DidYouGetIncentiveForNotTriggeringBreakClausePage, AboutTheRentFreePeriodPage),
                    answers = answers
                  )
                else
                  if (answers.get(AboutTheRentFreePeriodPage).nonEmpty)
                    uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
                  else
                    uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(CheckMode)
              case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.HowMuchWasTheLumpSumController.show(CheckMode)
          case value =>
            genericNavigationSwitchHandler(
              mode = CheckMode,
              currentPage = DidYouGetIncentiveForNotTriggeringBreakClausePage,
              nextPageToBeClean = HowMuchWasTheLumpSumPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
              dropList = List(DidYouGetIncentiveForNotTriggeringBreakClausePage, HowMuchWasTheLumpSumPage, AboutTheRentFreePeriodPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - ConfirmBreakClausePage")
      }

    case HowMuchWasTheLumpSumPage => _ => answers =>
      answers.get(DidYouGetIncentiveForNotTriggeringBreakClausePage) match {
        case Some(value) => value match {
          case value if value.checkBox.contains(YesRentFreePeriod) =>
            if (answers.get(AboutTheRentFreePeriodPage).nonEmpty)
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
            else
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutTheRentFreePeriodController.show(CheckMode)
          case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
        }
        case None => throw new NotFoundException("Failed to find answers - HowMuchWasTheLumpSumPage")
      }

    case RepairsAndFittingOutPage => _ => answers =>
      answers.get(RepairsAndFittingOutPage) match {
        case Some(value) => value match {
          case true =>
            if (answers.get(AboutRepairsAndFittingOutPage).nonEmpty)
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
            else
              uk.gov.hmrc.ngrraldfrontend.controllers.routes.AboutRepairsAndFittingOutController.show(CheckMode)
          case _    =>
            genericNavigationSwitchHandler(
              mode = CheckMode,
              currentPage = RepairsAndFittingOutPage,
              nextPageToBeClean = AboutRepairsAndFittingOutPage,
              nextPageCall = uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show,
              dropList = List(RepairsAndFittingOutPage, AboutRepairsAndFittingOutPage),
              answers = answers
            )
        }
        case None => throw new NotFoundException("Failed to find answers - RepairsAndFittingOutPage")
      }

    case _ => _ => _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
  }

  private def genericNavigationSwitchHandler[A](
                                                 mode: Mode,
                                                 currentPage: Settable[_],
                                                 nextPageToBeClean: Gettable[A],
                                                 nextPageCall: Call,
                                                 dropList: List[Settable[_]],
                                                 answers: UserAnswers,
                                                 returnCYA: Boolean = false
                                               )(implicit sessionRepository: SessionRepository, reads: Reads[A]) =  {
    answers.get(nextPageToBeClean) match {
      case Some(value) =>
        println(Console.MAGENTA_B + value + Console.RESET)
        val updatedAnswers = trimUserAnswersFromPage(currentPage, dropList, answers).getOrElse(answers)
        sessionRepository.set(updatedAnswers)
        nextPageCall
      case None =>
        println(Console.MAGENTA_B + nextPageToBeClean + Console.RESET)
        mode match {
          case CheckMode => if (returnCYA) uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show else nextPageCall
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

  private def checkModeNextPage[A](nextPage: Gettable[A], nextPageCall: Call, answers: UserAnswers)(implicit reads: Reads[A]) =
    answers.get(nextPage) match
      case Some(_) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show
      case _ => nextPageCall

  def nextPage(page: Page, mode: Mode, userAnswers: UserAnswers, shouldGoToRentPeriodsPage: Boolean = false): Call = mode match {
    case NormalMode =>
      normalRoutes(page)(userAnswers)
    case CheckMode =>
      checkRouteMap(page)(shouldGoToRentPeriodsPage)(userAnswers)
  }

  private def nextPageForRentBasedOnRentReviewCheckMode(userAnswers: UserAnswers, isTOC: Boolean) = {
    val pagesToBeRemoved = List(WhatIsYourRentBasedOnPage, WhatYourRentIncludesPage)
    val direction = if (isTOC) uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatRentIncludesRatesWaterServiceController.show(CheckMode) else
      uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatYourRentIncludesController.show(CheckMode)
    genericNavigationSwitchHandler(
      mode = CheckMode,
      currentPage = WhatIsYourRentBasedOnPage,
      nextPageToBeClean = WhatYourRentIncludesPage,
      nextPageCall = direction,
      dropList = if (isTOC) pagesToBeRemoved :+ RepairsAndInsurancePage else pagesToBeRemoved,
      answers = userAnswers
    )
  }

  private def nextPageForRepairsAndInsurance(userAnswers: UserAnswers): Call = {
    userAnswers.get(TellUsAboutRentPage) match {
      case Some(value) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.ConfirmBreakClauseController.show(NormalMode)
      case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RentReviewController.show(NormalMode)
    }
  }

  private def nextPageForRentBaseOnNonTOCCheckModeOnly[A](nextPage: Gettable[A], nextPageCall: Call, userAnswers: UserAnswers)(implicit reads: Reads[A]): Call =
    (userAnswers.get(nextPage).isEmpty, userAnswers.get(RepairsAndInsurancePage).isEmpty) match
      case (true, _) => nextPageCall
      case (false, true) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(CheckMode)
      case (false, false) => uk.gov.hmrc.ngrraldfrontend.controllers.routes.CheckAnswersController.show

  private[navigation] def skipRepairsAndInsuranceIfRentBasedOnIsTOC(userAnswers: UserAnswers): Call = {
    userAnswers.get(WhatIsYourRentBasedOnPage).map(_.rentBased).getOrElse("") match {
      case "" if userAnswers.get(WhatTypeOfAgreementPage).exists(_ != "Verbal") =>
        uk.gov.hmrc.ngrraldfrontend.controllers.routes.WhatIsYourRentBasedOnController.show(NormalMode)
      case "TotalOccupancyCost" => nextPageForRepairsAndInsurance(userAnswers)
      case _ => uk.gov.hmrc.ngrraldfrontend.controllers.routes.RepairsAndInsuranceController.show(NormalMode)
    }
  }
}
