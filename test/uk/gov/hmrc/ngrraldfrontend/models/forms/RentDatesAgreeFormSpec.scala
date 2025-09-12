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
import uk.gov.hmrc.ngrraldfrontend.models.NGRDate

class RentDatesAgreeFormSpec extends AnyWordSpec with Matchers {

  val fieldDay = "date.day"
  val fieldMonth = "date.month"
  val fieldYear = "date.year"
  val requiredDayError = "rentDatesAgree.date.day.required.error"
  val requiredMonthError = "rentDatesAgree.date.month.required.error"
  val requiredYearError = "rentDatesAgree.date.year.required.error"

  "RentDatesAgreeForm" should {
    "bind successfully with a valid date values" in {
      val data = Map(
        fieldDay -> "12",
        fieldMonth -> "12",
        fieldYear -> "2026",
      )
      val boundForm = RentDatesAgreeForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(RentDatesAgreeForm(NGRDate(day = "12", month = "12", year = "2026")))
    }
    "return an error when day value is missing" in {
      val data = Map(
        fieldDay -> "",
        fieldMonth -> "12",
        fieldYear -> "2026",
      )
      val boundForm = RentDatesAgreeForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("date", requiredDayError))
    }
    "return an error when month value is missing" in {
      val data = Map(
        fieldDay -> "12",
        fieldMonth -> "",
        fieldYear -> "2026",
      )
      val boundForm = RentDatesAgreeForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("date", requiredMonthError))
    }
    "return an error when year value is missing" in {
      val data = Map(
        fieldDay -> "12",
        fieldMonth -> "12",
        fieldYear -> "",
      )
      val boundForm = RentDatesAgreeForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("date", requiredYearError))
    }


    "fail when date fields are empty" in {
      val data = Map(
        fieldDay -> "",
        fieldMonth -> "",
        fieldYear -> ""
      )

      val boundForm = RentDatesAgreeForm.form.bind(data)
      boundForm.errors.map(_.message) should contain("rentDatesAgree.date.required.error")
    }


    "fail when date is invalid" in {
      val data = Map(
        fieldDay -> "31",
        fieldMonth -> "2",
        fieldYear -> "2025"
      )

      val boundForm = RentDatesAgreeForm.form.bind(data)
      boundForm.errors.map(_.message) should contain("rentDatesAgree.date.invalid.error")
    }


    "unbind correctly to a data map" in {
      val form = RentDatesAgreeForm.form.fill(RentDatesAgreeForm(NGRDate(day = "12", month = "12", year = "2026")))
      form.data shouldBe Map(
        fieldDay -> "12",
        fieldMonth -> "12",
        fieldYear -> "2026"
      )
    }
  }
}
