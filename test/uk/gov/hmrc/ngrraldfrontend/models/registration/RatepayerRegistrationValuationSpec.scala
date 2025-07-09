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

import play.api.libs.json.Json
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport
import uk.gov.hmrc.ngrraldfrontend.models.registration.RatepayerRegistrationValuation

class RatepayerRegistrationValuationSpec extends TestSupport {

  "RatepayerRegistrationValuationModel" should {
    "serialise into Json" when {
      "all fields are present" in {
        Json.toJson(regValuationModel) mustBe regValuationJson
      }
      "optional fields are not present" in {
        Json.toJson(minRegValuationModel) mustBe minRegValuationJson
      }
    }
    "deserialise from Json" when {
      "all fields are present" in {
        regValuationJson.as[RatepayerRegistrationValuation] mustBe regValuationModel
      }
      "the optional fields are not present" in {
        minRegValuationJson.as[RatepayerRegistrationValuation] mustBe minRegValuationModel
      }
    }
  }

}
