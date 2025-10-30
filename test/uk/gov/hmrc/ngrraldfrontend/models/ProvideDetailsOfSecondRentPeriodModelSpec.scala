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

class ProvideDetailsOfSecondRentPeriodModelSpec extends AnyFlatSpec with Matchers:

  "ProvideDetailsOfSecondRentPeriod" should "serialize to JSON correctly" in {
    val rentDetails = ProvideDetailsOfSecondRentPeriod(
      endDate = LocalDate.of(2025, 1, 31),
      rentPeriodAmount = BigDecimal(1000)
    )

    val json = Json.toJson(rentDetails)
    (json \ "endDate").as[String] shouldBe "2025-01-31"
    (json \ "rentPeriodAmount").as[BigDecimal] shouldBe BigDecimal(1000)
  }

  it should "deserialize from JSON correctly" in {
    val json = Json.parse(
      """
        |{
        |  "endDate": "2025-01-31",
        |  "rentPeriodAmount": 111000
        |}
        |""".stripMargin)

    val result = json.as[ProvideDetailsOfSecondRentPeriod]
    result.endDate shouldBe LocalDate.of(2025, 1, 31)
    result.rentPeriodAmount shouldBe BigDecimal(111000)
  }
