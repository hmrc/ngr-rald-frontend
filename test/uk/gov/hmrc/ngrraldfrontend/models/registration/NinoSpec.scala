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

import play.api.libs.json.{JsString, JsValue, Json}
import uk.gov.hmrc.ngrraldfrontend.helpers.TestSupport


class NinoSpec extends TestSupport {

  val nino: Nino = Nino("AA055075C")

  val ninoJson: JsValue = Json.parse(
    """
      |{
      |"nino": "AA055075C"
      |}
      |""".stripMargin
  )

  "Nino" should {
    "deserialize to json" in {
      Json.toJson(nino) mustBe JsString("AA055075C")
    }
    "serialize to json" in {
      ninoJson.as[Nino] mustBe nino
    }
  }
}
