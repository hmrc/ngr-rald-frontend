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

import scala.collection.immutable.ArraySeq

class RentReviewDetailsFormSpec extends AnyWordSpec with Matchers {
  val rentReviewDetailsForm: RentReviewDetailsForm = RentReviewDetailsForm(BigDecimal("3000"), "GoUpOrDown", NGRDate("30", "10", "2020"), "false", Some("Arbitrator"))
  val rentReviewDetailsJson: JsValue = Json.parse(
    """{
      | "annualRentAmount": 3000,
      | "whatHappensAtRentReview": "GoUpOrDown",
      | "startDate": {
      |   "day": "30",
      |   "month": "10",
      |   "year": "2020"
      | },
      | "hasAgreedNewRent": "false",
      | "whoAgreed": "Arbitrator"
      |}""".stripMargin)

  "RentReviewDetailsForm" should {
    "serialize into json" in {
      Json.toJson(rentReviewDetailsForm) shouldBe rentReviewDetailsJson
    }
    "deserialize from json" in {
      rentReviewDetailsJson.as[RentReviewDetailsForm] shouldBe rentReviewDetailsForm
    }
    "bind successfully without who agreed new rent" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true")
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewDetailsForm(BigDecimal("3000"), "OnlyGoUp", NGRDate("30", "10", "2020"), "true", None))
    }
    "bind successfully with who agreed new rent" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "false",
        RentReviewDetailsForm.whoAgreedRadio -> "Arbitrator"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewDetailsForm(BigDecimal("3000"), "OnlyGoUp", NGRDate("30", "10", "2020"), "false", Some("Arbitrator")))
    }
    "Return errors when radios are unselected" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> ""
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(RentReviewDetailsForm.whatHappensAtRentReviewRadio, "rentReviewDetails.whatHappensAtRentReview.required.error"))
      boundForm.errors should contain(FormError(RentReviewDetailsForm.hasAgreedNewRentRadio, "rentReviewDetails.hasAgreedNewRent.required.error"))
    }
    "Return errors when who agreed the new rent radio is unselected" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "false",
        RentReviewDetailsForm.whoAgreedRadio -> ""
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(RentReviewDetailsForm.whoAgreedRadio, "rentReviewDetails.whoAgreed.required.error"))
    }
    "Return errors when annual rent amount is not entered" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(RentReviewDetailsForm.annualAmount, "rentReviewDetails.annualAmount.required.error", ArraySeq("annualAmount")))
    }
    "Return errors when annual rent amount has special characters" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "AS&%$",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(RentReviewDetailsForm.annualAmount, "rentReviewDetails.annualAmount.invalid.error", ArraySeq("([0-9]+\\.[0-9]+|[0-9]+)")))
    }
    "Return errors when annual rent amount is too big" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "234500000000000000000000000000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(RentReviewDetailsForm.annualAmount, "rentReviewDetails.annualAmount.maximum.error", ArraySeq(9999999.99)))
    }
    "Return errors when start date is missing" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "",
        "startDate.month" -> "",
        "startDate.year" -> "",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.required.error"))
    }
    "Return errors when day is missing" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.day.required.error"))
    }
    "Return errors when month is missing" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.month.required.error"))
    }
    "Return errors when year is missing" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.year.required.error"))
    }
    "Return errors when day and month are missing" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "",
        "startDate.month" -> "",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.dayAndMonth.required.error"))
    }
    "Return errors when day and year are missing" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "",
        "startDate.month" -> "10",
        "startDate.year" -> "",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.dayAndYear.required.error"))
    }
    "Return errors when month and year are missing" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "",
        "startDate.year" -> "",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.monthAndYear.required.error"))
    }
    "Return errors when start date is invalid" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "2",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.invalid.error"))
    }
    "Return errors when start date contains characters" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "AS",
        "startDate.month" -> "2",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.invalid.error"))
    }
    "Return errors when start date is before 1900" in {
      val data = Map(RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "OnlyGoUp",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "1800",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "true"
      )
      val boundForm = RentReviewDetailsForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("startDate", "rentReviewDetails.startDate.before.1900.error"))
    }
    "unbind correctly to a data map" in {
      val form = RentReviewDetailsForm.form.fill(RentReviewDetailsForm(BigDecimal("3000"), "GoUpOrDown", NGRDate("30", "10", "2020"), "false", Some("Arbitrator")))
      form.data shouldBe Map(
        RentReviewDetailsForm.annualAmount -> "3000",
        RentReviewDetailsForm.whatHappensAtRentReviewRadio -> "GoUpOrDown",
        "startDate.day" -> "30",
        "startDate.month" -> "10",
        "startDate.year" -> "2020",
        RentReviewDetailsForm.hasAgreedNewRentRadio -> "false",
        RentReviewDetailsForm.whoAgreedRadio -> "Arbitrator"
      )
    }
  }

}
