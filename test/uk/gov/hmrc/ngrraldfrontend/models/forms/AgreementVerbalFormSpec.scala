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

class AgreementVerbalFormSpec extends AnyWordSpec with Matchers {
  "AgreementVerbalForm" should {

    "bind successfully with a valid input values" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "Yes"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreementVerbalForm("Yes", NGRDate("30", "4", "2025"), None))
    }

    "bind successfully with a radio value 'No' and agreement end date" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "30",
        "agreementEndDate.month" -> "4",
        "agreementEndDate.year" -> "2027")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreementVerbalForm("No", NGRDate("30", "4", "2025"), Some(NGRDate("30", "4", "2027"))))
    }

    "bind successfully with a radio value 'Yes' and invalid agreement end date" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "Yes",
        "agreementEndDate.day" -> "AS",
        "agreementEndDate.month" -> "4",
        "agreementEndDate.year" -> "2027")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreementVerbalForm("Yes", NGRDate("30", "4", "2025"), Some(NGRDate("AS", "4", "2027"))))
    }

    "fail to bind when radio is unselected" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreement-verbal-radio", List("agreementVerbal.radio.unselected.error")))
    }

    "fail to bind when agreement start date is missing" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "",
        "agreement-verbal-radio" -> "Yes")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.required.error")))
    }

    "fail to bind when agreement start dates are missing day" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "Yes"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.day.required.error")))
    }

    "fail to bind when agreement start date is missing month" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "Yes")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.month.required.error")))
    }

    "fail to bind when agreement start date is missing year" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "",
        "agreement-verbal-radio" -> "Yes")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.year.required.error")))
    }

    "fail to bind when agreement start date is missing day and year" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "",
        "agreement-verbal-radio" -> "Yes")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.dayAndYear.required.error")))
    }

    "fail to bind when agreement start date is missing month and year" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "",
        "agreement-verbal-radio" -> "Yes")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.monthAndYear.required.error")))
    }

    "fail to bind when agreement start date is missing day and month" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "Yes")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.dayAndMonth.required.error")))
    }

    "fail to bind when agreement start date has characters" in {
      val data = Map("agreementStartDate.day" -> "AS",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "Yes"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.invalid.error")))
    }

    "fail to bind when agreement start dates is not a valid date" in {
      val data = Map("agreementStartDate.day" -> "50",
        "agreementStartDate.month" -> "30",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "Yes"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreementVerbal.startDate.invalid.error")))
    }

    "fail to bind when agreement end date is missing day" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "",
        "agreementEndDate.month" -> "4",
        "agreementEndDate.year" -> "2027"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.day.required.error")))
    }

    "fail to bind when agreement end date is missing month" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "30",
        "agreementEndDate.month" -> "",
        "agreementEndDate.year" -> "2027"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.month.required.error")))
    }

    "fail to bind when agreement end date is missing year" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "30",
        "agreementEndDate.month" -> "4",
        "agreementEndDate.year" -> ""
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.year.required.error")))
    }

    "fail to bind when agreement end date is missing day and year" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "",
        "agreementEndDate.month" -> "4",
        "agreementEndDate.year" -> ""
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.dayAndYear.required.error")))
    }

    "fail to bind when agreement end date is missing month and year" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "30",
        "agreementEndDate.month" -> "",
        "agreementEndDate.year" -> "")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.monthAndYear.required.error")))
    }

    "fail to bind when agreement end date is missing day and month" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "",
        "agreementEndDate.month" -> "",
        "agreementEndDate.year" -> "2027")
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.dayAndMonth.required.error")))
    }

    "fail to bind when agreement end date has characters" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "AS",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2027"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.invalid.error")))
    }

    "fail to bind when agreement end dates is not a valid date" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "12",
        "agreementStartDate.year" -> "2025",
        "agreement-verbal-radio" -> "No",
        "agreementEndDate.day" -> "50",
        "agreementEndDate.month" -> "30",
        "agreementEndDate.year" -> "2027"
      )
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("", List("agreementVerbal.endDate.invalid.error")))
    }

    "fail to bind when input is missing" in {
      val data = Map.empty[String, String]
      val boundForm = AgreementVerbalForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreement-verbal-radio", List("agreementVerbal.radio.unselected.error")))
      boundForm.errors should contain(FormError("agreementStartDate.day", List("error.required")))
      boundForm.errors should contain(FormError("agreementStartDate.month", List("error.required")))
      boundForm.errors should contain(FormError("agreementStartDate.year", List("error.required")))
    }

    "serialize to JSON correctly" in {
      val form = AgreementVerbalForm("No", NGRDate("30", "4", "2025"), Some(NGRDate("30", "4", "2027")))
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "radioValue" -> "No",
        "agreementStartDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "agreementEndDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2027"
        )
      )
    }

    "serialize to JSON correctly without end date" in {
      val form = AgreementVerbalForm("Yes", NGRDate("30", "4", "2025"), None)
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "radioValue" -> "Yes",
        "agreementStartDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        )
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "radioValue" -> "No",
        "agreementStartDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "agreementEndDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2027"
        )
      )
      val result = json.validate[AgreementVerbalForm]

      result.isSuccess shouldBe true
      result.get shouldBe AgreementVerbalForm("No", NGRDate("30", "4", "2025"), Some(NGRDate("30", "4", "2027")))
    }

    "deserialize from JSON correctly without end date" in {
      val json = Json.obj(
        "radioValue" -> "Yes",
        "agreementStartDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        )
      )
      val result = json.validate[AgreementVerbalForm]

      result.isSuccess shouldBe true
      result.get shouldBe AgreementVerbalForm("Yes", NGRDate("30", "4", "2025"), None)
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[AgreementVerbalForm]

      result.isError shouldBe true
    }
  }
}
