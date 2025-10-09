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

import org.scalatestplus.play.PlaySpec
import play.api.libs.json._

class WhatYourRentIncludesSpec extends PlaySpec {

  "WhatYourRentIncludes" should {

    "serialize to JSON correctly" in {
      val rentIncludes = WhatYourRentIncludes(
        livingAccommodation = true,
        rentPartAddress = false,
        rentEmptyShell = true,
        rentIncBusinessRates = Some(false),
        rentIncWaterCharges = Some(true),
        rentIncService = Some(false),
        bedroomNumbers = Some(6)
      )

      val expectedJson = Json.parse(
        """
          {
            "livingAccommodation": true,
            "rentPartAddress": false,
            "rentEmptyShell": true,
            "rentIncBusinessRates": false,
            "rentIncWaterCharges": true,
            "rentIncService": false,
            "bedroomNumbers": 6
          }
        """
      )

      Json.toJson(rentIncludes) mustBe expectedJson
    }

    "deserialize from JSON correctly" in {
      val json = Json.parse(
        """
          {
            "livingAccommodation": false,
            "rentPartAddress": true,
            "rentEmptyShell": false,
            "rentIncBusinessRates": true,
            "rentIncWaterCharges": false,
            "rentIncService": true
          }
        """
      )

      val expected = WhatYourRentIncludes(
        livingAccommodation = false,
        rentPartAddress = true,
        rentEmptyShell = false,
        rentIncBusinessRates = Some(true),
        rentIncWaterCharges = Some(false),
        rentIncService = Some(true),
        bedroomNumbers = None
      )

      json.as[WhatYourRentIncludes] mustBe expected
    }

    "fail to deserialize if a field is missing" in {
      val incompleteJson = Json.parse(
        """
          {
            "livingAccommodation": true,
            "rentPartAddress": false
          }
        """
      )

      assertThrows[JsResultException] {
        incompleteJson.as[WhatYourRentIncludes]
      }
    }
  }
}

