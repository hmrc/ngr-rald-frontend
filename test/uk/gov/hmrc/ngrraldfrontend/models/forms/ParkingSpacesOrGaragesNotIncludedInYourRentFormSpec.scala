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
import uk.gov.hmrc.ngrraldfrontend.models.NGRDate

import scala.collection.immutable.ArraySeq

class ParkingSpacesOrGaragesNotIncludedInYourRentFormSpec  extends AnyWordSpec with Matchers {

  "ParkingSpacesOrGaragesNotIncludedInYourRentForm" should {

    "bind valid input" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"

      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(
        ParkingSpacesOrGaragesNotIncludedInYourRentForm(
          uncoveredSpaces = 1,
          coveredSpaces = 0,
          garages = 0,
          totalCost = BigDecimal(2000),
          agreementDate = NGRDate(day = "01",month = "10" ,year = "2025")
        )
      )
    }

    "bind with 2 empty parking inputs" in {
      val data = Map(
        "uncoveredSpaces" -> "",
        "coveredSpaces" -> "1",
        "garages" -> "",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)
      boundForm.hasErrors shouldBe false
      boundForm.errors shouldBe List()
    }

    "bind with 1 empty parking input" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "1",
        "garages" -> "",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)
      boundForm.hasErrors shouldBe false
      boundForm.errors shouldBe List()
    }

    "fail to bind with 3 empty parking inputs" in {
      val data = Map(
        "uncoveredSpaces" -> "",
        "coveredSpaces" -> "",
        "garages" -> "",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("", "parkingSpacesOrGaragesNotIncludedInYourRent.error.required"))
    }

    "bind field with commas within parking input" in {
      val data = Map(
        "uncoveredSpaces" -> "1,000",
        "coveredSpaces" -> "100",
        "garages" -> "100",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)
      boundForm.hasErrors shouldBe false
    }

    "fail to bind field with decimal place in parking field" in {
      val data = Map(
        "uncoveredSpaces" -> "10.5",
        "coveredSpaces" -> "100",
        "garages" -> "100",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )

      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)
      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("uncoveredSpaces", List("parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces.wholeNum.error"), ArraySeq("""^\d+$""")))
    }

    "fail to bind non-numeric input in parking field" in {
      val data = Map(
        "uncoveredSpaces" -> "abc",
        "coveredSpaces" -> "100",
        "garages" -> "100",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.errors shouldBe List(FormError("uncoveredSpaces", List("parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces.wholeNum.error"), ArraySeq("""^\d+$""")))
    }

    "fail to bind input greater than 9,999 in parking field" in {
      val data = Map(
        "uncoveredSpaces" -> "10000",
        "coveredSpaces" -> "100",
        "garages" -> "100",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("uncoveredSpaces", List("parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces.tooHigh.error"), ArraySeq(9999)))
    }

    "fail to bind input is a very large number in parking field" in {
      val data = Map(
        "uncoveredSpaces" -> "100000000000000000",
        "coveredSpaces" -> "100",
        "garages" -> "100",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("uncoveredSpaces", List("parkingSpacesOrGaragesNotIncludedInYourRent.uncoveredSpaces.tooHigh.error"), ArraySeq(9999)))
    }


    "fail to bind when input fields are all 0 in parking inputs" in {
      val data = Map(
        "uncoveredSpaces" -> "0",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("", "parkingSpacesOrGaragesNotIncludedInYourRent.error.required"))
    }

    "bind edge case of exactly 9,999 in parking field" in {
      val data = Map(
        "uncoveredSpaces" -> "9999",
        "coveredSpaces" -> "100",
        "garages" -> "100",
        "totalCost" -> "2000",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe false
      boundForm.value shouldBe Some(
        ParkingSpacesOrGaragesNotIncludedInYourRentForm(
          uncoveredSpaces = 9999,
          coveredSpaces = 100,
          garages = 100,
          totalCost = BigDecimal(2000),
          agreementDate = NGRDate(day = "01",month = "10" ,year = "2025")
        )
      )
    }
    "fail to bind when the input field is 0 for total cost" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "0",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("totalCost", List("parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.minimum.error"), ArraySeq(1)))
    }
    "fail to bind when the input field is non numeric for total cost" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "hello",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("totalCost", List("parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.invalid.error"), ArraySeq("""([0-9]+\.[0-9]+|[0-9]+)""")))
    }
    "fail to bind when the input field is greater than £9,999,999.99 for total cost" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "99999991.99",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("totalCost", List("parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.max.error"), ArraySeq(9999999.99)))
    }
    "fail to bind when the input field is not inputed for total cost" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("totalCost", List("parkingSpacesOrGaragesNotIncludedInYourRent.totalCost.required.error"), ArraySeq("totalCost")))
    }
    "fail to bind when the input field day is missing in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.day.required.error")))
    }
    "fail to bind when the input field month is missing in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "1",
        "agreementDate.month" -> "",
        "agreementDate.year" -> "2025"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.month.required.error")))
    }

    "fail to bind when the input field year is missing in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "1",
        "agreementDate.month" -> "10",
        "agreementDate.year" -> ""
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.year.required.error")))
    }

    "fail to bind when the input field, day, month and year is missing in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "",
        "agreementDate.month" -> "",
        "agreementDate.year" -> ""
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.required.error")))
    }

    "fail to bind when the input field year is before 1900 in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "12",
        "agreementDate.year" -> "1700"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.before.1900.error")))
    }

    "fail to bind when the input field month is over 12 in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "01",
        "agreementDate.month" -> "13",
        "agreementDate.year" -> "2020"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.invalid.error")))
    }

    "fail to bind when the input field day is too high in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "35",
        "agreementDate.month" -> "13",
        "agreementDate.year" -> "2020"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.invalid.error")))
    }

    "fail to bind when the input field day and month is missing in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "",
        "agreementDate.month" -> "",
        "agreementDate.year" -> "2020"
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.dayAndMonth.required.error")))
    }

    "fail to bind when the input field day and year is missing in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "",
        "agreementDate.month" -> "11",
        "agreementDate.year" -> ""
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.dayAndYear.required.error")))
    }

    "fail to bind when the input field month and year is missing in agreementDate" in {
      val data = Map(
        "uncoveredSpaces" -> "1",
        "coveredSpaces" -> "0",
        "garages" -> "0",
        "totalCost" -> "1",
        "agreementDate.day" -> "2",
        "agreementDate.month" -> "",
        "agreementDate.year" -> ""
      )
      val boundForm = ParkingSpacesOrGaragesNotIncludedInYourRentForm.form.bind(data)

      boundForm.hasErrors shouldBe true
      boundForm.errors shouldBe List(FormError("agreementDate", List("parkingSpacesOrGaragesNotIncludedInYourRent.agreementDate.monthAndYear.required.error")))
    }
  }


  "serialize to JSON correctly" in {
    val form = ParkingSpacesOrGaragesNotIncludedInYourRentForm(10, 10, 10, BigDecimal(10), NGRDate("10", "10", "2025"))
    val json = Json.toJson(form)

    json shouldBe Json.obj(
      "uncoveredSpaces" -> 10,
      "coveredSpaces" -> 10,
      "garages" -> 10,
      "totalCost" -> 10,
      "agreementDate" -> Json.obj("day" -> "10", "month" -> "10", "year" -> "2025")
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
    result.get shouldBe HowManyParkingSpacesOrGaragesIncludedInRentForm(10, 10, 10)
  }

  "fail deserialization if value is missing" in {
    val json = Json.obj()
    val result = json.validate[HowManyParkingSpacesOrGaragesIncludedInRentForm]

    result.isError shouldBe true
  }
}

