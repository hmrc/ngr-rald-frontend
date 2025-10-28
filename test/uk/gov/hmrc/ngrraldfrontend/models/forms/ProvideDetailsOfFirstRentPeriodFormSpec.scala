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
import uk.gov.hmrc.ngrraldfrontend.models.ProvideDetailsOfFirstRentPeriod

import java.time.LocalDate

class ProvideDetailsOfFirstRentPeriodFormSpec extends AnyWordSpec with Matchers:

  val firstRentPeriod: ProvideDetailsOfFirstRentPeriod =
    ProvideDetailsOfFirstRentPeriod(
      LocalDate.parse("2025-01-01"),
      LocalDate.parse("2025-01-31"),
      true,
      Some(BigDecimal(1999000))
    )

  "ProvideDetailsOfFirstRentPeriodForm" should {
    "bind successfully with all the data provided" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfFirstRentPeriod(
        LocalDate.parse("2025-01-01"),
        LocalDate.parse("2025-01-31"),
        true,
        Some(BigDecimal(1777000.99))
      ))
    }

    "bind successfully ensuring rentPeriodAmount is rounded to 2 decimal places" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "1,777,000.449"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfFirstRentPeriod(
        LocalDate.parse("2025-01-01"),
        LocalDate.parse("2025-01-31"),
        true,
        Some(BigDecimal(1777000.45))
      ))
    }

    "bind successfully rent-free period" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "false"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfFirstRentPeriod(
        LocalDate.parse("2025-01-01"),
        LocalDate.parse("2025-01-31"),
        false,
        None
      ))
    }

    "unbind correctly to a data Map" in {
      val form = ProvideDetailsOfFirstRentPeriodForm.form.fill(firstRentPeriod)

      form.data shouldBe Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "1999000"
      )
    }

    "return errors for an empty form" in {
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(Map.empty)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 3
      boundForm.errors should contain(FormError("startDate", "provideDetailsOfFirstRentPeriod.startDate.required.error"))
      boundForm.errors should contain(FormError("endDate", "provideDetailsOfFirstRentPeriod.endDate.required.error"))
      boundForm.errors should contain(
        FormError("provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod", "provideDetailsOfFirstRentPeriod.firstPeriod.radio.error.required")
      )
    }

    "return error if startDate is before 1900" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "1899",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "1900",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "£777,000"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("startDate", "provideDetailsOfFirstRentPeriod.startDate.before.1900.error")
      )
    }

    "return error if endDate is invalid" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "33",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "777,000.00"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfFirstRentPeriod.endDate.invalid.error")
      )
    }

    "return errors if startDate is invalid and endDate is before 1900" in {
      val data = Map(
        "startDate.day" -> "33",
        "startDate.month" -> "13",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "1899",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "777000"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 2
      boundForm.errors should contain(
        FormError("startDate", "provideDetailsOfFirstRentPeriod.startDate.invalid.error")
      )
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfFirstRentPeriod.endDate.before.1900.error")
      )
    }

    "return error if endDate is before startDate" in {
      val data = Map(
        "startDate.day" -> "2",
        "startDate.month" -> "10",
        "startDate.year" -> "2025",
        "endDate.day" -> "1",
        "endDate.month" -> "10",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "false"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfFirstRentPeriod.endDate.before.startDate.error")
      )
    }

    "return error if rentPeriodAmount is empty" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> ""
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfFirstRentPeriod.firstPeriod.amount.required.error")
      )
    }

    "return error if rentPeriodAmount contains disallowed characters" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "200bucks"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfFirstRentPeriod.firstPeriod.amount.invalid.error")
      )
    }

    "return error if rentPeriodAmount exceeds £9,999,999.99" in {
      val data = Map(
        "startDate.day" -> "1",
        "startDate.month" -> "1",
        "startDate.year" -> "2025",
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "provideDetailsOfFirstRentPeriod-radio-isRentPayablePeriod" -> "true",
        "rentPeriodAmount" -> "10,000,000.00"
      )
      val boundForm = ProvideDetailsOfFirstRentPeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfFirstRentPeriod.firstPeriod.amount.max.error")
      )
    }

  }
