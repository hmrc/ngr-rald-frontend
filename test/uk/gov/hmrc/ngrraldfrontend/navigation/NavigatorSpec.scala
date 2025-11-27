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
import uk.gov.hmrc.http.NotFoundException
import uk.gov.hmrc.ngrraldfrontend.controllers.routes
import uk.gov.hmrc.ngrraldfrontend.helpers.TestData
import uk.gov.hmrc.ngrraldfrontend.models.AgreementType.RentAgreement
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{CheckMode, NormalMode, RentBasedOn, RentFreePeriod, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.models.*
import uk.gov.hmrc.ngrraldfrontend.pages.*
import uk.gov.hmrc.ngrraldfrontend.repo.SessionRepository

class NavigatorSpec
  extends AnyWordSpec
    with Matchers
    with MockitoSugar
    with BeforeAndAfterEach
    with TestData {

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val navigator = new Navigator(mockSessionRepository)
  val answersWithoutData = UserAnswers(credId)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "checkRouteMap for WhatTypeOfAgreementPage" should {
    "return AgreementVerbalController when value is Verbal and AgreementVerbalPage is missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Verbal")
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(answers)

      result shouldBe routes.AgreementVerbalController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when value is Verbal and AgreementVerbalPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Verbal")
        .flatMap(_.set(AgreementVerbalPage, agreementVerbalModel))
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return AgreementController when value is Written and AgreementPage is missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Written")
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(answers)

      result shouldBe routes.AgreementController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when value is Written and AgreementPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatTypeOfAgreementPage, "Written")
        .flatMap(_.set(AgreementPage, agreementModel))
        .success.value

      val result = navigator.checkRouteMap(WhatTypeOfAgreementPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "throw NotFoundException when WhatTypeOfAgreementPage is missing" in {
      val answers = UserAnswers(CredId("1234"))

      an[NotFoundException] shouldBe thrownBy {
        navigator.checkRouteMap(WhatTypeOfAgreementPage)(answers)
      }
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for WhatIsYourRentBasedOnPage" should {

    "return CheckAnswersController when rentBased is Other and TellUsAboutRentPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(TellUsAboutRentPage, RentAgreement))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return HowMuchIsTotalAnnualRentController when rentBased is PercentageTurnover and TellUsAboutRentPage missing and HowMuchIsTotalAnnualRentPage missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(answers)

      result shouldBe routes.HowMuchIsTotalAnnualRentController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when rentBased is PercentageTurnover and HowMuchIsTotalAnnualRentPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatIsYourRentBasedOnPage, RentBasedOn("PercentageTurnover", None))
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return WhatIsYourRentBasedOnController when rentBased is Other and TellUsAboutRentPage missing and AgreedRentChangePage missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(answers)

      result shouldBe routes.AgreedRentChangeController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when rentBased is Other and AgreedRentChangePage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(WhatIsYourRentBasedOnPage, rentBasedOnModel)
        .flatMap(_.set(AgreedRentChangePage, true))
        .success.value

      val result = navigator.checkRouteMap(WhatIsYourRentBasedOnPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for AgreedRentChangePage" should {

    "return CheckAnswersController when AgreedRentChange is true and ProvideDetailsOfFirstRentPeriodPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(AgreedRentChangePage, true)
        .flatMap(_.set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return ProvideDetailsOfFirstRentPeriodController when AgreedRentChange is true and ProvideDetailsOfFirstRentPeriodPage is missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(AgreedRentChangePage, true)
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(answers)

      result shouldBe routes.ProvideDetailsOfFirstRentPeriodController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "return CheckAnswersController when AgreedRentChange is false and HowMuchIsTotalAnnualRentPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(AgreedRentChangePage, false)
        .flatMap(_.set(HowMuchIsTotalAnnualRentPage, BigDecimal("10000")))
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return AgreedRentChangeController when AgreedRentChange is false and HowMuchIsTotalAnnualRentPage is missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(AgreedRentChangePage, false)
        .success.value

      val result = navigator.checkRouteMap(AgreedRentChangePage)(answers)

      result shouldBe routes.AgreedRentChangeController.show(NormalMode)
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "throw NotFoundException when AgreedRentChangePage is missing" in {
      val answers = UserAnswers(CredId("1234"))

      an[NotFoundException] shouldBe thrownBy {
        navigator.checkRouteMap(AgreedRentChangePage)(answers)
      }
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for CheckRentFreePeriodPage" should {
    "return CheckAnswersController when CheckRentFreePeriod is true and RentFreePeriodPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(CheckRentFreePeriodPage, true)
        .flatMap(_.set(RentFreePeriodPage, RentFreePeriod(months = 2, reasons = "Was not in the country"))).success.value

      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return RentFreePeriodController when CheckRentFreePeriod is true and RentFreePeriodPage is missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(CheckRentFreePeriodPage, true)
        .success.value

      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(answers)

      result shouldBe routes.RentFreePeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove RentFreePeriodPage when CheckRentFreePeriod is false" in {
      val answers = UserAnswers(CredId("1234"))
        .set(CheckRentFreePeriodPage, false)
        .flatMap(_.set(RentFreePeriodPage, RentFreePeriod(months = 2, reasons = "Was not in the country"))).success.value

      val result = navigator.checkRouteMap(CheckRentFreePeriodPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "throw NotFoundException when CheckRentFreePeriodPage is missing" in {
      val answers = UserAnswers(CredId("1234"))

      an[NotFoundException] shouldBe thrownBy {
        navigator.checkRouteMap(CheckRentFreePeriodPage)(answers)
      }
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for DoesYourRentIncludeParkingPage" should {

    "return CheckAnswersController when DoesYourRentIncludeParking is true and HowManyParkingSpacesOrGaragesIncludedInRentPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(DoesYourRentIncludeParkingPage, true)
        .flatMap(_.set(HowManyParkingSpacesOrGaragesIncludedInRentPage, parkingSpacesIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return HowManyParkingSpacesOrGaragesIncludedInRentController when DoesYourRentIncludeParking is true and HowManyParkingSpacesOrGaragesIncludedInRentPage is missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(DoesYourRentIncludeParkingPage, true)
        .success.value

      val result = navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(answers)

      result shouldBe routes.HowManyParkingSpacesOrGaragesIncludedInRentController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove HowManyParkingSpacesOrGaragesIncludedInRentPage when DoesYourRentIncludeParking is false" in {
      val answers = UserAnswers(CredId("1234"))
        .set(DoesYourRentIncludeParkingPage, false)
        .flatMap(_.set(HowManyParkingSpacesOrGaragesIncludedInRentPage, parkingSpacesIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "throw NotFoundException when DoesYourRentIncludeParkingPage is missing" in {
      val answers = UserAnswers(CredId("1234"))

      an[NotFoundException] shouldBe thrownBy {
        navigator.checkRouteMap(DoesYourRentIncludeParkingPage)(answers)
      }
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for DoYouPayExtraForParkingSpacesPage" should {

    "return CheckAnswersController when DoYouPayExtraForParkingSpaces is true and ParkingSpacesOrGaragesNotIncludedInYourRentPage is present" in {
      val answers = UserAnswers(CredId("1234"))
        .set(DoYouPayExtraForParkingSpacesPage, true)
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return ParkingSpacesOrGaragesNotIncludedInYourRentController when DoYouPayExtraForParkingSpaces is true and ParkingSpacesOrGaragesNotIncludedInYourRentPage is missing" in {
      val answers = UserAnswers(CredId("1234"))
        .set(DoYouPayExtraForParkingSpacesPage, true)
        .success.value

      val result = navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(answers)

      result shouldBe routes.ParkingSpacesOrGaragesNotIncludedInYourRentController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }

    "return CheckAnswersController and remove ParkingSpacesOrGaragesNotIncludedInYourRentPage when DoYouPayExtraForParkingSpaces is false" in {
      val answers = UserAnswers(CredId("1234"))
        .set(DoYouPayExtraForParkingSpacesPage, false)
        .flatMap(_.set(ParkingSpacesOrGaragesNotIncludedInYourRentPage, parkingSpacesNotIncluded))
        .success.value

      val result = navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(answers)

      result shouldBe routes.CheckAnswersController.show
      verify(mockSessionRepository, times(1)).set(any[UserAnswers])
    }

    "throw NotFoundException when DoYouPayExtraForParkingSpacesPage is missing" in {
      val answers = UserAnswers(CredId("1234"))

      an[NotFoundException] shouldBe thrownBy {
        navigator.checkRouteMap(DoYouPayExtraForParkingSpacesPage)(answers)
      }
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
  "checkRouteMap for ProvideDetailsOfFirstRentPeriodPage" should {
    "return ProvideDetailsOfSecondRentPeriodController when shouldGoToSecondRentPeriod is true" in {
      val result = navigator.nextPage(ProvideDetailsOfFirstRentPeriodPage, CheckMode, answersWithoutData, true)

      result shouldBe routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when shouldGoToSecondRentPeriod is false" in {
      val result = navigator.nextPage(ProvideDetailsOfFirstRentPeriodPage, CheckMode, answersWithoutData, false)

      result shouldBe routes.CheckAnswersController.show()
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  "checkRouteMap for ProvideDetailsOfSecondRentPeriodPage" should {
    "return RentPeriodsController when shouldGoToRentPeriodsPage is true" in {
      val result = navigator.nextPage(ProvideDetailsOfSecondRentPeriodPage, CheckMode, answersWithoutData, true)

      result shouldBe routes.RentPeriodsController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when shouldGoToRentPeriodsPage is false" in {
      val result = navigator.nextPage(ProvideDetailsOfSecondRentPeriodPage, CheckMode, answersWithoutData)

      result shouldBe routes.CheckAnswersController.show()
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }

  "checkRouteMap for RentPeriodsPage" should {
    "return ProvideDetailsOfSecondRentPeriodController when rent periods seq is empty and user select yes for adding rent period" in {
      val answers = answersWithoutData.set(RentPeriodsPage, true).success.value
      val result = navigator.nextPage(RentPeriodsPage, CheckMode, answers)

      result shouldBe routes.ProvideDetailsOfSecondRentPeriodController.show(CheckMode)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return AdditionalRentPeriodController when rent periods seq is not empty and user select yes for adding rent period" in {
      val answers = answersWithoutData.set(RentPeriodsPage, true)
        .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod))
        .success.value
      val result = navigator.nextPage(RentPeriodsPage, CheckMode, answers)

      result shouldBe routes.AdditionalRentPeriodController.show(CheckMode, detailsOfRentPeriod.size)
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
    "return CheckAnswersController when user select no for adding rent period" in {
      val answers = answersWithoutData.set(RentPeriodsPage, false).success.value
      val result = navigator.nextPage(RentPeriodsPage, CheckMode, answers)

      result shouldBe routes.CheckAnswersController.show()
      verify(mockSessionRepository, never()).set(any[UserAnswers])
    }
  }
}


