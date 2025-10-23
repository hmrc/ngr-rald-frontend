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

class AboutRepairsAndFittingOutSpec extends TestSupport {

  val aboutRepairsAndFittingOut: AboutRepairsAndFittingOut =
    AboutRepairsAndFittingOut(
      cost = BigDecimal("1234.56"),
      date = "10/2025"
    )

  val aboutRepairsAndFittingOutJson: JsValue = Json.parse(
    """
      |{
      |  "cost": 1234.56,
      |  "date": "10/2025"
      |}
      |""".stripMargin
  )

  "AboutRepairsAndFittingOut" should {
    "serialize to json" in {
      Json.toJson(aboutRepairsAndFittingOut) mustBe aboutRepairsAndFittingOutJson
    }

    "deserialize from json" in {
      aboutRepairsAndFittingOutJson.as[AboutRepairsAndFittingOut] mustBe aboutRepairsAndFittingOut
    }
  }
}