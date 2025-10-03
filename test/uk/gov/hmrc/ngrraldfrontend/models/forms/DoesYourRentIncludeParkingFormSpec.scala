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
import play.api.libs.json.Json

class DoesYourRentIncludeParkingFormSpec extends AnyFlatSpec with Matchers {

  val validData = Map(
    "doesYourRentIncludeParking-radio-value" -> "true"
  )

  "DoesYourRentIncludeParkingForm" should "bind valid data successfully" in {
    val boundForm = DoesYourRentIncludeParkingForm.form.bind(validData)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(DoesYourRentIncludeParkingForm("true"))
  }

  it should "fail when Does Your Rent Include Parking (radio) is missing" in {
    val data = validData - "doesYourRentIncludeParking-radio-value"
    val boundForm = DoesYourRentIncludeParkingForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("doesYourRentIncludeParking-radio-value", List("doesYourRentIncludeParking.empty.error"), List()))
  }

  "DoesYourRentIncludeParkingForm.unapply" should "extract fields correctly" in {
    val form = DoesYourRentIncludeParkingForm("true")
    val result = DoesYourRentIncludeParkingForm.unapply(form)
    result shouldBe Some("true")
  }

  "DoesYourRentIncludeParkingForm.format" should "serialize to JSON correctly" in {
    val form = DoesYourRentIncludeParkingForm("true")
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "radio" -> "true",
    )
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.obj(
      "radio" -> "false",
    )

    val result = json.validate[DoesYourRentIncludeParkingForm]
    result.isSuccess shouldBe true
    result.get shouldBe DoesYourRentIncludeParkingForm("false")
  }
}
