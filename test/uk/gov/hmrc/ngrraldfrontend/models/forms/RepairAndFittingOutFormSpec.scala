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

class RepairAndFittingOutFormSpec extends AnyFlatSpec with Matchers {

  val validData = Map(
    "repairsAndFittingOut-radio-value" -> "true"
  )

  "RepairAndFittingOutForm" should "bind valid data successfully" in {
    val boundForm = RepairsAndFittingOutForm.form.bind(validData)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(RepairsAndFittingOutForm("true"))
  }

  it should "fail when repairsAndFittingOut (radio) is missing" in {
    val data = validData - "repairsAndFittingOut-radio-value"
    val boundForm = RepairsAndFittingOutForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("repairsAndFittingOut-radio-value", List("repairsAndFittingOut.empty.error"), List()))
  }

  it should "fail to bind when repairsAndFittingOut is empty" in {
    val data = Map("repairsAndFittingOut-radio-value" -> "")
    val boundForm = RepairsAndFittingOutForm.form.bind(data)

    boundForm.hasErrors shouldBe true
    boundForm.errors should contain(FormError("repairsAndFittingOut-radio-value", List("repairsAndFittingOut.empty.error")))
  }

  it should "fail when no is selected" in {
    val data = Map(
      "repairsAndFittingOut-radio-value" -> "false",
    )

    val boundForm = RepairsAndFittingOutForm.form.bind(data)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(RepairsAndFittingOutForm("false"))
  }

  "DoesYourRentIncludeParkingForm.format" should "serialize to JSON correctly" in {
    val form = RepairsAndFittingOutForm("true")
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "radioValue" -> "true",
    )
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.obj(
      "radioValue" -> "false",
    )

    val result = json.validate[RepairsAndFittingOutForm]
    result.isSuccess shouldBe true
    result.get shouldBe RepairsAndFittingOutForm("false")
  }
}
