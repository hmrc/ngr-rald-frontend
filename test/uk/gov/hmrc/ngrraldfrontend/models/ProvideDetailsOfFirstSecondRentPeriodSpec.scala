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

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import play.api.libs.json._

class ProvideDetailsOfFirstSecondRentPeriodSpec extends AnyFlatSpec with Matchers {

  "ProvideDetailsOfFirstSecondRentPeriod" should "serialize to JSON correctly" in {
    val rentDetails = ProvideDetailsOfFirstSecondRentPeriod(
      firstDateStart = "2025-01-01",
      firstDateEnd = "2025-01-31",
      firstRentPeriodRadio = true,
      firstRentPeriodAmount = Some("1000"),
      secondDateStart = "2025-02-01",
      secondDateEnd = "2025-02-28",
      secondHowMuchIsRent = "1200"
    )

    val json = Json.toJson(rentDetails)
    (json \ "firstDateStart").as[String] shouldBe "2025-01-01"
    (json \ "firstRentPeriodAmount").asOpt[String] shouldBe Some("1000")
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.parse(
      """
        |{
        |  "firstDateStart": "2025-01-01",
        |  "firstDateEnd": "2025-01-31",
        |  "firstRentPeriodRadio": true,
        |  "firstRentPeriodAmount": "1000",
        |  "secondDateStart": "2025-02-01",
        |  "secondDateEnd": "2025-02-28",
        |  "secondHowMuchIsRent": "1200"
        |}
        |""".stripMargin)

    val result = json.as[ProvideDetailsOfFirstSecondRentPeriod]
    result.firstDateStart shouldBe "2025-01-01"
    result.firstRentPeriodAmount shouldBe Some("1000")
  }

  it should "handle missing optional field gracefully" in {
    val json = Json.parse(
      """
        |{
        |  "firstDateStart": "2025-01-01",
        |  "firstDateEnd": "2025-01-31",
        |  "firstRentPeriodRadio": false,
        |  "secondDateStart": "2025-02-01",
        |  "secondDateEnd": "2025-02-28",
        |  "secondHowMuchIsRent": "1200"
        |}
        |""".stripMargin)

    val result = json.as[ProvideDetailsOfFirstSecondRentPeriod]
    result.firstRentPeriodAmount shouldBe None
  }
}