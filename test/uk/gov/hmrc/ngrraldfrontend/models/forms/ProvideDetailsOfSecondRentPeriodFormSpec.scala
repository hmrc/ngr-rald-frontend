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

package uk.gov.hmrc.ngrraldfrontend.models.forms

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import uk.gov.hmrc.ngrraldfrontend.models.ProvideDetailsOfSecondRentPeriod

import java.time.LocalDate

class ProvideDetailsOfSecondRentPeriodFormSpec extends AnyWordSpec with Matchers:

  val secondRentPeriod: ProvideDetailsOfSecondRentPeriod =
    ProvideDetailsOfSecondRentPeriod(
      LocalDate.parse("2025-01-31"),
      BigDecimal(1999000)
    )

  "ProvideDetailsOfSecondRentPeriodForm" should {
    "bind successfully with all the data provided" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = ProvideDetailsOfSecondRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfSecondRentPeriod(
        LocalDate.parse("2025-01-31"),
        BigDecimal(1777000.99)
      ))
    }

    "bind successfully ensuring rentPeriodAmount is rounded to 2 decimal places" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1,777,000.449"
      )
      val boundForm = ProvideDetailsOfSecondRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfSecondRentPeriod(
        LocalDate.parse("2025-01-31"),
        BigDecimal(1777000.45)
      ))
    }
    
    "unbind correctly to a data Map" in {
      val form = ProvideDetailsOfSecondRentPeriodForm.form.fill(secondRentPeriod)
      form.data shouldBe Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1999000"
      )
    }

    "return error if endDate is invalid" in {
      val data = Map(
        "endDate.day" -> "33",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "777,000.00"
      )
      val boundForm = ProvideDetailsOfSecondRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.invalid.error")
      )
    }

    "return errors if endDate is before 1900" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "1899",
        "rentPeriodAmount" -> "777000"
      )
      val boundForm = ProvideDetailsOfSecondRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.before.1900.error")
      )
    }

    "return error if rentPeriodAmount is empty" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> ""
      )
      val boundForm = ProvideDetailsOfSecondRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.required.error")
      )
    }

    "return error if rentPeriodAmount contains disallowed characters" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "xyz"
      )
      val boundForm = ProvideDetailsOfSecondRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.invalid.error")
      )
    }

    "return error if rentPeriodAmount exceeds £9,999,999.99" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "10,000,000.00"
      )
      val boundForm = ProvideDetailsOfSecondRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.max.error")
      )
    }

  }
