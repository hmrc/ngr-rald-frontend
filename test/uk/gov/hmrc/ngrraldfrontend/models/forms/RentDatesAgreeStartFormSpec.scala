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
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.models.NGRDate

class RentDatesAgreeStartFormSpec extends AnyWordSpec with Matchers {
  "RentDatesAgreeStartForm" should {

    "bind successfully with a valid input values" in {
      val data = Map("agreedDate.day" -> "30",
        "agreedDate.month" -> "4",
        "agreedDate.year" -> "2025",
        "startPayingDate.day" -> "1",
        "startPayingDate.month" -> "6",
        "startPayingDate.year" -> "2025"
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentDatesAgreeStartForm(NGRDate("30", "4", "2025"), NGRDate("1", "6", "2025")))
    }

    "fail to bind when agreed and start paying dates are missing" in {
      val data = Map("agreedDate.day" -> "",
        "agreedDate.month" -> "",
        "agreedDate.year" -> "",
        "startPayingDate.day" -> "",
        "startPayingDate.month" -> "",
        "startPayingDate.year" -> "")
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.required.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.required.error")))
    }

    "fail to bind when agreed and start paying dates are missing day" in {
      val data = Map("agreedDate.day" -> "",
        "agreedDate.month" -> "4",
        "agreedDate.year" -> "2025",
        "startPayingDate.day" -> "",
        "startPayingDate.month" -> "6",
        "startPayingDate.year" -> "2025"
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.day.required.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.day.required.error")))
    }

    "fail to bind when agreed and start paying dates are missing month" in {
      val data = Map("agreedDate.day" -> "30",
        "agreedDate.month" -> "",
        "agreedDate.year" -> "2025",
        "startPayingDate.day" -> "1",
        "startPayingDate.month" -> "",
        "startPayingDate.year" -> "2025"
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.month.required.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.month.required.error")))
    }

    "fail to bind when agreed and start paying datea are missing year" in {
      val data = Map("agreedDate.day" -> "30",
        "agreedDate.month" -> "4",
        "agreedDate.year" -> "",
        "startPayingDate.day" -> "1",
        "startPayingDate.month" -> "6",
        "startPayingDate.year" -> ""
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.year.required.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.year.required.error")))
    }

    "fail to bind when agreed and start paying dates are missing day and year" in {
      val data = Map("agreedDate.day" -> "",
        "agreedDate.month" -> "4",
        "agreedDate.year" -> "",
        "startPayingDate.day" -> "",
        "startPayingDate.month" -> "6",
        "startPayingDate.year" -> ""
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.dayAndYear.required.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.dayAndYear.required.error")))
    }

    "fail to bind when agreed and start paying dates are missing month and year" in {
      val data = Map("agreedDate.day" -> "30",
        "agreedDate.month" -> "",
        "agreedDate.year" -> "",
        "startPayingDate.day" -> "1",
        "startPayingDate.month" -> "",
        "startPayingDate.year" -> ""
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.monthAndYear.required.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.monthAndYear.required.error")))
    }

    "fail to bind when agreed and start paying dates are missing day and month" in {
      val data = Map("agreedDate.day" -> "",
        "agreedDate.month" -> "",
        "agreedDate.year" -> "2025",
        "startPayingDate.day" -> "",
        "startPayingDate.month" -> "",
        "startPayingDate.year" -> "2025"
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.dayAndMonth.required.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.dayAndMonth.required.error")))
    }

    "fail to bind when agreed and start paying dates have characters" in {
      val data = Map("agreedDate.day" -> "AS",
        "agreedDate.month" -> "4",
        "agreedDate.year" -> "2025",
        "startPayingDate.day" -> "1",
        "startPayingDate.month" -> "BC",
        "startPayingDate.year" -> "2025"
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.invalid.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.invalid.error")))
    }

    "fail to bind when agreed and start paying dates years are older than 1900" in {
      val data = Map("agreedDate.day" -> "1",
        "agreedDate.month" -> "4",
        "agreedDate.year" -> "250",
        "startPayingDate.day" -> "1",
        "startPayingDate.month" -> "1",
        "startPayingDate.year" -> "1899"
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.before.1900.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.before.1900.error")))
    }

    "fail to bind when agreed and start paying dates are not a valid date" in {
      val data = Map("agreedDate.day" -> "50",
        "agreedDate.month" -> "30",
        "agreedDate.year" -> "2025",
        "startPayingDate.day" -> "31",
        "startPayingDate.month" -> "2",
        "startPayingDate.year" -> "2025"
      )
      val boundForm = RentDatesAgreeStartForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreedDate", List("rentDatesAgreeStart.agreedDate.invalid.error")))
      boundForm.errors should contain(FormError("startPayingDate", List("rentDatesAgreeStart.startPayingDate.invalid.error")))
    }

    "serialize to JSON correctly" in {
      val form = RentDatesAgreeStartForm(NGRDate("30", "4", "2025"), NGRDate("1", "6", "2025"))
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "agreedDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "startPayingDate" -> Json.obj(
          "day" -> "1",
          "month" -> "6",
          "year" -> "2025"
        )
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "agreedDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "startPayingDate" -> Json.obj(
          "day" -> "1",
          "month" -> "6",
          "year" -> "2025"
        )
      )
      val result = json.validate[RentDatesAgreeStartForm]

      result.isSuccess shouldBe true
      result.get shouldBe RentDatesAgreeStartForm(NGRDate("30", "4", "2025"), NGRDate("1", "6", "2025"))
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[RentDatesAgreeStartForm]

      result.isError shouldBe true
    }
  }
}
