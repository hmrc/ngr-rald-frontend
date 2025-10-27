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

import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import uk.gov.hmrc.ngrraldfrontend.models.NGRMonthYear

class AboutRepairsAndFittingOutFormSpec extends AnyWordSpec with Matchers {

  "AboutRepairsAndFittingOutForm" should {

    "bind valid input" in {
      val data = Map(
        "cost" -> "123456.78",
        "date.month" -> "10",
        "date.year" -> "2025"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.hasErrors mustBe false
      boundForm.value mustBe Some(
        AboutRepairsAndFittingOutForm(
          cost = BigDecimal("123456.78"),
          date = NGRMonthYear("10", "2025")
        )
      )
    }

    "fail to bind empty input" in {
      val data = Map.empty[String, String]
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.hasErrors mustBe true
      boundForm.errors.map(_.key) must contain allOf ("date.month", "date.year")

    }

    "fail to bind non-numeric cost" in {
      val data = Map(
        "cost" -> "abc",
        "date.month" -> "10",
        "date.year" -> "2025"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.errors.exists(e =>
        e.key == "cost" && e.message == "aboutRepairsAndFittingOut.cost.error.nonNumeric"
      ) mustBe true
    }

    "fail to bind cost over 9999999.99" in {
      val data = Map(
        "cost" -> "10000000.00",
        "date.month" -> "10",
        "date.year" -> "2025"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.errors.exists(e =>
        e.key == "cost" && e.message == "aboutRepairsAndFittingOut.cost.error.exceed"
      ) mustBe true
    }

    "fail to bind invalid month" in {
      val data = Map(
        "cost" -> "1000.00",
        "date.month" -> "13",
        "date.year" -> "2025"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.errors must contain(FormError("date", "aboutRepairsAndFittingOut.date.invalid.error"))
    }

    "fail to bind year before 1900" in {
      val data = Map(
        "cost" -> "1000.00",
        "date.month" -> "10",
        "date.year" -> "1899"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.errors must contain(FormError("date", "aboutRepairsAndFittingOut.date.before.1900.error"))
    }

    "bind and round cost to 2 decimal places" in {
      val data = Map(
        "cost" -> "123.456",
        "date.month" -> "10",
        "date.year" -> "2025"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.value mustBe Some(
        AboutRepairsAndFittingOutForm(
          cost = BigDecimal("123.46"),
          date = NGRMonthYear("10", "2025")
        )
      )
    }

    "fail to bind missing month" in {
      val data = Map(
        "cost" -> "1000.00",
        "date.month" -> "",
        "date.year" -> "2025"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.errors must contain(FormError("date", "aboutRepairsAndFittingOut.date.month.required.error"))
    }

    "fail to bind missing year" in {
      val data = Map(
        "cost" -> "1000.00",
        "date.month" -> "10",
        "date.year" -> ""
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.errors must contain(FormError("date", "aboutRepairsAndFittingOut.date.year.required.error"))
    }

    "fail to bind missing cost" in {
      val data = Map(
        "cost" -> "",
        "date.month" -> "10",
        "date.year" -> "2025"
      )
      val boundForm = AboutRepairsAndFittingOutForm.form.bind(data)

      boundForm.errors must contain(
        FormError("cost", List("aboutRepairsAndFittingOut.cost.error.missing"), List("cost"))
      )
    }

  }
}