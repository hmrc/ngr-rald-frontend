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

class DidYouGetMoneyFromLandlordFormSpec extends AnyFlatSpec with Matchers {

  val validData = Map(
    "didYouGetMoneyFromLandlord-radio-value" -> "true"
  )

  "didYouGetMoneyFromLandlordForm" should "bind valid data successfully" in {
    val boundForm = DidYouGetMoneyFromLandlordForm.form.bind(validData)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(DidYouGetMoneyFromLandlordForm("true"))
  }

  it should "fail when didYouGetMoneyFromLandlord (radio) is missing" in {
    val data = validData - "didYouGetMoneyFromLandlord-radio-value"
    val boundForm = DidYouGetMoneyFromLandlordForm.form.bind(data)

    boundForm.errors shouldBe List(FormError("didYouGetMoneyFromLandlord-radio-value", List("didYouGetMoneyFromLandlord.empty.error"), List()))
  }

  it should "fail to bind when didYouGetMoneyFromLandlord is empty" in {
    val data = Map("didYouGetMoneyFromLandlord-radio-value" -> "")
    val boundForm = DidYouGetMoneyFromLandlordForm.form.bind(data)

    boundForm.hasErrors shouldBe true
    boundForm.errors should contain(FormError("didYouGetMoneyFromLandlord-radio-value", List("didYouGetMoneyFromLandlord.empty.error")))
  }

  it should "fail when no is selected" in {
    val data = Map(
      "didYouGetMoneyFromLandlord-radio-value" -> "false",
    )

    val boundForm = DidYouGetMoneyFromLandlordForm.form.bind(data)

    boundForm.errors shouldBe empty
    boundForm.value shouldBe Some(DidYouGetMoneyFromLandlordForm("false"))
  }

  "DoesYourRentIncludeParkingForm.format" should "serialize to JSON correctly" in {
    val form = DidYouGetMoneyFromLandlordForm("Yes")
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "radio" -> "Yes",
    )
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.obj(
      "radio" -> "No",
    )

    val result = json.validate[DidYouGetMoneyFromLandlordForm]
    result.isSuccess shouldBe true
    result.get shouldBe DidYouGetMoneyFromLandlordForm("No")
  }
}
