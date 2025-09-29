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

import org.scalatest.wordspec.AnyWordSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class RepairsAndInsuranceSpec extends AnyWordSpec with Matchers {

  "RepairsAndInsurance JSON format" should {

    "serialize to JSON correctly" in {
      val model = RepairsAndInsurance(
        internalRepairs = "You",
        externalRepairs = "The landlord",
        buildingInsurance = "You and the landlord"
      )

      val expectedJson = Json.obj(
        "internalRepairs" -> "You",
        "externalRepairs" -> "The landlord",
        "buildingInsurance" -> "You and the landlord"
      )

      Json.toJson(model) shouldBe expectedJson
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj(
        "internalRepairs" -> "You",
        "externalRepairs" -> "The landlord",
        "buildingInsurance" -> "You and the landlord"
      )

      val expectedModel = RepairsAndInsurance(
        internalRepairs = "You",
        externalRepairs = "The landlord",
        buildingInsurance = "You and the landlord"
      )

      json.as[RepairsAndInsurance] shouldBe expectedModel
    }

    "fail to deserialize if a field is missing" in {
      val incompleteJson = Json.obj(
        "internalRepairs" -> "You",
        "externalRepairs" -> "The landlord"
      )

      assertThrows[JsResultException] {
        incompleteJson.as[RepairsAndInsurance]
      }
    }
  }
}

