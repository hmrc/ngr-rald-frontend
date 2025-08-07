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

class RentBasedOnSpec extends TestSupport {

  val rentBasedOn: RentBasedOn = RentBasedOn("Other", Some("The rent agreement"))

  val rentBasedOnJson: JsValue = Json.parse(
    """
      |{
      |"rentBased": "Other",
      |"otherDesc":"The rent agreement"
      |}
      |""".stripMargin
  )

  "RentBasedOn" should {
    "deserialize to json" in {
      Json.toJson(rentBasedOn) mustBe rentBasedOnJson
    }
    "serialize to json" in {
      rentBasedOnJson.as[RentBasedOn] mustBe rentBasedOn
    }
  }
}
