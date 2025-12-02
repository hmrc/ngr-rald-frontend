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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import uk.gov.hmrc.ngrraldfrontend.utils.MoneyFormatter


class MoneyFormatterSpec extends AnyFlatSpec with Matchers {

  "format" should "format whole numbers with two decimal places" in {
    MoneyFormatter.format(BigDecimal(1000)) shouldBe "£1,000"
    MoneyFormatter.format(BigDecimal(50)) shouldBe "£50"
  }

  it should "format decimal values with exactly two decimal places" in {
    MoneyFormatter.format(BigDecimal(1234.5)) shouldBe "£1,234.50"
    MoneyFormatter.format(BigDecimal(99.99)) shouldBe "£99.99"
    MoneyFormatter.format(BigDecimal(99.991)) shouldBe "£99.99" 

  }

  it should "handle zero correctly" in {
    MoneyFormatter.format(BigDecimal(0)) shouldBe "£0"
  }

  it should "format large numbers with commas and two decimals" in {
    MoneyFormatter.format(BigDecimal(1000000)) shouldBe "£1,000,000"
  }
}