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
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import play.api.libs.json.Json
import uk.gov.hmrc.ngrraldfrontend.models.NGRDate

import scala.collection.immutable.ArraySeq

class MoneyToTakeOnTheLeaseFormSpec extends AnyWordSpec with Matchers {

  "MoneyToTakeOnTheLeaseForm" should {

    "bind valid input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(MoneyToTakeOnTheLeaseForm(
        amount = BigDecimal(123456.78),
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "fail to bind empty input" in {
      val data = Map.empty[String, String]
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("amount", "error.required"))
    }

    "fail to bind non-numeric input for how much input field" in {
      val data = Map(
        "amount" -> "hello",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("amount", List("moneyToTakeOnTheLease.amount.format.error"), ArraySeq("""^\d+\.?\d{0,}$""")))
    }

    "fail to bind input greater than 9999999.99 for how much input field" in {
      val data = Map(
        "amount" -> "123456789.78",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors shouldBe List(FormError("amount", List("moneyToTakeOnTheLease.amount.tooLarge.error"), ArraySeq(9999999.99)))
    }

    "bind edge case of exactly 9999999.99" in {
      val data = Map(
        "amount" -> "9999999.99",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(MoneyToTakeOnTheLeaseForm(
        amount = BigDecimal("9999999.99"),
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "bind and drop decimal places to make 2" in {
      val data = Map(
        "amount" -> "11.123",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(MoneyToTakeOnTheLeaseForm(
        amount = BigDecimal("11.12"),
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "bind and round the decimal places up" in {
      val data = Map(
        "amount" -> "11.125",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(MoneyToTakeOnTheLeaseForm(
        amount = BigDecimal("11.13"),
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "bind decimal with two .00" in {
      val data = Map(
        "amount" -> "11.00",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(MoneyToTakeOnTheLeaseForm(
        amount = BigDecimal("11.00"),
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "bind amount with commas" in {
      val data = Map(
        "amount" -> "9,999,999.99",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(MoneyToTakeOnTheLeaseForm(
        amount = BigDecimal("9999999.99"),
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "fail to bind incorrect day for day input field" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "99",
        "date.month" -> "13",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.invalid.error"))
    }

    "fail to bind incorrect month for month input field" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "13",
        "date.year" -> "1990"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.invalid.error"))
    }
    "fail to bind year before 1900 for year input field" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1800"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.before.1900.error"))
    }

    "fail to bind missing day input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "",
        "date.month" -> "1",
        "date.year" -> "2000"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.day.required.error"))
    }

    "fail to bind missing month input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "",
        "date.year" -> "2000"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.month.required.error"))
    }

    "fail to bind missing year input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "13",
        "date.year" -> ""
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.year.required.error"))
    }

    "fail to bind missing day and month input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "",
        "date.month" -> "",
        "date.year" -> "2000"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.dayAndMonth.required.error"))
    }

    "fail to bind missing day and year input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "",
        "date.month" -> "1",
        "date.year" -> ""
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.dayAndYear.required.error"))
    }

    "fail to bind missing month and year input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "",
        "date.year" -> ""
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.monthAndYear.required.error"))
    }

    "fail to bind non numeric format for month input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "xyz",
        "date.year" -> "2000"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.invalid.error"))
    }

    "fail to bind non numeric format for year input" in {
      val data = Map(
        "amount" -> "123456.78",
        "date.day" -> "1",
        "date.month" -> "xyz",
        "date.year" -> "test"
      )
      val boundForm = MoneyToTakeOnTheLeaseForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "moneyToTakeOnTheLease.date.invalid.error"))
    }

  }

  "serialize to JSON correctly" in {
    val form = MoneyToTakeOnTheLeaseForm(
      amount =  BigDecimal("9999999.99"),
      date = NGRDate(day = "1", month = "1", year = "1990")
    )
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "amount" -> 9999999.99,
      "date" -> Json.obj(
        "day" -> "1",
        "month" -> "1",
        "year" -> "1990"
      )
    )
  }

  "deserialize from JSON correctly" in {
    val json = Json.obj(
      "amount" -> 9999999.99,
      "date" -> Json.obj(
        "day" -> "1",
        "month" -> "1",
        "year" -> "1990"
      )
    )
    val result = json.validate[MoneyToTakeOnTheLeaseForm]

    result.isSuccess shouldBe true
    result.get shouldBe MoneyToTakeOnTheLeaseForm(
      amount = BigDecimal(9999999.99),
      date = NGRDate(day = "1", month = "1", year = "1990")
    )
  }

  "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[MoneyToTakeOnTheLeaseForm]

    result.isError shouldBe true
  }
}