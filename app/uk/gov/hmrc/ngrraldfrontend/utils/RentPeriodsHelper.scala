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

import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, ProvideDetailsOfRentPeriod}

object RentPeriodsHelper {

  def hasCurrentRentPeriodEndDateChanged(periods: Seq[ProvideDetailsOfRentPeriod], enteredEndDate: NGRDate, index: Int): Seq[ProvideDetailsOfRentPeriod] = {
    if (periods.size > index && periods(index).endDate != enteredEndDate.makeString)
      periods.dropRight(periods.size - index)
    else
      periods
  }
  
}
