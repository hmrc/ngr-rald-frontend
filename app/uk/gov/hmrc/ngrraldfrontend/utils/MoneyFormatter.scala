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

import java.text.NumberFormat
import java.util.Locale
object MoneyFormatter {

  def format(amount: BigDecimal): String = {
    val ukFormatter = NumberFormat.getCurrencyInstance(Locale.UK)

    if (amount.isValidInt || amount.scale == 0 || amount % 1 == 0) {
      ukFormatter.setMaximumFractionDigits(0)
    } else {
      ukFormatter.setMinimumFractionDigits(2)
      ukFormatter.setMaximumFractionDigits(2)
    }

    ukFormatter.format(amount)
  }
}
