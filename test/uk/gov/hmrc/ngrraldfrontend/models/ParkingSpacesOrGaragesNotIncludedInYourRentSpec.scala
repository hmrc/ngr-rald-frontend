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

package uk.gov.hmrc.ngrraldfrontend.models

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._
import java.time.LocalDate

class ParkingSpacesOrGaragesNotIncludedInYourRentSpec extends AnyFlatSpec with Matchers {

  // Assuming NGRDate wraps a LocalDate and has a Format defined
  "ParkingSpacesOrGaragesNotIncludedInYourRent" should "serialize to JSON correctly" in {
    val parkingInfo = ParkingSpacesOrGaragesNotIncludedInYourRent(
      uncoveredSpaces = 2,
      coveredSpaces = 1,
      garages = 1,
      totalCost = BigDecimal(150.50),
      agreementDate = NGRDate("01","10","2025")
    )

    val json = Json.toJson(parkingInfo)
    json shouldBe Json.obj(
      "uncoveredSpaces" -> 2,
      "coveredSpaces" -> 1,
      "garages" -> 1,
      "totalCost" -> 150.50,
      "agreementDate" -> Json.obj("day" -> "01", "month" -> "10", "year" -> "2025")
    )
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.obj(
      "uncoveredSpaces" -> 2,
      "coveredSpaces" -> 1,
      "garages" -> 1,
      "totalCost" -> 150.50,
      "agreementDate" -> Json.obj("day" -> "01", "month" -> "10", "year" -> "2025")
    )

    val expected = ParkingSpacesOrGaragesNotIncludedInYourRent(
      uncoveredSpaces = 2,
      coveredSpaces = 1,
      garages = 1,
      totalCost = BigDecimal(150.50),
      agreementDate = NGRDate("01","10","2025")
    )

    json.as[ParkingSpacesOrGaragesNotIncludedInYourRent] shouldBe expected
  }

  it should "fail to deserialize if required fields are missing" in {
    val invalidJson = Json.obj(
      "uncoveredSpaces" -> 2,
      "coveredSpaces" -> 1
      // missing garages, totalCost, agreementDate
    )

    assertThrows[JsResultException] {
      invalidJson.as[ParkingSpacesOrGaragesNotIncludedInYourRent]
    }
  }
}
