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

package uk.gov.hmrc.ngrraldfrontend.models.registration

import play.api.libs.json.{JsSuccess, JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport


class TradingNameSpec extends TestSupport {
  "TradingName" should {

    val tradingName = TradingName("Apple")
    val json = Json.parse("""{ "value": "Apple" }""")

    "serialize to JSON" in {
      Json.toJson(tradingName) mustBe json
    }

    "deserialize from JSON" in {
      json.validate[TradingName] mustBe JsSuccess(tradingName)
    }

    "fail to deserialize from incorrect JSON" in {
      val invalidJson = Json.parse("""{ "wrongKey": "Apple" }""")
      invalidJson.validate[TradingName].isError mustBe true
    }
  }
}
