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

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatest.TryValues.convertTryToSuccessOrFailure
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Call
import uk.gov.hmrc.ngrraldfrontend.config.AppConfig
import uk.gov.hmrc.ngrraldfrontend.controllers.routes
import uk.gov.hmrc.ngrraldfrontend.helpers.TestData
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.{NewAgreement, RenewedAgreement, RentAgreement}
import uk.gov.hmrc.ngrraldfrontend.models.Incentive.*
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.pages.*
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository


class NavigatorSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with TestData {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val mockConfig: AppConfig = mock[AppConfig]
  val navigator = new Navigator(mockSessionRepository, mockConfig)
  val userAnswersWithoutData = UserAnswers(CredId("1234"))

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository, mockConfig)
  }

  "normalRoute for WhatTypeOfAgreementPage" should {
    "return AgreementVerbalController when AgreementVerbal is select and AgreementPage is removed" in {
      val answers = userAnswersWithoutData
        .set(WhatTypeOfAgreementPage, "Verbal")
        .flatMap(_.set(AgreementPage, agreementModel))
        .success.value

      val result = navigator.nextPage(WhatTypeOfAgreementPage, NormalMode, answers)

      result shouldBe routes.AgreementVerbalController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return AgreementController when LeaseOrTenancy is select and AgreementVerbalPage is removed" in {
      val answers = userAnswersWithoutData
        .set(WhatTypeOfAgreementPage, "LeaseOrTenancy")
        .flatMap(_.set(AgreementVerbalPage, agreementVerbalModel))
        .success.value

      val result = navigator.nextPage(WhatTypeOfAgreementPage, NormalMode, answers)

      result shouldBe routes.AgreementController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }

  "normalRoute for WhatIsYourRentBasedOnPage" should {
    "return HowMuchIsTotalAnnualRentController when PercentageTurnover is select and AgreedRentChangePage is removed" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnPercentageTurnover)
        .flatMap(_.set(AgreedRentChangePage, true))
        .success.value

      val result = navigator.nextPage(WhatIsYourRentBasedOnPage, NormalMode, answers)

      result shouldBe routes.HowMuchIsTotalAnnualRentController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return AgreedRentChangeController when TotalOccupancyCost is select and WhatYourRentIncludesPage is removed" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnTOC)
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllYes))
        .success.value

      val result = navigator.nextPage(WhatIsYourRentBasedOnPage, NormalMode, answers)

      result shouldBe routes.AgreedRentChangeController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return WhatRentIncludesRatesWaterServiceController when TotalOccupancyCost is select, TellUsAboutRentPage is provided and WhatYourRentIncludesPage is removed" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnTOC)
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .flatMap(_.set(RepairsAndInsurancePage, repairsAndInsuranceModel))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllYes))
        .success.value

      val result = navigator.nextPage(WhatIsYourRentBasedOnPage, NormalMode, answers)

      result shouldBe routes.WhatRentIncludesRatesWaterServiceController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return AgreedRentChangeController when Other is select and HowMuchIsTotalAnnualRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.nextPage(WhatIsYourRentBasedOnPage, NormalMode, answers)

      result shouldBe routes.AgreedRentChangeController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }

  "normalRoute for AgreedRentChangePage" should {
    "return ProvideDetailsOfFirstRentPeriodController when yes is select and HowMuchIsTotalAnnualRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, true)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.nextPage(AgreedRentChangePage, NormalMode, answers)

      result shouldBe routes.ProvideDetailsOfFirstRentPeriodController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return HowMuchIsTotalAnnualRentController when no is select and ProvideDetailsOfFirstRentPeriodPage is removed" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, false)
        .flatMap(_.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod))
        .success.value

      val result = navigator.nextPage(AgreedRentChangePage, NormalMode, answers)

      result shouldBe routes.HowMuchIsTotalAnnualRentController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for DidYouAgreeRentWithLandlordPage" should {
    "return RentDatesAgreeController when yes is select and RentInterimPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, true)
        .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod))
        .flatMap(_.set(RentInterimPage, true))
        .success.value

      val result = navigator.nextPage(DidYouAgreeRentWithLandlordPage, NormalMode, answers)

      result shouldBe routes.RentDatesAgreeController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return CheckRentFreePeriodController when yes is select and RentInterimPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, true)
        .flatMap(_.set(RentInterimPage, true))
        .success.value

      val result = navigator.nextPage(DidYouAgreeRentWithLandlordPage, NormalMode, answers)

      result shouldBe routes.CheckRentFreePeriodController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for CheckRentFreePeriodPage" should {
    "return RentDatesAgreeStartController when no is select and RentFreePeriodPage is removed" in {
      val answers = userAnswersWithoutData
        .set(CheckRentFreePeriodPage, false)
        .flatMap(_.set(RentFreePeriodPage, rentFreePeriodModel))
        .success.value

      val result = navigator.nextPage(CheckRentFreePeriodPage, NormalMode, answers)

      result shouldBe routes.RentDatesAgreeStartController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for RentInterimPage" should {
    "return CheckRentFreePeriodController when no is select and InterimSetByTheCourtPage is removed" in {
      val answers = userAnswersWithoutData
        .set(RentInterimPage, false)
        .flatMap(_.set(InterimSetByTheCourtPage, interimRentSetByTheCourtModel))
        .success.value

      val result = navigator.nextPage(RentInterimPage, NormalMode, answers)

      result shouldBe routes.CheckRentFreePeriodController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for DoesYourRentIncludeParkingPage" should {
    "return DoYouPayExtraForParkingSpacesController when no is select and HowManyParkingSpacesOrGaragesIncludedInRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DoesYourRentIncludeParkingPage, false)
        .flatMap(_.set(HowManyParkingSpacesOrGaragesIncludedInRentPage, parkingSpacesIncluded))
        .success.value

      val result = navigator.nextPage(DoesYourRentIncludeParkingPage, NormalMode, answers)

      result shouldBe routes.DoYouPayExtraForParkingSpacesController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for RentFreePeriodPage" should {
    "return HasAnythingElseAffectedTheRentController when no is select and DidYouGetIncentiveForNotTriggeringBreakClausePage is removed" in {
      val answers = userAnswersWithoutData
        .set(ConfirmBreakClausePage, false)
        .flatMap(_.set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesLumpSum, YesRentFreePeriod))))
        .success.value

      val result = navigator.nextPage(ConfirmBreakClausePage, NormalMode, answers)

      result shouldBe routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for DidYouGetIncentiveForNotTriggeringBreakClausePage" should {
    "return AboutTheRentFreePeriodController when only YesRentFreePeriod is select and HowMuchWasTheLumpSumPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesRentFreePeriod)))
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(1000)))
        .success.value

      val result = navigator.nextPage(DidYouGetIncentiveForNotTriggeringBreakClausePage, NormalMode, answers)

      result shouldBe routes.AboutTheRentFreePeriodController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for DidYouGetMoneyFromLandlordPage" should {
    "return DidYouPayAnyMoneyToLandlordController when no is select and MoneyToTakeOnTheLeasePage is removed" in {
      val answers = userAnswersWithoutData
        .set(DidYouGetMoneyFromLandlordPage, false)
        .flatMap(_.set(MoneyToTakeOnTheLeasePage, MoneyToTakeOnTheLease(10000, "2000-01-01")))
        .success.value

      val result = navigator.nextPage(DidYouGetMoneyFromLandlordPage, NormalMode, answers)

      result shouldBe routes.DidYouPayAnyMoneyToLandlordController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for DoYouPayExtraForParkingSpacesPage" should {
    "return RentReviewController when no is select, rentBasedOn is TOC and ParkingSpacesOrGaragesNotIncludedInYourRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, false)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.nextPage(DoYouPayExtraForParkingSpacesPage, NormalMode, answers)

      result shouldBe routes.RentReviewController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return ConfirmBreakClauseController when no is select, TellUsAboutRentPage is provided, rentBasedOn is TOC and ParkingSpacesOrGaragesNotIncludedInYourRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, false)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.nextPage(DoYouPayExtraForParkingSpacesPage, NormalMode, answers)

      result shouldBe routes.ConfirmBreakClauseController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return WhatIsYourRentBasedOnController when no is select, WhatTypeOfAgreementPage is Written and ParkingSpacesOrGaragesNotIncludedInYourRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, false)
        .flatMap(_.set(WhatTypeOfAgreementPage, "Written"))
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.nextPage(DoYouPayExtraForParkingSpacesPage, NormalMode, answers)

      result shouldBe routes.WhatIsYourRentBasedOnController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return RepairsAndInsuranceController when no is select, WhatTypeOfAgreementPage is Verbal and ParkingSpacesOrGaragesNotIncludedInYourRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, false)
        .flatMap(_.set(WhatTypeOfAgreementPage, "Verbal"))
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.nextPage(DoYouPayExtraForParkingSpacesPage, NormalMode, answers)

      result shouldBe routes.RepairsAndInsuranceController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return RepairsAndInsuranceController when no is select, rentBasedOn is Other and ParkingSpacesOrGaragesNotIncludedInYourRentPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, false)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.nextPage(DoYouPayExtraForParkingSpacesPage, NormalMode, answers)

      result shouldBe routes.RepairsAndInsuranceController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for RepairsAndFittingOutPage" should {
    "return DidYouGetMoneyFromLandlordController when no is select and AboutRepairsAndFittingOutPage is removed" in {
      val answers = userAnswersWithoutData
        .set(RepairsAndFittingOutPage, false)
        .flatMap(_.set(AboutRepairsAndFittingOutPage, aboutRepairsAndFittingOutModel))
        .success.value

      val result = navigator.nextPage(RepairsAndFittingOutPage, NormalMode, answers)

      result shouldBe routes.DidYouGetMoneyFromLandlordController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "normalRoute for HowMuchWasTheLumpSumPage" should {
    "return HasAnythingElseAffectedTheRentController when only YesLumpSum is select and AboutTheRentFreePeriodPage is removed" in {
      val answers = userAnswersWithoutData
        .set(HowMuchWasTheLumpSumPage, BigDecimal(1000))
        .flatMap(_.set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesLumpSum))))
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .success.value

      val result = navigator.nextPage(HowMuchWasTheLumpSumPage, NormalMode, answers)

      result shouldBe routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return AboutTheRentFreePeriodController when YesLumpSum and YesRentFreePeriod are select and AboutTheRentFreePeriodPage is not removed" in {
      val answers = userAnswersWithoutData
        .set(HowMuchWasTheLumpSumPage, BigDecimal(1000))
        .flatMap(_.set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesLumpSum, YesRentFreePeriod))))
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .success.value

      val result = navigator.nextPage(HowMuchWasTheLumpSumPage, NormalMode, answers)

      result shouldBe routes.AboutTheRentFreePeriodController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "normalRoute for DidYouPayAnyMoneyToLandlordPage" should {
    "return HasAnythingElseAffectedTheRentController when no is select and MoneyYouPaidInAdvanceToLandlordPage is removed" in {
      val answers = userAnswersWithoutData
        .set(DidYouPayAnyMoneyToLandlordPage, false)
        .flatMap(_.set(MoneyYouPaidInAdvanceToLandlordPage, moneyYouPaidInAdvanceToLandlordModel))
        .success.value

      val result = navigator.nextPage(DidYouPayAnyMoneyToLandlordPage, NormalMode, answers)

      result shouldBe routes.HasAnythingElseAffectedTheRentController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "checkRouteMap for WhatTypeOfAgreementPage" should {
    "return AgreementVerbalController when value is Verbal and AgreementVerbalPage is missing" in {
      val answers = userAnswersWithoutData
        .set(WhatTypeOfAgreementPage, "Verbal")
        .flatMap(_.set(AgreementPage, agreementModel))
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(false)(answers)

      result shouldBe routes.AgreementVerbalController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return CheckAnswersController when value is Verbal and AgreementVerbalPage is present" in {
      val answers = userAnswersWithoutData
        .set(WhatTypeOfAgreementPage, "Verbal")
        .flatMap(_.set(AgreementVerbalPage, agreementVerbalModel))
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return AgreementController when value is Written and AgreementPage is missing" in {
      val answers = userAnswersWithoutData
        .set(WhatTypeOfAgreementPage, "Written")
        .flatMap(_.set(AgreementVerbalPage, agreementVerbalModel))
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(false)(answers)

      result shouldBe routes.AgreementController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when value is Written and AgreementPage is present" in {
      val answers = userAnswersWithoutData
        .set(WhatTypeOfAgreementPage, "Written")
        .flatMap(_.set(AgreementPage, agreementModel))
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return WhatTypeOfAgreementController when WhatTypeOfAgreementPage is missing" in {
      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(false)(userAnswersWithoutData)

      result shouldBe routes.WhatTypeOfAgreementController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for AgreementVerbalPage" should {
    "return HowMuchIsTotalAnnualRentController when HowMuchIsTotalAnnualRentPage is missing" in {
      val answers = userAnswersWithoutData
        .set(AgreementVerbalPage, agreementVerbalModel)
        .success.value

      val result = navigator.checkRouteMap(AgreementVerbalPage)(false)(answers)

      result shouldBe routes.HowMuchIsTotalAnnualRentController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when HowMuchIsTotalAnnualRentPage is present" in {
      val answers = userAnswersWithoutData
        .set(AgreementVerbalPage, agreementVerbalModel)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.checkRouteMap(AgreementVerbalPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for AgreementPage" should {
    "return WhatIsYourRentBasedOnController when WhatIsYourRentBasedOnPage is missing" in {
      val answers = userAnswersWithoutData
        .set(AgreementPage, agreementModel)
        .success.value

      val result = navigator.checkRouteMap(AgreementPage)(false)(answers)

      result shouldBe routes.WhatIsYourRentBasedOnController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when WhatIsYourRentBasedOnPage is present" in {
      val answers = userAnswersWithoutData
        .set(AgreementPage, agreementModel)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .success.value

      val result = navigator.checkRouteMap(AgreementPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for RentFreePeriodPage" should {
    "return RentDatesAgreeStartController when RentDatesAgreeStartPage is missing" in {
      val answers = userAnswersWithoutData
        .set(RentFreePeriodPage, rentFreePeriodModel)
        .success.value

      val result = navigator.checkRouteMap(RentFreePeriodPage)(false)(answers)

      result shouldBe routes.RentDatesAgreeStartController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when RentDatesAgreeStartPage is present" in {
      val answers = userAnswersWithoutData
        .set(RentFreePeriodPage, rentFreePeriodModel)
        .flatMap(_.set(RentDatesAgreeStartPage, rentDatesAgreeStartModel))
        .success.value

      val result = navigator.checkRouteMap(RentFreePeriodPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for WhatIsYourRentBasedOnPage" should {

    "return CheckAnswersController when rentBased is Other and TellUsAboutRentPage is present" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllYes))
        .flatMap(_.set(RepairsAndInsurancePage, repairsAndInsuranceModel))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return WhatYourRentIncludesController when rentBased is Other and TellUsAboutRentPage is present but RepairsAndInsurancePage is missing" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllYes))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return AgreedRentChangeController when rentBased is Other, WhatYourRentIncludesPage and AgreedRentChangePage are missing" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.AgreedRentChangeController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return WhatYourRentIncludesController when rentBased is Other and TellUsAboutRentPage missing and WhatYourRentIncludesPage last 3 questions are missing but AgreedRentChangePage is provided" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(AgreedRentChangePage, true))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelForTOC))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when rentBased is Other and TellUsAboutRentPage is missing but AgreedRentChangePage and WhatYourRentIncludesPage are provided " in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(AgreedRentChangePage, true))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllYes))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return HowMuchIsTotalAnnualRentController when rentBased is PercentageTurnover and HowMuchIsTotalAnnualRentPage are missing" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .flatMap(_.set(AgreedRentChangePage, true))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.HowMuchIsTotalAnnualRentController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return HowMuchIsTotalAnnualRentController when rentBased is PercentageTurnover and AgreedRentChangePage are removed" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .flatMap(_.set(AgreedRentChangePage, true))
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.HowMuchIsTotalAnnualRentController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return WhatYourRentIncludesController when rentBased is PercentageTurnover, AgreedRentChangePage and WhatYourRentIncludesPage last 3 questions are missing" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelForTOC))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when rentBased is PercentageTurnover and HowMuchIsTotalAnnualRentPage is present but TellUsAboutRentPage is missing" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return WhatYourRentIncludesController when rentBased is PercentageTurnover and TellUsAboutRentPage but RepairsAndInsurancePage is missing" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelForTOC))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return CheckAnswersController when rentBased is PercentageTurnover, TellUsAboutRentPage and RepairsAndInsurancePage are provided" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .flatMap(_.set(RepairsAndInsurancePage, repairsAndInsuranceModel))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return WhatRentIncludesRatesWaterServiceController when rentBased is TOC, TellUsAboutRentPage and RepairsAndInsurancePage are provided" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnTOC)
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .flatMap(_.set(RepairsAndInsurancePage, repairsAndInsuranceModel))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.WhatRentIncludesRatesWaterServiceController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return CheckAnswersController when rentBased is TOC and TellUsAboutRentPage but RepairsAndInsurancePage is missing" in {
      val answers = userAnswersWithoutData
        .set(WhatIsYourRentBasedOnPage, rentBasedOnTOC)
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for AgreedRentChangePage" should {

    "return WhatYourRentIncludesController when AgreedRentChange is true, ProvideDetailsOfFirstRentPeriodPage has been entered but WhatYourRentIncludesPage and HowMuchIsTotalAnnualRentPage are missing" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, true)
        .flatMap(_.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return WhatRentIncludesRatesWaterServiceController when AgreedRentChange is true, ProvideDetailsOfFirstRentPeriodPage has been entered and rentBaseOn is TOC" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, true)
        .flatMap(_.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod))
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.WhatRentIncludesRatesWaterServiceController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return ProvideDetailsOfFirstRentPeriodController when AgreedRentChange is true and ProvideDetailsOfFirstRentPeriodPage is missing" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, true)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.ProvideDetailsOfFirstRentPeriodController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when AgreedRentChange is true and ProvideDetailsOfFirstRentPeriodPage is provided" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, true)
        .flatMap(_.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController when AgreedRentChange is false, HowMuchIsTotalAnnualRentPage and rentIncludes are provided" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, false)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return AgreedRentChangeController when AgreedRentChange is false and HowMuchIsTotalAnnualRentPage is missing" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, false)
        .flatMap(_.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriodNoRentPayed))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.HowMuchIsTotalAnnualRentController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return WhatYourRentIncludesController when AgreedRentChange is false, HowMuchIsTotalAnnualRentPage has been entered but WhatYourRentIncludesPage and ProvideDetailsOfFirstRentPeriodPage are missing" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, false)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return WhatRentIncludesRatesWaterServiceController when AgreedRentChange is false, HowMuchIsTotalAnnualRentPage has been entered and rentBaseOn is TOC" in {
      val answers = userAnswersWithoutData
        .set(AgreedRentChangePage, false)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(answers)

      result shouldBe routes.WhatRentIncludesRatesWaterServiceController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return AgreedRentChangeController when AgreedRentChangePage is missing" in {
      val result = navigator.checkRouteMap(AgreedRentChangePage)(false)(userAnswersWithoutData)

      result shouldBe routes.AgreedRentChangeController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for HowMuchIsTotalAnnualRentPage" should {
    "return CheckRentFreePeriodController when TellUsAboutYourRenewedAgreementPage is provided and CheckRentFreePeriodPage is missing" in {
      val answers = userAnswersWithoutData
        .set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000"))
        .flatMap(_.set(TellUsAboutYourRenewedAgreementPage, RenewedAgreement))
        .success.value

      val result = navigator.checkRouteMap(HowMuchIsTotalAnnualRentPage)(false)(answers)

      result shouldBe routes.CheckRentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return RepairsAndInsuranceController when TellUsAboutYourRenewedAgreementPage and CheckRentFreePeriodPage are provided, rentBasedOn Other and RepairsAndInsurancePage is missing" in {
      val answers = userAnswersWithoutData
        .set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000"))
        .flatMap(_.set(TellUsAboutYourRenewedAgreementPage, RenewedAgreement))
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(CheckRentFreePeriodPage, true))
        .success.value

      val result = navigator.checkRouteMap(HowMuchIsTotalAnnualRentPage)(false)(answers)

      result shouldBe routes.RepairsAndInsuranceController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when TellUsAboutYourRenewedAgreementPage, CheckRentFreePeriodPage, rentBasedOn Other and RepairsAndInsurancePage are provided" in {
      val answers = userAnswersWithoutData
        .set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000"))
        .flatMap(_.set(TellUsAboutYourRenewedAgreementPage, RenewedAgreement))
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(CheckRentFreePeriodPage, true))
        .flatMap(_.set(RepairsAndInsurancePage, repairsAndInsuranceModel))
        .success.value

      val result = navigator.checkRouteMap(HowMuchIsTotalAnnualRentPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckRentFreePeriodController when TellUsAboutYourRenewedAgreementPage and CheckRentFreePeriodPage are missing" in {
      val answers = userAnswersWithoutData
        .set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000"))
        .success.value

      val result = navigator.checkRouteMap(HowMuchIsTotalAnnualRentPage)(false)(answers)

      result shouldBe routes.CheckRentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when TellUsAboutYourRenewedAgreementPage is missing but CheckRentFreePeriodPage is provided" in {
      val answers = userAnswersWithoutData
        .set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000"))
        .flatMap(_.set(CheckRentFreePeriodPage, true))
        .success.value

      val result = navigator.checkRouteMap(HowMuchIsTotalAnnualRentPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for RentDatesAgreePage" should {
    "return WhatYourRentIncludesController when WhatYourRentIncludesPage is missing and rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreePage, "2025-12-04")
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreePage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return WhatYourRentIncludesController when WhatYourRentIncludesPage is missing last 3 questions and rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreePage, "2025-12-04")
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelForTOC))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreePage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when WhatYourRentIncludesPage is provided and rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreePage, "2025-12-04")
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return WhatRentIncludesRatesWaterServiceController when WhatYourRentIncludesPage is missing and rentBasedOn is TOC" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreePage, "2025-12-04")
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreePage)(false)(answers)

      result shouldBe routes.WhatRentIncludesRatesWaterServiceController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when WhatYourRentIncludesPage is provided and rentBasedOn is TOC" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreePage, "2025-12-04")
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for RentDatesAgreeStartPage" should {
    "return WhatYourRentIncludesController when WhatYourRentIncludesPage is missing and rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreeStartPage, rentDatesAgreeStartModel)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreeStartPage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return WhatYourRentIncludesController when WhatYourRentIncludesPage is missing last 3 questions and rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreeStartPage, rentDatesAgreeStartModel)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelForTOC))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreeStartPage)(false)(answers)

      result shouldBe routes.WhatYourRentIncludesController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when WhatYourRentIncludesPage is provided and rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreeStartPage, rentDatesAgreeStartModel)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreeStartPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return WhatRentIncludesRatesWaterServiceController when WhatYourRentIncludesPage is missing and rentBasedOn is TOC" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreeStartPage, rentDatesAgreeStartModel)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreeStartPage)(false)(answers)

      result shouldBe routes.WhatRentIncludesRatesWaterServiceController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when WhatYourRentIncludesPage is provided and rentBasedOn is TOC" in {
      val answers = userAnswersWithoutData
        .set(RentDatesAgreeStartPage, rentDatesAgreeStartModel)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .flatMap(_.set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo))
        .success.value

      val result = navigator.checkRouteMap(RentDatesAgreeStartPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for WhatYourRentIncludesPage" should {
    "return RepairsAndInsuranceController when rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .success.value

      val result = navigator.checkRouteMap(WhatYourRentIncludesPage)(false)(answers)

      result shouldBe routes.RepairsAndInsuranceController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return RepairsAndInsuranceController when AgreementVerbalPage is provide" in {
      val answers = userAnswersWithoutData
        .set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo)
        .flatMap(_.set(AgreementVerbalPage, agreementVerbalModel))
        .success.value

      val result = navigator.checkRouteMap(WhatYourRentIncludesPage)(false)(answers)

      result shouldBe routes.RepairsAndInsuranceController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when AgreementVerbalPage and RepairsAndInsurancePage are provide and rentBasedOn is Other" in {
      val answers = userAnswersWithoutData
        .set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo)
        .flatMap(_.set(AgreementVerbalPage, agreementVerbalModel))
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnModel))
        .flatMap(_.set(RepairsAndInsurancePage, repairsAndInsuranceModel))
        .success.value

      val result = navigator.checkRouteMap(WhatYourRentIncludesPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when rentBasedOn is TOC" in {
      val answers = userAnswersWithoutData
        .set(WhatYourRentIncludesPage, whatYourRentIncludesModelAllNo)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, rentBasedOnTOC))
        .success.value

      val result = navigator.checkRouteMap(WhatYourRentIncludesPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for CheckRentFreePeriodPage" should {
    "return CheckAnswersController when CheckRentFreePeriod is true and RentFreePeriodPage is present" in {
      val answers = userAnswersWithoutData
        .set(CheckRentFreePeriodPage, true)
        .flatMap(_.set(RentFreePeriodPage, RentFreePeriod(months = 2, reasons = "Was not in the country"))).success.value

      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return RentFreePeriodController when CheckRentFreePeriod is true and RentFreePeriodPage is missing" in {
      val answers = userAnswersWithoutData
        .set(CheckRentFreePeriodPage, true)
        .success.value

      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(false)(answers)

      result shouldBe routes.RentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove RentFreePeriodPage when CheckRentFreePeriod is false" in {
      val answers = userAnswersWithoutData
        .set(CheckRentFreePeriodPage, false)
        .flatMap(_.set(RentFreePeriodPage, RentFreePeriod(months = 2, reasons = "Was not in the country")))
        .flatMap(_.set(RentDatesAgreeStartPage, rentDatesAgreeStartModel))
        .success.value

      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return RentDatesAgreeStartController and remove RentFreePeriodPage when CheckRentFreePeriod is false and RentDatesAgreeStartPage is missing" in {
      val answers = userAnswersWithoutData
        .set(CheckRentFreePeriodPage, false)
        .flatMap(_.set(RentFreePeriodPage, RentFreePeriod(months = 2, reasons = "Was not in the country")))
        .success.value

      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(false)(answers)

      result shouldBe routes.RentDatesAgreeStartController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckRentFreePeriodController when CheckRentFreePeriodPage is missing" in {
      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(false)(userAnswersWithoutData)

      result shouldBe routes.CheckRentFreePeriodController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for DoesYourRentIncludeParkingPage" should {

    "return CheckAnswersController when DoesYourRentIncludeParking is true and HowManyParkingSpacesOrGaragesIncludedInRentPage is present" in {
      val answers = userAnswersWithoutData
        .set(DoesYourRentIncludeParkingPage, true)
        .flatMap(_.set(HowManyParkingSpacesOrGaragesIncludedInRentPage, parkingSpacesIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return HowManyParkingSpacesOrGaragesIncludedInRentController when DoesYourRentIncludeParking is true and HowManyParkingSpacesOrGaragesIncludedInRentPage is missing" in {
      val answers = userAnswersWithoutData
        .set(DoesYourRentIncludeParkingPage, true)
        .success.value

      val result = navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(false)(answers)

      result shouldBe routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove HowManyParkingSpacesOrGaragesIncludedInRentPage when DoesYourRentIncludeParking is false" in {
      val answers = userAnswersWithoutData
        .set(DoesYourRentIncludeParkingPage, false)
        .flatMap(_.set(HowManyParkingSpacesOrGaragesIncludedInRentPage, parkingSpacesIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return DoesYourRentIncludeParkingController when DoesYourRentIncludeParkingPage is missing" in {
      val result = navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(false)(userAnswersWithoutData)

      result shouldBe routes.DoesYourRentIncludeParkingController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for DoYouPayExtraForParkingSpacesPage" should {

    "return CheckAnswersController when DoYouPayExtraForParkingSpaces is true and ParkingSpacesOrGaragesNotIncludedInYourRentPage is present" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, true)
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return ParkingSpacesOrGaragesNotIncludedInYourRentController when DoYouPayExtraForParkingSpaces is true and ParkingSpacesOrGaragesNotIncludedInYourRentPage is missing" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, true)
        .success.value

      val result = navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(false)(answers)

      result shouldBe routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove ParkingSpacesOrGaragesNotIncludedInYourRentPage when DoYouPayExtraForParkingSpaces is false" in {
      val answers = userAnswersWithoutData
        .set(DoYouPayExtraForParkingSpacesPage, false)
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return DoYouPayExtraForParkingSpacesController when DoYouPayExtraForParkingSpacesPage is missing" in {
      val result = navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(false)(userAnswersWithoutData)

      result shouldBe routes.DoYouPayExtraForParkingSpacesController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for DidYouGetMoneyFromLandlordPage" should {

    "return CheckAnswersController when DidYouGetMoneyFromLandlord is true and MoneyToTakeOnTheLeasePage is present" in {
      val answers = userAnswersWithoutData
        .set(DidYouGetMoneyFromLandlordPage, true)
        .flatMap(_.set(MoneyToTakeOnTheLeasePage, MoneyToTakeOnTheLease(10000, "2000-01-01")))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetMoneyFromLandlordPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return MoneyToTakeOnTheLeaseController when DidYouGetMoneyFromLandlord is true and MoneyToTakeOnTheLeasePage is missing" in {
      val answers = userAnswersWithoutData
        .set(DidYouGetMoneyFromLandlordPage, true)
        .success.value

      val result = navigator.checkRouteMap(DidYouGetMoneyFromLandlordPage)(false)(answers)

      result shouldBe routes.MoneyToTakeOnTheLeaseController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove MoneyToTakeOnTheLeasePage when DidYouGetMoneyFromLandlord is false" in {
      val answers = userAnswersWithoutData
        .set(DidYouGetMoneyFromLandlordPage, false)
        .flatMap(_.set(MoneyToTakeOnTheLeasePage, MoneyToTakeOnTheLease(10000, "2000-01-01")))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetMoneyFromLandlordPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return DidYouGetMoneyFromLandlordController when DidYouGetMoneyFromLandlordPage is missing" in {
      val result = navigator.checkRouteMap(DidYouGetMoneyFromLandlordPage)(false)(userAnswersWithoutData)

      result shouldBe routes.DidYouGetMoneyFromLandlordController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for DidYouPayAnyMoneyToLandlordPage" should {

    "return CheckAnswersController when DidYouPayAnyMoneyToLandlord is true and MoneyYouPaidInAdvanceToLandlordPage is present" in {
      val answers = userAnswersWithoutData
        .set(DidYouPayAnyMoneyToLandlordPage, true)
        .flatMap(_.set(MoneyYouPaidInAdvanceToLandlordPage, paymentAdvance))
        .success.value

      val result = navigator.checkRouteMap(DidYouPayAnyMoneyToLandlordPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return MoneyYouPaidInAdvanceToLandlordController when DidYouPayAnyMoneyToLandlord is true and MoneyYouPaidInAdvanceToLandlordPage is missing" in {
      val answers = userAnswersWithoutData
        .set(DidYouPayAnyMoneyToLandlordPage, true)
        .success.value

      val result = navigator.checkRouteMap(DidYouPayAnyMoneyToLandlordPage)(false)(answers)

      result shouldBe routes.MoneyYouPaidInAdvanceToLandlordController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove MoneyYouPaidInAdvanceToLandlordPage when DidYouPayAnyMoneyToLandlord is false" in {
      val answers = userAnswersWithoutData
        .set(DidYouPayAnyMoneyToLandlordPage, false)
        .flatMap(_.set(MoneyYouPaidInAdvanceToLandlordPage, paymentAdvance))
        .success.value

      val result = navigator.checkRouteMap(DidYouPayAnyMoneyToLandlordPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return DidYouPayAnyMoneyToLandlordController when DidYouPayAnyMoneyToLandlordPage is missing" in {
      val result = navigator.checkRouteMap(DidYouPayAnyMoneyToLandlordPage)(false)(userAnswersWithoutData)

      result shouldBe routes.DidYouPayAnyMoneyToLandlordController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for DidYouAgreeRentWithLandlordPage" should {

    "return CheckAnswersController and update answers when DidYouAgreeRentWithLandlord is true, CheckRentFreePeriodPage is provided" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, true)
        .flatMap(_.set(CheckRentFreePeriodPage, false))
        .flatMap(_.set(RentInterimPage, false))
        .success.value

      val result = navigator.checkRouteMap(DidYouAgreeRentWithLandlordPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController and update answers when DidYouAgreeRentWithLandlord is true, ProvideDetailsOfSecondRentPeriodPage and RentDatesAgreePage are provided" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, true)
        .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod))
        .flatMap(_.set(RentDatesAgreePage, "2025-12-04"))
        .flatMap(_.set(RentInterimPage, false))
        .success.value

      val result = navigator.checkRouteMap(DidYouAgreeRentWithLandlordPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return RentDatesAgreeController and update answers when DidYouAgreeRentWithLandlord is true, ProvideDetailsOfSecondRentPeriodPage is provided but RentDatesAgreePage is missing" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, true)
        .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod))
        .flatMap(_.set(RentInterimPage, false))
        .success.value

      val result = navigator.checkRouteMap(DidYouAgreeRentWithLandlordPage)(false)(answers)

      result shouldBe routes.RentDatesAgreeController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckRentFreePeriodController and update answers when DidYouAgreeRentWithLandlord is true, CheckRentFreePeriodPage is missing" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, true)
        .flatMap(_.set(RentInterimPage, false))
        .success.value

      val result = navigator.checkRouteMap(DidYouAgreeRentWithLandlordPage)(false)(answers)

      result shouldBe routes.CheckRentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when DidYouAgreeRentWithLandlord is false and RentInterimPage is present" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, false)
        .flatMap(_.set(RentInterimPage, true))
        .success.value

      val result = navigator.checkRouteMap(DidYouAgreeRentWithLandlordPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return RentInterimController when DidYouAgreeRentWithLandlord is false and RentInterimPage is missing" in {
      val answers = userAnswersWithoutData
        .set(DidYouAgreeRentWithLandlordPage, false)
        .success.value

      val result = navigator.checkRouteMap(DidYouAgreeRentWithLandlordPage)(false)(answers)

      result shouldBe routes.RentInterimController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return DidYouAgreeRentWithLandlordController when DidYouAgreeRentWithLandlordPage is missing" in {
      val result = navigator.checkRouteMap(DidYouAgreeRentWithLandlordPage)(false)(userAnswersWithoutData)

      result shouldBe routes.DidYouAgreeRentWithLandlordController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  "checkRouteMap for RentInterimPage" should {
    "return CheckAnswersController when RentInterimPage is true and InterimSetByTheCourtPage is present" in {
      val answers = userAnswersWithoutData
        .set(RentInterimPage, true)
        .flatMap(_.set(InterimSetByTheCourtPage, interimRentSetByTheCourtModel))
        .success.value

      val result = navigator.checkRouteMap(RentInterimPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return InterimRentSetByTheCourtController when RentInterimPage is true and InterimSetByTheCourtPage is present" in {
      val answers = userAnswersWithoutData
        .set(RentInterimPage, true)
        .success.value

      val result = navigator.checkRouteMap(RentInterimPage)(false)(answers)

      result shouldBe routes.InterimRentSetByTheCourtController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when RentInterimPage is false and InterimSetByTheCourtPage is removed" in {
      val answers = userAnswersWithoutData
        .set(RentInterimPage, false)
        .flatMap(_.set(InterimSetByTheCourtPage, interimRentSetByTheCourtModel))
        .success.value

      val result = navigator.checkRouteMap(RentInterimPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
  }
  "checkRouteMap for ConfirmBreakClausePage" should {

    "return CheckAnswersController when ConfirmBreakClause is true and DidYouGetIncentiveForNotTriggeringBreakClausePage is present" in {
      val answers = userAnswersWithoutData
        .set(ConfirmBreakClausePage, true)
        .flatMap(_.set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesLumpSum, YesRentFreePeriod))))
        .success.value

      val result = navigator.checkRouteMap(ConfirmBreakClausePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return DidYouGetIncentiveForNotTriggeringBreakClauseController when ConfirmBreakClause is true and DidYouGetIncentiveForNotTriggeringBreakClausePage is missing" in {
      val answers = userAnswersWithoutData
        .set(ConfirmBreakClausePage, true)
        .success.value

      val result = navigator.checkRouteMap(ConfirmBreakClausePage)(false)(answers)

      result shouldBe routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and update answers when ConfirmBreakClause is false" in {
      val answers = userAnswersWithoutData
        .set(ConfirmBreakClausePage, false)
        .flatMap(_.set(DidYouGetIncentiveForNotTriggeringBreakClausePage, DidYouGetIncentiveForNotTriggeringBreakClause(checkBox = Set(YesLumpSum, YesRentFreePeriod))))
        .success.value

      val result = navigator.checkRouteMap(ConfirmBreakClausePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "throw NotFoundException when ConfirmBreakClausePage is missing" in {
      val result = navigator.checkRouteMap(ConfirmBreakClausePage)(false)(userAnswersWithoutData)

      result shouldBe routes.ConfirmBreakClauseController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }


  "checkRouteMap for DidYouGetIncentiveForNotTriggeringBreakClausePage" should {

    "return AboutTheRentFreePeriodController when only YesRentFreePeriod selected and AboutTheRentFreePeriodPage is missing" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesRentFreePeriod))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .success
        .value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)
      result shouldBe routes.AboutTheRentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController when only YesRentFreePeriod selected and AboutTheRentFreePeriodPage is provided" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesRentFreePeriod))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .success
        .value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)
      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController when only YesRentFreePeriod selected, AboutTheRentFreePeriodPage is provided and HowMuchWasTheLumpSumPage is removed" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesRentFreePeriod))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .success
        .value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)
      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }


    "return HowMuchWasTheLumpSumController when YesLumpSum selected and HowMuchWasTheLumpSumPage is missing" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesLumpSum))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .success.value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)

      result shouldBe routes.HowMuchWasTheLumpSumController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController when only YesLumpSum selected, HowMuchWasTheLumpSumPage is provided and AboutTheRentFreePeriodPage is removed" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesLumpSum))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return AboutTheRentFreePeriodController when YesLumpSum and YesLumpSum selected, HowMuchWasTheLumpSumPage is provided but AboutTheRentFreePeriodPage is missing" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesRentFreePeriod, YesLumpSum))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)

      result shouldBe routes.AboutTheRentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController when all required pages are present" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesRentFreePeriod, YesLumpSum))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController when no is selected and HowMuchWasTheLumpSumPage and AboutTheRentFreePeriodPage are removed" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(No))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when no is selected and HowMuchWasTheLumpSumPage is removed" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(No))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when no is selected and AboutTheRentFreePeriodPage is removed" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(No))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .success.value

      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return DidYouGetIncentiveForNotTriggeringBreakClauseController when DidYouGetIncentiveForNotTriggeringBreakClausePage is missing" in {
      val result = navigator.checkRouteMap(DidYouGetIncentiveForNotTriggeringBreakClausePage)(false)(userAnswersWithoutData)

      result shouldBe routes.DidYouGetIncentiveForNotTriggeringBreakClauseController.show(NormalMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for HowMuchWasTheLumpSumPage" should {
    "return CheckAnswersController when YesRentFreePeriod is selected and AboutTheRentFreePeriodPage is provided" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesRentFreePeriod, YesLumpSum))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .flatMap(_.set(AboutTheRentFreePeriodPage, aboutTheRentFreePeriodModel))
        .success.value

      val result = navigator.checkRouteMap(HowMuchWasTheLumpSumPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when only YesLumpSum is selected" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesLumpSum))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .success.value

      val result = navigator.checkRouteMap(HowMuchWasTheLumpSumPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return AboutTheRentFreePeriodController when YesRentFreePeriod is selected and AboutTheRentFreePeriodPage is missing" in {
      val incentive = DidYouGetIncentiveForNotTriggeringBreakClause(Set(YesRentFreePeriod, YesLumpSum))
      val answers = userAnswersWithoutData
        .set(DidYouGetIncentiveForNotTriggeringBreakClausePage, incentive)
        .flatMap(_.set(HowMuchWasTheLumpSumPage, BigDecimal(7500.00)))
        .success.value

      val result = navigator.checkRouteMap(HowMuchWasTheLumpSumPage)(false)(answers)

      result shouldBe routes.AboutTheRentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for RepairsAndFittingOutPage" should {
    "return CheckAnswersController when RepairsAndFittingOutPage is true and AboutRepairsAndFittingOutPage is provided" in {
      val answers = userAnswersWithoutData
        .set(RepairsAndFittingOutPage, true)
        .flatMap(_.set(AboutRepairsAndFittingOutPage, aboutRepairsAndFittingOutModel))
        .success.value

      val result = navigator.checkRouteMap(RepairsAndFittingOutPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when RepairsAndFittingOutPage is false and AboutRepairsAndFittingOutPage is removed" in {
      val answers = userAnswersWithoutData
        .set(RepairsAndFittingOutPage, false)
        .flatMap(_.set(AboutRepairsAndFittingOutPage, aboutRepairsAndFittingOutModel))
        .success.value

      val result = navigator.checkRouteMap(RepairsAndFittingOutPage)(false)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }
    "return AboutRepairsAndFittingOutController when RepairsAndFittingOutPage is true and AboutRepairsAndFittingOutPage is missing" in {
      val answers = userAnswersWithoutData
        .set(RepairsAndFittingOutPage, true)
        .success.value

      val result = navigator.checkRouteMap(RepairsAndFittingOutPage)(false)(answers)

      result shouldBe routes.AboutRepairsAndFittingOutController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for ProvideDetailsOfFirstRentPeriodPage" should {
    "return ProvideDetailsOfSecondRentPeriodController when shouldGoToSecondRentPeriod is true" in {
      val result = navigator.nextPage(ProvideDetailsOfFirstRentPeriodPage, CheckMode, userAnswersWithoutData, true)

      result shouldBe routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when shouldGoToSecondRentPeriod is false" in {
      val result = navigator.nextPage(ProvideDetailsOfFirstRentPeriodPage, CheckMode, userAnswersWithoutData, false)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  "checkRouteMap for ProvideDetailsOfSecondRentPeriodPage" should {
    "return RentPeriodsController when shouldGoToRentPeriodsPage is true" in {
      val result = navigator.nextPage(ProvideDetailsOfSecondRentPeriodPage, CheckMode, userAnswersWithoutData, true)

      result shouldBe routes.RentPeriodsController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when shouldGoToRentPeriodsPage is false" in {
      val result = navigator.nextPage(ProvideDetailsOfSecondRentPeriodPage, CheckMode, userAnswersWithoutData)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  "checkRouteMap for RentPeriodsPage" should {
    "return ProvideDetailsOfSecondRentPeriodController when rent periods seq is empty and user select yes for adding rent period" in {
      val answers = userAnswersWithoutData.set(RentPeriodsPage, true).success.value
      val result = navigator.nextPage(RentPeriodsPage, CheckMode, answers)

      result shouldBe routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return AdditionalRentPeriodController when rent periods seq is not empty and user select yes for adding rent period" in {
      val answers = userAnswersWithoutData.set(RentPeriodsPage, true)
        .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod))
        .success.value
      val result = navigator.nextPage(RentPeriodsPage, CheckMode, answers)

      result shouldBe routes.AdditionalRentPeriodController.show(CheckMode, detailsOfRentPeriod.size)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return RentDatesAgreeController when rent periods seq is not empty and user select yes for adding rent period" in {
      val answers = userAnswersWithoutData.set(RentPeriodsPage, false)
        .success.value
      val result = navigator.nextPage(RentPeriodsPage, CheckMode, answers)

      result shouldBe routes.RentDatesAgreeController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when user select no for adding rent period" in {
      val answers = userAnswersWithoutData.set(RentPeriodsPage, false)
        .flatMap(_.set(RentDatesAgreePage, "2025-01-01"))
        .success.value
      val result = navigator.nextPage(RentPeriodsPage, CheckMode, answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  "skipRepairsAndInsuranceIfRentBasedOnIsTOC" should {
    "return WhatIsYourRentBasedOnController if rentBasedOn is empty and type of agreement is not verbal" in {
      val answers = userAnswersWithoutData.set(WhatTypeOfAgreementPage, "LeaseOrTenancy").success.value
      val result = navigator.skipRepairsAndInsuranceIfRentBasedOnIsTOC(answers)

      result shouldBe routes.WhatIsYourRentBasedOnController.show(NormalMode)
    }
    "return RepairsAndInsuranceController if rentBasedOn is empty and type of agreement is verbal" in {
      val answers = userAnswersWithoutData.set(WhatTypeOfAgreementPage, "Verbal").success.value
      val result = navigator.skipRepairsAndInsuranceIfRentBasedOnIsTOC(answers)

      result shouldBe routes.RepairsAndInsuranceController.show(NormalMode)
    }
    "return RepairsAndInsuranceController if rentBasedOn is open market" in {
      val answers = userAnswersWithoutData.set(WhatIsYourRentBasedOnPage, RentBasedOn("OpenMarket", None)).success.value
      val result = navigator.skipRepairsAndInsuranceIfRentBasedOnIsTOC(answers)

      result shouldBe routes.RepairsAndInsuranceController.show(NormalMode)
    }
    "return RentReviewController if rentBasedOn is TOC in new agreement journey" in {
      val answers = userAnswersWithoutData.set(TellUsAboutYourNewAgreementPage, NewAgreement)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, RentBasedOn("TotalOccupancyCost", None))).success.value
      val result = navigator.skipRepairsAndInsuranceIfRentBasedOnIsTOC(answers)

      result shouldBe routes.RentReviewController.show(NormalMode)
    }
    "return ConfirmBreakClauseController if rentBasedOn is TOC in rent review journey" in {
      val answers = userAnswersWithoutData.set(TellUsAboutRentPage, RentAgreement)
        .flatMap(_.set(WhatIsYourRentBasedOnPage, RentBasedOn("TotalOccupancyCost", None))).success.value
      val result = navigator.skipRepairsAndInsuranceIfRentBasedOnIsTOC(answers)

      result shouldBe routes.ConfirmBreakClauseController.show(NormalMode)
    }
  }
}