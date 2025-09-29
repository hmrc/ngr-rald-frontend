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

class DoYouPayExtraForParkingSpacesFormSpec extends AnyWordSpec with Matchers {
  val requiredError = "doYouPayExtraForParkingSpaces.required.error"
  val checkRentPeriodModel: DoYouPayExtraForParkingSpacesForm = DoYouPayExtraForParkingSpacesForm("Yes")
  val checkRentPeriodJson: JsValue = Json.parse("""{"radioValue":"Yes"}
                                                  |""".stripMargin)

  "DoYouPayExtraForParkingSpacesForm" should {
    "serialize into json" in {
      Json.toJson(checkRentPeriodModel) shouldBe checkRentPeriodJson
    }
    "deserialize from json" in {
      checkRentPeriodJson.as[DoYouPayExtraForParkingSpacesForm] shouldBe checkRentPeriodModel
    }
    "bind successfully with a valid value yes" in {
      val data = Map(DoYouPayExtraForParkingSpacesForm.payExtraRadio -> "yes")
      val boundForm = DoYouPayExtraForParkingSpacesForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(DoYouPayExtraForParkingSpacesForm("yes"))
    }
    "bind successfully with a valid value no" in {
      val data = Map(DoYouPayExtraForParkingSpacesForm.payExtraRadio -> "no")
      val boundForm = DoYouPayExtraForParkingSpacesForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(DoYouPayExtraForParkingSpacesForm("no"))
    }
    "return an error when the radio value is empty" in {
      val data = Map(DoYouPayExtraForParkingSpacesForm.payExtraRadio -> "")
      val boundForm = DoYouPayExtraForParkingSpacesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(DoYouPayExtraForParkingSpacesForm.payExtraRadio, requiredError))
    }

    "return an error when the radio value is missing" in {
      val data = Map.empty[String, String]
      val boundForm = DoYouPayExtraForParkingSpacesForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors should contain(FormError(DoYouPayExtraForParkingSpacesForm.payExtraRadio, requiredError))
    }

    "unbind correctly to a data map" in {
      val form = DoYouPayExtraForParkingSpacesForm.form.fill(DoYouPayExtraForParkingSpacesForm("no"))
      form.data shouldBe Map(DoYouPayExtraForParkingSpacesForm.payExtraRadio -> "no")
    }
  }

}
