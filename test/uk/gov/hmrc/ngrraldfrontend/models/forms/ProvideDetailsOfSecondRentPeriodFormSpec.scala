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
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import play.api.data.{Form, FormError}
import play.api.libs.json.Json
import uk.gov.hmrc.ngrraldfrontend.helpers.{TestData, ViewBaseSpec}
import uk.gov.hmrc.ngrraldfrontend.models.{DetailsOfRentPeriod, NGRDate}
import uk.gov.hmrc.ngrraldfrontend.models.forms.ProvideDetailsOfSecondRentPeriodForm.formToAnswers

import java.time.LocalDate
import scala.collection.immutable.ArraySeq

class ProvideDetailsOfSecondRentPeriodFormSpec extends ViewBaseSpec with TestData {

  private val secondRentPeriodForm: ProvideDetailsOfSecondRentPeriodForm =
    ProvideDetailsOfSecondRentPeriodForm(
      NGRDate("31", "01", "2025"),
      BigDecimal(1999000)
    )

  private val form: Form[ProvideDetailsOfSecondRentPeriodForm] = ProvideDetailsOfSecondRentPeriodForm.form(LocalDate.of(2024, 12, 1), 0)

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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.day.required.error", ArraySeq("second"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.month.required.error", ArraySeq("second"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.year.required.error", ArraySeq("second"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.dayAndMonth.required.error", ArraySeq("second"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.dayAndYear.required.error", ArraySeq("second"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.monthAndYear.required.error", ArraySeq("second"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.invalid.error", ArraySeq("second"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.before.1900.error", ArraySeq("second"))
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
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.required.error", ArraySeq("second", "rentPeriodAmount"))
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
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.invalid.error", ArraySeq("second", "^\\d+\\.?\\d{0,}$"))
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
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.max.error", ArraySeq("second", 9999999.99))
      )
    }
    "serialize to JSON correctly" in {
      val json = Json.toJson(secondRentPeriodForm)

      json shouldBe Json.obj(
        "endDate" -> Json.obj(
          "day" -> "31",
          "month" -> "01",
          "year" -> "2025"
        ),
        "rentPeriodAmount" -> 1999000
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "endDate" -> Json.obj(
          "day" -> "31",
          "month" -> "01",
          "year" -> "2025"
        ),
        "rentPeriodAmount" -> 1999000
      )
      val result = json.validate[ProvideDetailsOfSecondRentPeriodForm]

      result.isSuccess shouldBe true
      result.get shouldBe secondRentPeriodForm
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[ProvideDetailsOfSecondRentPeriodForm]

      result.isError shouldBe true
    }
  }
  "ProvideDetailsOfSecondRentPeriodForm for additional periods" should {
    val form: Form[ProvideDetailsOfSecondRentPeriodForm] = ProvideDetailsOfSecondRentPeriodForm.form(LocalDate.of(2024, 12, 1), 1)
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.day.required.error", ArraySeq("third"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.month.required.error", ArraySeq("third"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.year.required.error", ArraySeq("third"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.dayAndMonth.required.error", ArraySeq("third"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.dayAndYear.required.error", ArraySeq("third"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.monthAndYear.required.error", ArraySeq("third"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.invalid.error", ArraySeq("third"))
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
        FormError("endDate", "provideDetailsOfSecondRentPeriod.endDate.before.1900.error", ArraySeq("third"))
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
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.required.error", ArraySeq("third", "rentPeriodAmount"))
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
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.invalid.error", ArraySeq("third", "^\\d+\\.?\\d{0,}$"))
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
        FormError("rentPeriodAmount", "provideDetailsOfSecondRentPeriod.rentPeriodAmount.max.error", ArraySeq("third", 9999999.99))
      )
    }
  }
  "formToAnswers method" should {
    "update existing rent period details" in {
      val actual = formToAnswers(secondRentPeriodForm, detailsOfRentPeriod, 0)
      actual.size shouldBe detailsOfRentPeriod.size
      actual.contains(DetailsOfRentPeriod("2025-01-31", BigDecimal(1999000))) shouldBe true
    }
    "add a new rent period details" in {
      val actual = formToAnswers(secondRentPeriodForm, detailsOfRentPeriod, detailsOfRentPeriod.size)
      actual.size shouldBe detailsOfRentPeriod.size + 1
      actual.last shouldBe DetailsOfRentPeriod("2025-01-31", BigDecimal(1999000))
    }
  }
}