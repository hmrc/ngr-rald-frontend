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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.data.FormError
import play.api.libs.json.{JsNull, Json}

class LandlordFormSpec extends AnyFlatSpec with Matchers {

  val validData = Map(
    "landlord-name-value" -> "John Doe",
    "landlord-radio" -> "PrivateLandlord",
    "landlord-radio-other" -> ""
  )

  "LandlordForm" should "bind valid data successfully" in {
    val boundForm = LandlordForm.form.bind(validData)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(LandlordForm("John Doe", "PrivateLandlord", None))
  }

  it should "fail when landlord name is missing" in {
    val data = validData - "landlord-name-value"
    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("landlord-name-value", List("error.required"), List()))
  }

  it should "fail when landlord type (radio) is missing" in {
    val data = validData - "landlord-radio"
    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("landlord-radio", List("error.required"), List()))
  }

  it should "fail when 'OtherRelationship' is selected but no description is provided" in {
    val data = Map(
      "landlord-name-value" -> "Jane Doe",
      "landlord-radio" -> "OtherRelationship",
      "landlord-radio-other" -> "   " // whitespace only
    )

    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors should contain(FormError("", "landlord.radio.other.empty.error"))
  }

  it should "pass when 'OtherRelationship' is selected and description is provided" in {
    val data = Map(
      "landlord-name-value" -> "Jane Doe",
      "landlord-radio" -> "OtherRelationship",
      "landlord-radio-other" -> "Friend of the family"
    )

    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(LandlordForm("Jane Doe", "OtherRelationship", Some("Friend of the family")))
  }

  "LandlordForm.unapply" should "extract fields correctly" in {
    val form = LandlordForm("John Doe", "PrivateLandlord", Some("Other info"))
    val result = LandlordForm.unapply(form)

    result shouldBe Some(("John Doe", "PrivateLandlord", Some("Other info")))
  }

  it should "handle None for landlordOther correctly" in {
    val form = LandlordForm("Jane Smith", "CompanyLandlord", None)
    val result = LandlordForm.unapply(form)

    result shouldBe Some(("Jane Smith", "CompanyLandlord", None))
  }

  "LandlordForm.format" should "serialize to JSON correctly" in {
    val form = LandlordForm("John Doe", "PrivateLandlord", Some("Other info"))
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "landlordName" -> "John Doe",
      "landLordType" -> "PrivateLandlord",
      "landlordOther" -> "Other info"
    )
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.obj(
      "landlordName" -> "Jane Smith",
      "landLordType" -> "CompanyLandlord",
      "landlordOther" -> JsNull
    )

    val result = json.validate[LandlordForm]
    result.isSuccess shouldBe true
    result.get shouldBe LandlordForm("Jane Smith", "CompanyLandlord", None)
  }

}

