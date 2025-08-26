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
import play.api.libs.json.*

class AgreementSpec extends AnyFlatSpec with Matchers {

  "Agreement" should "serialize to JSON correctly" in {
    val agreement = Agreement(
      agreementStart = "2025-01-01",
      isOpenEnded = true,
      openEndedDate = Some("2026-01-01"),
      haveBreakClause = true,
      breakClauseInfo = Some("6-month notice")
    )

    val json = Json.toJson(agreement)
    (json \ "agreementStart").as[String] shouldBe "2025-01-01"
    (json \ "openEndedDate").asOpt[String] shouldBe Some("2026-01-01")
    (json \ "breakClauseInfo").asOpt[String] shouldBe Some("6-month notice")
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.parse(
      """
        |{
        |  "agreementStart": "2025-01-01",
        |  "isOpenEnded": true,
        |  "openEndedDate": "2026-01-01",
        |  "haveBreakClause": true,
        |  "breakClauseInfo": "6-month notice"
        |}
        |""".stripMargin)

    val result = json.as[Agreement]
    result.agreementStart shouldBe "2025-01-01"
    result.openEndedDate shouldBe Some("2026-01-01")
    result.breakClauseInfo shouldBe Some("6-month notice")
  }

  it should "handle missing optional fields gracefully" in {
    val json = Json.parse(
      """
        |{
        |  "agreementStart": "2025-01-01",
        |  "isOpenEnded": false,
        |  "haveBreakClause": false
        |}
        |""".stripMargin)

    val result = json.as[Agreement]
    result.openEndedDate shouldBe None
    result.breakClauseInfo shouldBe None
  }
}

