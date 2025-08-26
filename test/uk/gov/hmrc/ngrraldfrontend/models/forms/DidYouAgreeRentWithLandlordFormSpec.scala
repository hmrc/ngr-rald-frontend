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

import org.scalatest.matchers.must.Matchers
import org.scalatest.matchers.should.Matchers.{should, shouldBe}
import org.scalatest.wordspec.AnyWordSpec
import play.api.data.FormError
import play.api.libs.json.Json

class DidYouAgreeRentWithLandlordFormSpec extends AnyWordSpec with Matchers {

  "DidYouAgreeRentWithLandlordForm" should {

    "bind successfully with a valid DidYouAgreeRentWithLandlordRadio value" in {
      val data = Map("did-you-agree-rent-with-landlord-radio" -> "YesTheLandlord")
      val boundForm = DidYouAgreeRentWithLandlordForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(DidYouAgreeRentWithLandlordForm("YesTheLandlord"))
    }

    "fail to bind when DidYouAgreeRentWithLandlordRadio is missing" in {
      val data = Map.empty[String, String]
      val boundForm = DidYouAgreeRentWithLandlordForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("did-you-agree-rent-with-landlord-radio", List("didYouAgreeRentWithLandlord.error.required")))
    }

    "fail to bind when DidYouAgreeRentWithLandlordRadio is empty" in {
      val data = Map("did-you-agree-rent-with-landlord-radio" -> "")
      val boundForm = DidYouAgreeRentWithLandlordForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError("did-you-agree-rent-with-landlord-radio", List("didYouAgreeRentWithLandlord.error.required")))
    }


    "serialize to JSON correctly" in {
      val form = DidYouAgreeRentWithLandlordForm("YesTheLandlord")
      val json = Json.toJson(form)

      json shouldBe Json.obj(
        "radioValue" -> "YesTheLandlord"
      )
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj("radioValue" -> "NoACourtSet")
      val result = json.validate[DidYouAgreeRentWithLandlordForm]

      result.isSuccess shouldBe true
      result.get shouldBe DidYouAgreeRentWithLandlordForm("NoACourtSet")
    }

    "fail deserialization if DidYouAgreeRentWithLandlordRadio is missing" in {
      val json = Json.obj()
      val result = json.validate[DidYouAgreeRentWithLandlordForm]

      result.isError shouldBe true
    }
  }
}
