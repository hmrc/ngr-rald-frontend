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


class CredIdSpec extends TestSupport {
  "CredId" should {

    val credId = CredId("1234")
    val json = Json.parse("""{ "value": "1234" }""")

    "serialize to JSON" in {
      Json.toJson(credId) mustBe json
    }

    "deserialize from JSON" in {
      json.validate[CredId] mustBe JsSuccess(credId)
    }

    "fail to deserialize from incorrect JSON" in {
      val invalidJson = Json.parse("""{ "wrongKey": "1234" }""")
      invalidJson.validate[CredId].isError mustBe true
    }
  }
}
