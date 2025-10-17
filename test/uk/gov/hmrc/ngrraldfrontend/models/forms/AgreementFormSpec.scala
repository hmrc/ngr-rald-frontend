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

class AgreementFormSpec extends AnyWordSpec with Matchers {
  "AgreementVerbalForm" should {

    "bind successfully with a valid start date input, open ended as yes and break clause as no" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "true",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)
      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreementForm(NGRDate("30", "4", "2025"), "true", None, "false", None))
    }

    "bind successfully with a valid start date input, open ended as no, with end date and break clause as no" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "30",
        "agreementEndDate.month" -> "4",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)
      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreementForm(NGRDate("30", "4", "2025"), "false", Some(NGRDate("30", "4", "2025")), "false", None))
    }

    "bind successfully with a valid start date input, open ended as no, with end date and break clause as yes" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "30",
        "agreementEndDate.month" -> "4",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "true",
        "about-break-clause" -> "I have a break clause for..."
      )
      val boundForm = AgreementForm.form.bind(data)
      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(AgreementForm(NGRDate("30", "4", "2025"), "false", Some(NGRDate("30", "4", "2025")), "true", Some("I have a break clause for...")))
    }

    "fail to bind when open ended radio is unselected" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreement-radio-openEnded", List("agreement.radio.openEnded.required.error")))
    }

    "fail to bind when break clause radio is unselected" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "true",
        "agreement-breakClause-radio" -> ""
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreement-breakClause-radio", List("agreement.radio.breakClause.required.error")))
    }

    "fail to bind when no reason is given for break clause after selecting yes to having a break clause" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "true",
        "agreement-breakClause-radio" -> "true",
        "about-break-clause" -> ""
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("about-break-clause", List("agreement.radio.conditional.breakClause.required.error")))
    }

    "fail to bind when reason is given for break clause but is too long after selecting yes to having a break clause" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "true",
        "agreement-breakClause-radio" -> "true",
        "about-break-clause" -> "over 250 characters, over 250 characters, over 250 characters, over 250 characters,over 250 characters, over 250 characters, over 250 characters, over 250 charactersover 250 characters, over 250 characters, over 250 characters, over 250 charactersover 250 characters, over 250 characters, over 250 characters, over 250 charactersover 250 characters, over 250 characters, over 250 characters, over 250 charactersover 250 characters, over 250 characters, over 250 characters, over 250 charactersover 250 characters, over 250 characters, over 250 characters, over 250 charactersover 250 characters, over 250 characters, over 250 characters, over 250 charactersover 250 characters, over 250 characters, over 250 characters, over 250 characters"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("about-break-clause", List("agreement.radio.conditional.breakClause.tooLong.error")))
    }

    "fail to bind when no date is given after selecting No to open ended" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "",
        "agreementEndDate.month" -> "",
        "agreementEndDate.year" -> "",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementEndDate", List("agreement.agreementEndDate.required.error")))
    }

    "fail to bind when no year and month is given after selecting No to open ended" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "",
        "agreementEndDate.year" -> "",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementEndDate", List("agreement.agreementEndDate.monthAndYear.required.error")))
    }

    "fail to bind when no day and year is given after selecting No to open ended" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementEndDate", List("agreement.agreementEndDate.dayAndYear.required.error")))
    }

    "fail to bind when no day and month is given after selecting No to open ended" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "",
        "agreementEndDate.month" -> "",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementEndDate", List("agreement.agreementEndDate.dayAndMonth.required.error")))
    }

    "fail to bind when no day is given after selecting No to open ended" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementEndDate", List("agreement.agreementEndDate.day.required.error")))
    }

    "fail to bind when no month is given after selecting No to open ended" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementEndDate", List("agreement.agreementEndDate.month.required.error")))
    }

    "fail to bind when no year is given after selecting No to open ended" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementEndDate", List("agreement.agreementEndDate.year.required.error")))
    }


    "fail to bind when no date is given for start date" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreement.agreementStartDate.required.error")))
    }

    "fail to bind when no year and month is given for start date" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreement.agreementStartDate.monthAndYear.required.error")))
    }

    "fail to bind when no day and year is given for start date" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreement.agreementStartDate.dayAndYear.required.error")))
    }

    "fail to bind when no day and month is given for start date" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreement.agreementStartDate.dayAndMonth.required.error")))
    }

    "fail to bind when no day is given for start date" in {
      val data = Map("agreementStartDate.day" -> "",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreement.agreementStartDate.day.required.error")))
    }

    "fail to bind when no month is given for start date" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "",
        "agreementStartDate.year" -> "2025",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreement.agreementStartDate.month.required.error")))
    }

    "fail to bind when no year is given for start date" in {
      val data = Map("agreementStartDate.day" -> "30",
        "agreementStartDate.month" -> "4",
        "agreementStartDate.year" -> "",
        "agreement-radio-openEnded" -> "false",
        "agreementEndDate.day" -> "12",
        "agreementEndDate.month" -> "12",
        "agreementEndDate.year" -> "2025",
        "agreement-breakClause-radio" -> "false"
      )
      val boundForm = AgreementForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("agreementStartDate", List("agreement.agreementStartDate.year.required.error")))
    }

    "serialize to JSON correctly" in {
      val form = AgreementForm(NGRDate("30", "4", "2025"), "false", Some(NGRDate("30", "4", "2025")), "true", Some("I have a break clause for..."))
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "agreementStart" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "openEndedRadio" -> "false",
        "openEndedDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "breakClauseRadio" -> "true",
        "breakClauseInfo" -> "I have a break clause for..."
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "agreementStart" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "openEndedRadio" -> "false",
        "openEndedDate" -> Json.obj(
          "day" -> "30",
          "month" -> "4",
          "year" -> "2025"
        ),
        "breakClauseRadio" -> "true",
        "breakClauseInfo" -> "I have a break clause for..."
      )

      val result = json.validate[AgreementForm]

      result.isSuccess shouldBe true
      result.get shouldBe AgreementForm(NGRDate("30", "4", "2025"), "false", Some(NGRDate("30", "4", "2025")), "true", Some("I have a break clause for..."))
    }

    "fail deserialization if value is missing" in {
      val json = Json.obj()
      val result = json.validate[AgreementForm]

      result.isError shouldBe true
    }
  }
}
