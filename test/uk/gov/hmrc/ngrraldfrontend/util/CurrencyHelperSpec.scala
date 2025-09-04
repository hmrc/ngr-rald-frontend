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

import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.utils.CurrencyHelper


class CurrencyHelperSpec extends TestSupport with CurrencyHelper {

  "method formatRateableValue" must {

    "Adding comma in the correct positions" in {
      val actual = formatRentValue(1000000.00)
      actual mustBe "£1,000,000"
    }
    
    "Convert number into pound currency" in {
      val actual = formatRentValue(703.25)
      actual mustBe "£703.25"
    }
  }
}