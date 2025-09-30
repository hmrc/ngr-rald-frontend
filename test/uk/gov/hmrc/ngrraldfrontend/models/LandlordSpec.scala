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

import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport

class LandlordSpec extends TestSupport {

  val landlord: Landlord = Landlord("John Doe", true, Some("Other description"))

  val landlordJson: JsValue = Json.parse(
    """
      |{
      |"landlordName": "John Doe",
      |"hasRelationship": true,
      |"landlordRelationship":"Other description"
      |}
      |""".stripMargin
  )

  "RentBasedOn" should {
    "deserialize to json" in {
      Json.toJson(landlord) mustBe landlordJson
    }
    "serialize to json" in {
      landlordJson.as[Landlord] mustBe landlord
    }
  }
}
