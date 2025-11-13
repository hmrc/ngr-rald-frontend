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

package uk.gov.hmrc.ngrraldfrontend.utils

import uk.gov.hmrc.ngrraldfrontend.models.{DetailsOfRentPeriod, NGRDate, UserAnswers}
import uk.gov.hmrc.ngrraldfrontend.pages.{ProvideDetailsOfFirstRentPeriodPage, ProvideDetailsOfSecondRentPeriodPage}

import java.time.LocalDate

object RentPeriodsHelper {

  def updateRentPeriodsIfEndDateIsChanged(userAnswers: UserAnswers, enteredEndDate: NGRDate, index: Int): Seq[DetailsOfRentPeriod] = {
    userAnswers.get(ProvideDetailsOfSecondRentPeriodPage) match {
      case Some(periods) if periods.size > index && periods(index).endDate != enteredEndDate.makeString =>
        periods.dropRight(periods.size - index)
      case Some(periods) => periods
      case None => Seq.empty
    }
  }

  def updateRentPeriodsIfFirstRentPeriodEndDateIsChanged(userAnswers: UserAnswers, enteredEndDate: LocalDate): UserAnswers = {
    userAnswers.get(ProvideDetailsOfFirstRentPeriodPage) match {
      case Some(firstPeriods) if !firstPeriods.endDate.isEqual(enteredEndDate) =>
        userAnswers.remove(ProvideDetailsOfSecondRentPeriodPage).get
      case _ =>
        userAnswers
    }
  }
  
}
