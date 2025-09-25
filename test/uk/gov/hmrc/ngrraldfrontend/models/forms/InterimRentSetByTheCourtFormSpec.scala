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
import uk.gov.hmrc.ngrraldfrontend.models.NGRMonthYear

import scala.collection.immutable.ArraySeq

class InterimRentSetByTheCourtFormSpec extends AnyWordSpec with Matchers {

  "InterimRentSetByTheCourtForm" should {

    "bind valid input" in {
      val data = Map(
        "interimAmount" -> "123456.78",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(InterimRentSetByTheCourtForm(
        amount = BigDecimal(123456.78),
        date = NGRMonthYear(month = "1", year = "1990")
      ))
    }

    "fail to bind empty input" in {
      val data = Map.empty[String, String]
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("interimAmount", "error.required"))
    }

    "fail to bind non-numeric input for how much input field" in {
      val data = Map(
        "interimAmount" -> "hello",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("interimAmount", List("interimRentSetByTheCourt.interimAmount.format.error"), ArraySeq("""([0-9]+\.[0-9]+|[0-9]+)""")))
    }

    "fail to bind input greater than 9999999.99 for how much input field" in {
      val data = Map(
        "interimAmount" -> "123456789.78",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.errors shouldBe List(FormError("interimAmount", List("interimRentSetByTheCourt.interimAmount.tooLarge.error"), ArraySeq(9999999.99)))
    }

    "bind edge case of exactly 9999999.99" in {
      val data = Map(
        "interimAmount" -> "9999999.99",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(InterimRentSetByTheCourtForm(
        amount = BigDecimal("9999999.99"),
        date = NGRMonthYear(month = "1", year = "1990")
      ))
    }

    "bind and drop decimal places to make 2" in {
      val data = Map(
        "interimAmount" -> "11.123",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(InterimRentSetByTheCourtForm(
        amount = BigDecimal("11.12"),
        date = NGRMonthYear(month = "1", year = "1990")
      ))
    }

    "bind and round the decimal places up" in {
      val data = Map(
        "interimAmount" -> "11.125",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(InterimRentSetByTheCourtForm(
        amount = BigDecimal("11.13"),
        date = NGRMonthYear(month = "1", year = "1990")
      ))
    }

    "bind decimal with two .00" in {
      val data = Map(
        "interimAmount" -> "11.00",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(InterimRentSetByTheCourtForm(
        amount = BigDecimal("11.00"),
        date = NGRMonthYear(month = "1", year = "1990")
      ))
    }

    "bind amount with commas" in {
      val data = Map(
        "interimAmount" -> "9,999,999.99",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(InterimRentSetByTheCourtForm(
        amount = BigDecimal("9999999.99"),
        date = NGRMonthYear(month = "1", year = "1990")
      ))
    }

    "fail to bind incorrect month for month input field" in {
      val data = Map(
        "interimAmount" -> "123456.78",
        "date.month" -> "13",
        "date.year" -> "1990"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "interimRentSetByTheCourt.month.format.error"))
    }
    "fail to bind year before 1900 for year input field" in {
      val data = Map(
        "interimAmount" -> "123456.78",
        "date.month" -> "10",
        "date.year" -> "1899"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "interimRentSetByTheCourt.startDate.before.1900.error"))
    }

    "fail to bind missing month input" in {
      val data = Map(
        "interimAmount" -> "123456.78",
        "date.month" -> "",
        "date.year" -> "1899"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "interimRentSetByTheCourt.month.required.error"))
    }

    "fail to bind missing year input" in {
      val data = Map(
        "interimAmount" -> "123456.78",
        "date.month" -> "1",
        "date.year" -> ""
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "interimRentSetByTheCourt.year.required.error"))
    }

    "fail to bind non numeric format for month input" in {
      val data = Map(
        "interimAmount" -> "123456.78",
        "date.month" -> "hello",
        "date.year" -> "1999"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "interimRentSetByTheCourt.month.format.error"))
    }

    "fail to bind non numeric format for year input" in {
      val data = Map(
        "interimAmount" -> "123456.78",
        "date.month" -> "1",
        "date.year" -> "hello"
      )
      val boundForm = InterimRentSetByTheCourtForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "interimRentSetByTheCourt.year.format.error"))
    }

  }

  "serialize to JSON correctly" in {
    val form = InterimRentSetByTheCourtForm(
      amount =  BigDecimal("9999999.99"),
      date = NGRMonthYear(month = "1", year = "1990")
    )
    val json = Json.toJson(form)

    json shouldBe Json.obj(
        "amount" -> 9999999.99,
        "date" -> Json.obj(
          "month" -> "1",
          "year" -> "1990"
        )
      )
  }

  "deserialize from JSON correctly" in {
    val json = Json.obj(
      "amount" -> 9999999.99,
      "date" -> Json.obj(
        "month" -> "1",
        "year" -> "1990"
      )
    )
    val result = json.validate[InterimRentSetByTheCourtForm]

    result.isSuccess shouldBe true
    result.get shouldBe InterimRentSetByTheCourtForm(
      amount = BigDecimal(9999999.99),
      date = NGRMonthYear(month = "1", year = "1990")
    )
  }

  "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[InterimRentSetByTheCourtForm]

    result.isError shouldBe true
  }
}