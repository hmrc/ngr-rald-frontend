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

import scala.collection.immutable.ArraySeq

class LandlordFormSpec extends AnyFlatSpec with Matchers {

  val validData = Map(
    "landlord-name-value" -> "John Doe",
    "landlord-radio" -> "false"
  )

  "LandlordForm" should "bind valid data successfully" in {
    val boundForm = LandlordForm.form.bind(validData)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(LandlordForm("John Doe", "false", None))
  }

  it should "fail when landlord name and relationship are missing" in {
    val data = Map(
      "landlord-name-value" -> "",
      "landlord-radio" -> "true",
      "landlord-relationship" -> ""
    )
    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("landlord-name-value", List("landlord.name.empty.error"), ArraySeq("landlord-name-value")),
      FormError("landlord-relationship", List("landlord.relationship.empty.error")))
  }

  it should "fail when landlord type (radio) is missing" in {
    val data = validData - "landlord-radio"
    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("landlord-radio", List("landlord.radio.empty.error"), List()))
  }

  it should "fail when 'Yes' is selected but no description is provided" in {
    val data = Map(
      "landlord-name-value" -> "Jane Doe",
      "landlord-radio" -> "true",
      "landlord-radio-other" -> "   " // whitespace only
    )

    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("landlord-relationship", List("landlord.relationship.empty.error"), List()))
  }

  it should "pass when 'Yes' is selected and description is provided" in {
    val data = Map(
      "landlord-name-value" -> "Jane Doe",
      "landlord-radio" -> "true",
      "landlord-relationship" -> "Friend of the family"
    )

    val boundForm = LandlordForm.form.bind(data)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(LandlordForm("Jane Doe", "true", Some("Friend of the family")))
  }

  "LandlordForm.unapply" should "extract fields correctly" in {
    val form = LandlordForm("John Doe", "true", Some("Other info"))
    val result = LandlordForm.unapply(form)

    result shouldBe Some(("John Doe", "true", Some("Other info")))
  }

  it should "handle None for landlordOther correctly" in {
    val form = LandlordForm("Jane Smith", "false", None)
    val result = LandlordForm.unapply(form)

    result shouldBe Some(("Jane Smith", "false", None))
  }

  "LandlordForm.format" should "serialize to JSON correctly" in {
    val form = LandlordForm("John Doe", "true", Some("Other info"))
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "landlordName" -> "John Doe",
      "hasRelationship" -> "true",
      "landlordRelationship" -> "Other info"
    )
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.obj(
      "landlordName" -> "Jane Smith",
      "hasRelationship" -> "true",
      "landlordRelationship" -> JsNull
    )

    val result = json.validate[LandlordForm]
    result.isSuccess shouldBe true
    result.get shouldBe LandlordForm("Jane Smith", "true", None)
  }

  def validate(form: LandlordForm): Boolean = {
      form.landlordRelationship.forall(_.length <= 250)
  }

  it should "pass if hasRelationship is Yes and landlordRelationship is <= 250 characters" in {
    val form = LandlordForm("Jane Smith","true", Some("This is a valid description."))
    validate(form) shouldBe true
  }

  it should "fail if hasRelationship is Yes and landlordRelationship is > 250 characters" in {
    val longText = "x" * 251
    val form = LandlordForm("Jane Smith", "true", Some(longText))
    validate(form) shouldBe false
  }

}

