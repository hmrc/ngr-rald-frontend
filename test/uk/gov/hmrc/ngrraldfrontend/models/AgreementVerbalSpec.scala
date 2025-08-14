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

class AgreementVerbalSpec extends TestSupport {

  val agreementVerbal: AgreementVerbal = AgreementVerbal("2025-10-30", true, Some("2027-11-30"))

  val agreementVerbalJson: JsValue = Json.parse(
    """
      |{
      |"startDate": "2025-10-30",
      |"openEnded": true,
      |"endDate": "2027-11-30"
      |}
      |""".stripMargin
  )

  "AgreementVerbal" should {
    "deserialize to json" in {
      Json.toJson(agreementVerbal) mustBe agreementVerbalJson
    }
    "serialize to json" in {
      agreementVerbalJson.as[AgreementVerbal] mustBe agreementVerbal
    }
  }
}
