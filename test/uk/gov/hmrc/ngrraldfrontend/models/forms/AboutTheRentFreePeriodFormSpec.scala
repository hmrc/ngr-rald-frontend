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
import uk.gov.hmrc.ngrraldfrontend.models.{NGRDate, NGRMonthYear}

import scala.collection.immutable.ArraySeq

class AboutTheRentFreePeriodFormSpec extends AnyWordSpec with Matchers {

  "AboutTheRentFreePeriodForm" should {

    "bind valid input" in {
      val data = Map(
        "howManyMonths" -> "1",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AboutTheRentFreePeriodForm(
        howManyMonths = 1,
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "fail to bind empty input" in {
      val data = Map.empty[String, String]
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("howManyMonths", "error.required"))
    }

    "fail to bind non-numeric input for how much input field" in {
      val data = Map(
        "howManyMonths" -> "hello",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("howManyMonths", List("aboutTheRentFreePeriod.months.invalid.error"), ArraySeq("""^\d+$""")))
    }

    "fail to bind input greater than 100 for how much input field" in {
      val data = Map(
        "howManyMonths" -> "100",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.errors shouldBe List(FormError("howManyMonths", List("aboutTheRentFreePeriod.months.maximum.error"), ArraySeq(99)))
    }

    "bind edge case of exactly 99" in {
      val data = Map(
        "howManyMonths" -> "99",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "1990"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AboutTheRentFreePeriodForm(
        howManyMonths = 99,
        date = NGRDate(day = "1", month = "1", year = "1990")
      ))
    }

    "fail to bind incorrect month for month input field" in {
      val data = Map(
        "howManyMonths" -> "1",
        "date.day" -> "1",
        "date.month" -> "13",
        "date.year" -> "1990"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "aboutTheRentFreePeriod.date.invalid.error"))
    }

    "fail to bind year before 1900 for year input field" in {
      val data = Map(
        "howManyMonths" -> "1",
        "date.day" -> "1",
        "date.month" -> "10",
        "date.year" -> "1899"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "aboutTheRentFreePeriod.date.before.1900.error"))
    }

    "fail to bind missing month input" in {
      val data = Map(
        "howManyMonths" -> "1",
        "date.day" -> "1",
        "date.month" -> "",
        "date.year" -> "1899"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "aboutTheRentFreePeriod.date.month.required.error"))
    }

    "fail to bind missing year input" in {
      val data = Map(
        "howManyMonths" -> "1",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> ""
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "aboutTheRentFreePeriod.date.year.required.error"))
    }

    "fail to bind non numeric format for month input" in {
      val data = Map(
        "howManyMonths" -> "1",
        "date.day" -> "1",
        "date.month" -> "hello",
        "date.year" -> "1999"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "aboutTheRentFreePeriod.date.invalid.error"))
    }

    "fail to bind non numeric format for year input" in {
      val data = Map(
        "howManyMonths" -> "1",
        "date.day" -> "1",
        "date.month" -> "1",
        "date.year" -> "hello"
      )
      val boundForm = AboutTheRentFreePeriodForm.form.bind(data)

      boundForm.errors should contain(FormError("date", "aboutTheRentFreePeriod.date.invalid.error"))
    }

  }

  "serialize to JSON correctly" in {
    val form = AboutTheRentFreePeriodForm(
      howManyMonths =  1,
      date = NGRDate(day = "1", month = "1", year = "1990")
    )
    val json = Json.toJson(form)

    json shouldBe Json.obj(
        "howManyMonths" -> 1,
        "date" -> Json.obj(
          "day" -> "1",
          "month" -> "1",
          "year" -> "1990"
        )
      )
  }

  "deserialize from JSON correctly" in {
    val json = Json.obj(
      "howManyMonths" -> 1,
      "date" -> Json.obj(
        "day" -> "1",
        "month" -> "1",
        "year" -> "1990"
      )
    )
    val result = json.validate[AboutTheRentFreePeriodForm]

    result.isSuccess shouldBe true
    result.get shouldBe AboutTheRentFreePeriodForm(
      howManyMonths = 1,
      date = NGRDate(day = "1", month = "1", year = "1990")
    )
  }

  "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[AboutTheRentFreePeriodForm]

    result.isError shouldBe true
  }
}