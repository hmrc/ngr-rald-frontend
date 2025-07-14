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

import play.api.libs.json.{JsString, JsSuccess, JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport


class EmailSpec extends TestSupport {
  "Email" should {

    val email = Email("Josh@gmail.com")
    val json = Json.parse("""{ "value": "Josh@gmail.com" }""")

    "serialize to JSON" in {
      Json.toJson(email) mustBe json
    }

    "deserialize from JSON" in {
      json.validate[Email] mustBe JsSuccess(email)
    }

    "fail to deserialize from incorrect JSON" in {
      val invalidJson = Json.parse("""{ "wrongKey": "Josh@gmail.com" }""")
      invalidJson.validate[Email].isError mustBe true
    }
  }
}
