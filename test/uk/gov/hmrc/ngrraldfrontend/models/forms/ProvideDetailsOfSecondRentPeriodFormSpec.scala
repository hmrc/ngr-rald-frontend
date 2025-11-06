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
import play.api.data.{Form, FormError}
import uk.gov.hmrc.ngrraldfrontend.models.NGRDate

import java.time.LocalDate

class ProvideDetailsOfSecondRentPeriodFormSpec extends AnyWordSpec with Matchers:

  private val secondRentPeriodForm: ProvideDetailsOfSecondRentPeriodForm =
    ProvideDetailsOfSecondRentPeriodForm(
      NGRDate("31", "01", "2025"),
      BigDecimal(1999000)
    )

  private val form: Form[ProvideDetailsOfSecondRentPeriodForm] = ProvideDetailsOfSecondRentPeriodForm.form(LocalDate.of(2024, 12, 1))

  "ProvideDetailsOfSecondRentPeriodForm" should {
    "bind successfully with all the data provided" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfSecondRentPeriodForm(
        NGRDate("31", "1", "2025"),
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
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(ProvideDetailsOfSecondRentPeriodForm(
        NGRDate("31", "1", "2025"),
        BigDecimal(1777000.45)
      ))
    }

    "unbind correctly to a data Map" in {
      val currentForm = form.fill(secondRentPeriodForm)
      currentForm.data shouldBe Map(
        "endDate.day" -> "31",
        "endDate.month" -> "01",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1999000"
      )
    }

    "return error if day is missing" in {
      val data = Map(
        "endDate.day" -> "",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.day.required.error")
      )
    }

    "return error if month is missing" in {
      val data = Map(
        "endDate.day" -> "1",
        "endDate.month" -> "",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.month.required.error")
      )
    }

    "return error if year is missing" in {
      val data = Map(
        "endDate.day" -> "1",
        "endDate.month" -> "1",
        "endDate.year" -> "",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.year.required.error")
      )
    }

    "return error if day and month is missing" in {
      val data = Map(
        "endDate.day" -> "",
        "endDate.month" -> "",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.dayAndMonth.required.error")
      )
    }

    "return error if day and year is missing" in {
      val data = Map(
        "endDate.day" -> "",
        "endDate.month" -> "1",
        "endDate.year" -> "",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.dayAndYear.required.error")
      )
    }

    "return error if month and year is missing" in {
      val data = Map(
        "endDate.day" -> "1",
        "endDate.month" -> "",
        "endDate.year" -> "",
        "rentPeriodAmount" -> "1,777,000.99"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.monthAndYear.required.error")
      )
    }

    "return error if endDate is invalid" in {
      val data = Map(
        "endDate.day" -> "33",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> "777,000.00"
      )
      val boundForm = form.bind(data)

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
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.before.1900.error")
      )
    }

    "return error if endDate is before start date" in {
      val data = Map(
        "endDate.day" -> "30",
        "endDate.month" -> "11",
        "endDate.year" -> "2024",
        "rentPeriodAmount" -> "777,000.00"
      )
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.before.startDate.error")
      )
    }

    "return error if rentPeriodAmount is empty" in {
      val data = Map(
        "endDate.day" -> "31",
        "endDate.month" -> "1",
        "endDate.year" -> "2025",
        "rentPeriodAmount" -> ""
      )
      val boundForm = form.bind(data)

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
      val boundForm = form.bind(data)

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
      val boundForm = form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors.size shouldBe 1
      boundForm.errors should contain(
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.max.error")
      )
    }
  }
