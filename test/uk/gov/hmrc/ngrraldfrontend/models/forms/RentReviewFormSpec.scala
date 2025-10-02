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
import uk.gov.hmrc.ngrraldfrontend.models.NGRMonthYear

class RentReviewFormSpec extends AnyWordSpec with Matchers {
  val rentReviewForm: RentReviewForm = RentReviewForm("true", Some(NGRMonthYear("11", "1")), "false")
  val rentReviewJson: JsValue = Json.parse(
    """{
      | "hasIncludeRentReview":"true",
      | "monthsYears": {
      |   "month": "11",
      |   "year": "1"
      | },
      | "canRentGoDown": "false"
      |}""".stripMargin)

  "RentReviewForm" should {
    "serialize into json" in {
      Json.toJson(rentReviewForm) shouldBe rentReviewJson
    }
    "deserialize from json" in {
      rentReviewJson.as[RentReviewForm] shouldBe rentReviewForm
    }
    "bind successfully without review months and years" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "false",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewForm("false", None, "false"))
    }
    "bind successfully with review months and years" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "11",
        "date.year" -> "1",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewForm("true", Some(NGRMonthYear("11", "1")), "false"))
    }
    "bind successfully with only review months" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "12",
        "date.year" -> "",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewForm("true", Some(NGRMonthYear("12", "")), "false"))
    }
    "bind successfully with review months and years is zero" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "12",
        "date.year" -> "0",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewForm("true", Some(NGRMonthYear("12", "0")), "false"))
    }
    "bind successfully with only review years" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "",
        "date.year" -> "2",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewForm("true", Some(NGRMonthYear("", "2")), "false"))
    }
    "bind successfully with invalid review years and months when included rent review is false" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "false",
        "date.month" -> "AS",
        "date.year" -> "200000",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewForm("false", Some(NGRMonthYear("AS", "200000")), "false"))
    }
    "bind successfully with review years and months is zero" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "0",
        "date.year" -> "2",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentReviewForm("true", Some(NGRMonthYear("0", "2")), "false"))
    }
    "Return errors when radios are unselected" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "",
        RentReviewForm.canRentGoDownRadio -> "")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(RentReviewForm.hasIncludeRentReviewRadio, "rentReview.hasIncludeRentReview.radio.empty.error"))
      boundForm.errors should contain(FormError(RentReviewForm.canRentGoDownRadio, "rentReview.canRentGoDown.radio.empty.error"))
    }
    "Return errors when months and years are empty" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", "rentReview.date.required.error"))
    }
    "Return errors when months and years are not numeric" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "AS",
        "date.year" -> "-2",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", "rentReview.rentReviewMonthsYears.months.invalid.error"))
      boundForm.errors should contain(FormError("", "rentReview.rentReviewMonthsYears.years.invalid.error"))
    }
    "Return errors when months is over 12 and years is empty" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "20",
        "date.year" -> "",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", "rentReview.rentReviewMonthsYears.months.maximum.12.error"))
    }
    "Return errors when months is over 12 and years is zero" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "209876234563433434343234",
        "date.year" -> "0",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", "rentReview.rentReviewMonthsYears.months.maximum.12.error"))
    }
    "Return errors when months is over 11 and years is greater than zero" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "12",
        "date.year" -> "2",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", "rentReview.rentReviewMonthsYears.months.maximum.11.error"))
    }
    "Return errors when years is greater than 1000" in {
      val data = Map(RentReviewForm.hasIncludeRentReviewRadio -> "true",
        "date.month" -> "11",
        "date.year" -> "23456834834839292244",
        RentReviewForm.canRentGoDownRadio -> "false")
      val boundForm = RentReviewForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", "rentReview.rentReviewMonthsYears.years.maximum.1000.error"))
    }
    "unbind correctly to a data map" in {
      val form = DoYouPayExtraForParkingSpacesForm.form.fill(DoYouPayExtraForParkingSpacesForm("no"))
      form.data shouldBe Map(DoYouPayExtraForParkingSpacesForm.payExtraRadio -> "no")
    }
  }

}
