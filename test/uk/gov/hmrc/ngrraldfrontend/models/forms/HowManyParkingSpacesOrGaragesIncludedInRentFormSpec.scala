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
import play.api.libs.json.Json

import scala.collection.immutable.ArraySeq

class HowManyParkingSpacesOrGaragesIncludedInRentFormSpec extends AnyWordSpec with Matchers {

  "HowManyParkingSpacesOrGaragesIncludedInRentForm" should {

    "bind valid input" in {
      val data = Map(
        "uncoveredSpaces" -> "100",
        "coveredSpaces" -> "100",
        "garages" -> "100"
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowManyParkingSpacesOrGaragesIncludedInRentForm(uncoveredSpaces = 100, coveredSpaces = 100, garages = 100))
    }

    "bind empty input" in {
      val data = Map(
        "uncoveredSpaces" -> "",
        "coveredSpaces" -> "100",
        "garages" -> "100"
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
    }

    "bind field with commas input" in {
      val data = Map(
        "uncoveredSpaces" -> "1,000",
        "coveredSpaces" -> "100",
        "garages" -> "100"
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
    }

    "fail to bind field with decimal place" in {
      val data = Map(
        "uncoveredSpaces" -> "10.5",
        "coveredSpaces" -> "100",
        "garages" -> "100"
      )


      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("uncoveredSpaces", List("howManyParkingSpacesOrGaragesIncludedInRent.uncoveredSpaces.wholeNum.error"), ArraySeq("""^\d+$""")))
    }

    "fail to bind non-numeric input" in {
      val data = Map(
        "uncoveredSpaces" -> "abc",
        "coveredSpaces" -> "100",
        "garages" -> "100"
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.errors shouldBe List(FormError("uncoveredSpaces", List("howManyParkingSpacesOrGaragesIncludedInRent.uncoveredSpaces.wholeNum.error"), ArraySeq("""^\d+$""")))
    }

    "fail to bind input greater than 9,999" in {
      val data = Map(
        "uncoveredSpaces" -> "10000",
        "coveredSpaces" -> "100",
        "garages" -> "100"
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("uncoveredSpaces", List("howManyParkingSpacesOrGaragesIncludedInRent.uncoveredSpaces.tooHigh.error"), ArraySeq(9999)))
    }

    "fail to bind when no input fields are entered" in {
      val data = Map(
        "uncoveredSpaces" -> "",
        "coveredSpaces" -> "",
        "garages" -> ""
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("", "howManyParkingSpacesOrGaragesIncludedInRent.error.required"))
    }

    "fail to bind when input fields are all 0" in {
      val data = Map(
        "uncoveredSpaces" -> "0",
        "coveredSpaces" -> "0",
        "garages" -> "0"
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("", "howManyParkingSpacesOrGaragesIncludedInRent.error.required"))
    }

    "bind edge case of exactly 9,999" in {
      val data = Map(
        "uncoveredSpaces" -> "9999",
        "coveredSpaces" -> "100",
        "garages" -> "100"
      )
      val boundForm = HowManyParkingSpacesOrGaragesIncludedInRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(HowManyParkingSpacesOrGaragesIncludedInRentForm(uncoveredSpaces = 9999, coveredSpaces = 100, garages = 100))
    }
  }

  "serialize to JSON correctly" in {
    val form = HowManyParkingSpacesOrGaragesIncludedInRentForm(10, 10, 10)
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "uncoveredSpaces" -> 10,
      "coveredSpaces" -> 10,
      "garages" -> 10
    )
  }

  "deserialize from JSON correctly" in {
    val json = Json.obj(
      "uncoveredSpaces" -> 10,
      "coveredSpaces" -> 10,
      "garages" -> 10
    )
    val result = json.validate[HowManyParkingSpacesOrGaragesIncludedInRentForm]

    result.isSuccess shouldBe true
    result.get shouldBe HowManyParkingSpacesOrGaragesIncludedInRentForm(10,10,10)
  }

  "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[HowManyParkingSpacesOrGaragesIncludedInRentForm]

    result.isError shouldBe true
  }
}
