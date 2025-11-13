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

package uk.gov.hmrc.ngrraldfrontend.util

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.shouldBe
import uk.gov.hmrc.ngrraldfrontend.helpers.{TestData, TestSupport}
import uk.gov.hmrc.ngrraldfrontend.models.registration.CredId
import uk.gov.hmrc.ngrraldfrontend.models.{DetailsOfRentPeriod, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstRentPeriodPage, ProvideDetailsOfSecondRentPeriodPage}
import uk.gov.hmrc.ngrraldfrontend.utils.RentPeriodsHelper.{updateRentPeriodsIfEndDateIsChanged, updateRentPeriodsIfFirstRentPeriodEndDateIsChanged}
import uk.gov.hmrc.ngrraldfrontend.utils.UniqueIdGenerator

import java.time.LocalDate

class RentPeriodHelperSpec extends TestSupport with TestData {

  "RentPeriodHelper updateRentPeriodsIfEndDateIsChanged method" must {
    val userAnswers = UserAnswers(CredId("id")).set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod)
      .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod)).get
    "Return the DetailsOfRentPeriod Seq without removing any periods when the end date is the same" in {
      val actual = updateRentPeriodsIfEndDateIsChanged(userAnswers, NGRDate("31", "5", "2025"), 1)
      actual shouldBe detailsOfRentPeriod
    }
    "Return the DetailsOfRentPeriod Seq without the following periods when the end date has been changed" in {
      val actual = updateRentPeriodsIfEndDateIsChanged(userAnswers, NGRDate("31", "8", "2025"), 1)
      actual.size shouldBe 1
      actual.contains(secondRentPeriod) shouldBe true
    }
  }
  "RentPeriodHelper updateRentPeriodsIfFirstRentPeriodEndDateIsChanged method" must {
    val userAnswers = UserAnswers(CredId("id")).set(ProvideDetailsOfFirstRentPeriodPage, firstRentPeriod)
      .flatMap(_.set(ProvideDetailsOfSecondRentPeriodPage, detailsOfRentPeriod)).get
    "Return UserAnswers without removing any periods when the end date is the same" in {
      val actual = updateRentPeriodsIfFirstRentPeriodEndDateIsChanged(userAnswers, LocalDate.parse("2025-01-31"))
      actual shouldBe userAnswers
    }
    "Return UserAnswers without the following periods when the end date has been changed" in {
      val actual = updateRentPeriodsIfFirstRentPeriodEndDateIsChanged(userAnswers, LocalDate.parse("2025-02-28"))
      actual.get(ProvideDetailsOfSecondRentPeriodPage) shouldBe None
    }
  }
}
