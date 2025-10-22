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

import java.time.LocalDate

class ProvideDetailsOfFirstRentPeriodModelSpec extends AnyFlatSpec with Matchers:

  "ProvideDetailsOfFirstRentPeriod" should "serialize to JSON correctly" in {
    val rentDetails = ProvideDetailsOfFirstRentPeriod(
      startDate = LocalDate.of(2025, 1, 1),
      endDate = LocalDate.of(2025, 1, 31),
      isRentPayablePeriod = true,
      rentPeriodAmount = Some(BigDecimal(1000))
    )

    val json = Json.toJson(rentDetails)
    (json \ "startDate").as[String] shouldBe "2025-01-01"
    (json \ "endDate").as[String] shouldBe "2025-01-31"
    (json \ "isRentPayablePeriod").as[Boolean] shouldBe true
    (json \ "rentPeriodAmount").asOpt[BigDecimal] shouldBe Some(BigDecimal(1000))
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.parse(
      """
        |{
        |  "startDate": "2025-01-01",
        |  "endDate": "2025-01-31",
        |  "isRentPayablePeriod": true,
        |  "rentPeriodAmount": 111000
        |}
        |""".stripMargin)

    val result = json.as[ProvideDetailsOfFirstRentPeriod]
    result.startDate shouldBe LocalDate.of(2025, 1, 1)
    result.endDate shouldBe LocalDate.of(2025, 1, 31)
    result.isRentPayablePeriod shouldBe true
    result.rentPeriodAmount shouldBe Some(BigDecimal(111000))
  }

  it should "handle skipped optional fields gracefully" in {
    val json = Json.parse(
      """
        |{
        |  "startDate": "2025-01-01",
        |  "endDate": "2025-01-31",
        |  "isRentPayablePeriod": false
        |}
        |""".stripMargin)

    val result = json.as[ProvideDetailsOfFirstRentPeriod]
    result.startDate shouldBe LocalDate.of(2025, 1, 1)
    result.endDate shouldBe LocalDate.of(2025, 1, 31)
    result.isRentPayablePeriod shouldBe false
    result.rentPeriodAmount shouldBe None
  }
