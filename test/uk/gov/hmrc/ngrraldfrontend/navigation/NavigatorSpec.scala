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
import uk.gov.hmrc.ngrraldfrontend.controllers.routes
import uk.gov.hmrc.ngrraldfrontend.helpers.TestData
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
  val navigator = new Navigator
  val answersWithoutData = UserAnswers(credId)

  override def beforeEach(): Unit = {
    super.beforeEach()
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